package edu.scu;

import edu.scu.util.Logger;
import edu.scu.util.Util;
import edu.scu.thread.ListenerThread;
import edu.scu.thread.PredecessorCheckThread;
import edu.scu.thread.StabilizeThread;
import edu.scu.thread.UpdateFingersThread;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Node class that implements the core data structure
 * and functionalities of a chord node.
 *
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 */

public class Node {

    private long localId;
    private InetSocketAddress localAddress;
    private InetSocketAddress predecessor;
    private HashMap<Integer, InetSocketAddress> finger;

    private ListenerThread listenerThread;
    private StabilizeThread stabilizeThread;
    private UpdateFingersThread fix_fingers;
    private PredecessorCheckThread ask_predecessor;

    public Node(InetSocketAddress address) {
        localAddress = address;
        localId = Util.hashSocketAddress(localAddress);

        // initialize an empty finger table
        finger = new HashMap<Integer, InetSocketAddress>();
        for (int i = 1; i <= 32; i++) {
            updateIthFinger(i, null);
        }

        predecessor = null;

        // initialize threads
        listenerThread = new ListenerThread(this);
        stabilizeThread = new StabilizeThread(this);
        fix_fingers = new UpdateFingersThread(this);
        ask_predecessor = new PredecessorCheckThread(this);
    }

    /**
     * Create or join a chord ring.
     */
    public boolean join(InetSocketAddress contact) {

        // if contact is other node (join ring), try to contact that node
        // (contact will never be null)
        if (contact != null && !contact.equals(localAddress)) {
            InetSocketAddress successor = Util.requestAddress(contact, "FINDSUCC_" + localId);
            if (successor == null) {
                Logger.log("\nCannot find node you are trying to contact. Please exit.\n");
                return false;
            }
            updateIthFinger(1, successor);
        }

        // start all threads
        listenerThread.start();
        stabilizeThread.start();
        fix_fingers.start();
        ask_predecessor.start();

        return true;
    }

    /**
     * Notify successor that this node should be its predecessor.
     */
    public String notify(InetSocketAddress successor) {
        if (successor != null && !successor.equals(localAddress))
            return Util.sendRequest(successor, "IAMPRE_" + localAddress.getAddress().toString() + ":" + localAddress.getPort());
        else
            return null;
    }

    /**
     * Being notified by another node, set it as my predecessor if it is.
     */
    public void notified(InetSocketAddress newPredecessor) {
        if (predecessor == null || predecessor.equals(localAddress)) {
            this.setPredecessor(newPredecessor);
        } else {
            long oldPredecessorId = Util.hashSocketAddress(predecessor);
            long localRelativeId = Util.computeRelativeId(localId, oldPredecessorId);
            long newPredecessorRelativeId = Util.computeRelativeId(Util.hashSocketAddress(newPredecessor), oldPredecessorId);
            if (newPredecessorRelativeId > 0 && newPredecessorRelativeId < localRelativeId)
                this.setPredecessor(newPredecessor);
        }
    }

    /**
     * Ask current node to find id's successor.
     */
    public InetSocketAddress findSuccessor(long id) {

        InetSocketAddress value = this.getSuccessor();
        InetSocketAddress pre = findPredecessor(id);

        // if other node found, ask it for its successor
        if (!pre.equals(localAddress))
            value = Util.requestAddress(pre, "YOURSUCC");

        if (value == null)
            value = localAddress;

        return value;
    }

    /**
     * Ask current node to find id's predecessor
     */
    private InetSocketAddress findPredecessor(long findId) {
        InetSocketAddress n = this.localAddress;
        InetSocketAddress nextSuccessor = this.getSuccessor();
        InetSocketAddress mostRecentlyAlive = this.localAddress;
        long nextSuccessorRelativeId = 0;

        if (nextSuccessor != null)
            nextSuccessorRelativeId = Util.computeRelativeId(Util.hashSocketAddress(nextSuccessor), Util.hashSocketAddress(n));

        long findRelativeId = Util.computeRelativeId(findId, Util.hashSocketAddress(n));

        while (!(findRelativeId > 0 && findRelativeId <= nextSuccessorRelativeId)) {

            // temporarily save current node
            InetSocketAddress predecessorNode = n;

            // if current node is local node, find my closest
            if (n.equals(this.localAddress)) {
                n = this.findClosestPrecedingFinger(findId);
            }

            // else current node is remote node, sent request to it for its closest
            else {
                InetSocketAddress result = Util.requestAddress(n, "CLOSEST_" + findId);

                // failed to get response, set n to most recently
                if (result == null) {
                    n = mostRecentlyAlive;
                    nextSuccessor = Util.requestAddress(n, "YOURSUCC");
                    if (nextSuccessor == null) {
                        Logger.log("It's not possible.");
                        return localAddress;
                    }
                    continue;
                }

                // if n's closest is itself, return n
                else if (result.equals(n))
                    return result;

                // else n's closest is other node "result"
                else {

                    mostRecentlyAlive = n;

                    // ask "result" for its successor
                    nextSuccessor = Util.requestAddress(result, "YOURSUCC");

                    // if we can get its response, then "result" must be our next n
                    if (nextSuccessor != null) {
                        n = result;
                    }

                    // else n sticks, ask n's successor
                    else {
                        nextSuccessor = Util.requestAddress(n, "YOURSUCC");
                    }
                }

                nextSuccessorRelativeId = Util.computeRelativeId(Util.hashSocketAddress(nextSuccessor), Util.hashSocketAddress(n));
                findRelativeId = Util.computeRelativeId(findId, Util.hashSocketAddress(n));
            }
            if (predecessorNode.equals(n))
                break;
        }
        return n;
    }

    /**
     * Return closest finger preceding node.
     */
    public InetSocketAddress findClosestPrecedingFinger(long findId) {
        long findRelativeId = Util.computeRelativeId(findId, localId);

        // traverse from last item in finger table
        for (int i = 32; i > 0; i--) {
            InetSocketAddress ithFinger = finger.get(i);
            if (ithFinger == null) {
                continue;
            }
            long ithFingerId = Util.hashSocketAddress(ithFinger);
            long ithFingerRelativeId = Util.computeRelativeId(ithFingerId, localId);

            // if its relative id is the closest, check if its alive
            if (ithFingerRelativeId > 0 && ithFingerRelativeId < findRelativeId) {
                String response = Util.sendRequest(ithFinger, "KEEP");

                //it is alive, return it
                if (response != null && response.equals("ALIVE")) {
                    return ithFinger;
                }

                // else, remove its existence from finger table
                else {
                    updateFingers(-2, ithFinger);
                }
            }
        }
        return localAddress;
    }

    /**
     * Update the finger table based on parameters.
     * Must be synchronized because all threads will access this method.
     */
    public synchronized void updateFingers(int i, InetSocketAddress value) {

        // valid index in [1, 32], just update the ith finger
        if (i > 0 && i <= 32) {
            updateIthFinger(i, value);
        }

        // caller wants to delete
        else if (i == -1) {
            deleteSuccessor();
        }

        // caller wants to delete a finger in table
        else if (i == -2) {
            deleteCertainFinger(value);
        }

        // caller wants to fill successor
        else if (i == -3) {
            fillSuccessor();
        }
    }

    /**
     * Update ith finger in finger table using new value.
     */
    private void updateIthFinger(int i, InetSocketAddress value) {
        finger.put(i, value);

        // if the updated one is successor, notify the new successor
        if (i == 1 && value != null && !value.equals(localAddress)) {
            notify(value);
        }
    }

    /**
     * Delete successor and all following fingers equal to successor
     */
    private void deleteSuccessor() {
        InetSocketAddress successor = getSuccessor();

        //nothing to delete, just return
        if (successor == null)
            return;

        // find the last existence of successor in the finger table
        int i = 32;
        for (i = 32; i > 0; i--) {
            InetSocketAddress ithFinger = finger.get(i);
            if (ithFinger != null && ithFinger.equals(successor))
                break;
        }

        // delete it, from the last existence to the first one
        for (int j = i; j >= 1; j--) {
            updateIthFinger(j, null);
        }

        // if predecessor is successor, delete it
        if (predecessor != null && predecessor.equals(successor))
            setPredecessor(null);

        // try to fill successor
        fillSuccessor();
        successor = getSuccessor();

        // if successor is still null or local node,
        // and the predecessor is another node, keep asking
        // it's predecessor until local node's new successor has been found
        if ((successor == null || successor.equals(successor)) && predecessor != null && !predecessor.equals(localAddress)) {
            InetSocketAddress p = predecessor;
            InetSocketAddress previousPredecessor = null;
            while (true) {
                previousPredecessor = Util.requestAddress(p, "YOURPRE");
                if (previousPredecessor == null)
                    break;

                // if p's predecessor is node is just deleted,
                // or itself (nothing found in p), or local address,
                // p is current node's new successor, break
                if (previousPredecessor.equals(p) || previousPredecessor.equals(localAddress) || previousPredecessor.equals(successor)) {
                    break;
                }

                // else, keep asking
                else {
                    p = previousPredecessor;
                }
            }

            // update successor
            updateIthFinger(1, p);
        }
    }

    /**
     * Delete a node from the finger table.
     */
    private void deleteCertainFinger(InetSocketAddress f) {
        for (int i = 32; i > 0; i--) {
            InetSocketAddress ithfinger = finger.get(i);
            if (ithfinger != null && ithfinger.equals(f))
                finger.put(i, null);
        }
    }

    /**
     * Try to fill successor with candidates in finger table or even predecessor
     */
    private void fillSuccessor() {
        InetSocketAddress successor = this.getSuccessor();
        if (successor == null || successor.equals(localAddress)) {
            for (int i = 2; i <= 32; i++) {
                InetSocketAddress ithfinger = finger.get(i);
                if (ithfinger != null && !ithfinger.equals(localAddress)) {
                    for (int j = i - 1; j >= 1; j--) {
                        updateIthFinger(j, ithfinger);
                    }
                    break;
                }
            }
        }
        successor = getSuccessor();
        if ((successor == null || successor.equals(localAddress)) && predecessor != null && !predecessor.equals(localAddress)) {
            updateIthFinger(1, predecessor);
        }
    }

    public void clearPredecessor() {
        setPredecessor(null);
    }

    public long getId() {
        return localId;
    }

    public InetSocketAddress getAddress() {
        return localAddress;
    }

    public InetSocketAddress getPredecessor() {
        return predecessor;
    }

    private synchronized void setPredecessor(InetSocketAddress pre) {
        predecessor = pre;
    }

    public InetSocketAddress getSuccessor() {
        if (finger != null && finger.size() > 0) {
            return finger.get(1);
        }
        return null;
    }

//    public void printNeighbors() {
//        Logger.log("\nListening on port: " + localAddress.getPort() + ".");
//        InetSocketAddress successor = finger.get(1);
//
//        // if it cannot find both predecessor and successor
//        if ((predecessor == null || predecessor.equals(localAddress)) && (successor == null || successor.equals(localAddress))) {
//            Logger.log("You are your predecessor and successor");
//        }
//
//        // else, it can find either predecessor or successor
//        else {
//            if (predecessor != null) {
//                Logger.log("Predecessor is node " + predecessor.getAddress().toString() + ", "
//                        + "port " + predecessor.getPort() + ".");
//            } else {
//                Logger.log("Predecessor is updating.");
//            }
//
//            if (successor != null) {
//                Logger.log("Successor is node " + successor.getAddress().toString() + ", "
//                        + "port " + successor.getPort() + ".");
//            } else {
//                Logger.log("Successor is updating.");
//            }
//        }
//    }

    public void printDataStructure() {
        Logger.log("\n--------------------------------------------------------------");
        Logger.log("\nLocal: " + localAddress.toString());

        if (predecessor != null)
        {
            Logger.log("\nPredecessor: " + predecessor.toString());
        }

        else
        {
            Logger.log("\nPredecessor: null");
        }
        
        Logger.log("\nFinger table: \n");

        for (int i = 1; i <= 5; i++) {
            long ithstart = Util.ithStart(Util.hashSocketAddress(localAddress), i);
            InetSocketAddress fingerId = finger.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append(i + "\t");
            
            if (fingerId != null)
                sb.append(fingerId.toString() + "\t" + Util.hashSocketAddress(fingerId));

            else
                sb.append("null");
            Logger.log(sb.toString());
        }
        Logger.log("\n--------------------------------------------------------------\n");
    }

    /**
     * Stop this node's all threads.
     */
    public void stopAllThreads() {
        if (listenerThread != null)
            listenerThread.toDie();
        if (fix_fingers != null)
            fix_fingers.toDie();
        if (stabilizeThread != null)
            stabilizeThread.toDie();
        if (ask_predecessor != null)
            ask_predecessor.toDie();
    }
}
