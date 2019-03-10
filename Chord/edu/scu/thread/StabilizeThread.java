package edu.scu.thread;

import java.net.InetSocketAddress;

import edu.scu.Node;
import edu.scu.util.Util;

/**
 * StabilizeThread thread that periodically asks successor for its predecessor
 * and determine if current node should update or delete its successor.
 *
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 */

public class StabilizeThread extends Thread {

    private Node local;
    private boolean alive;

    public StabilizeThread(Node local) {
        this.local = local;
        alive = true;
    }

    @Override
    public void run() {
        while (alive) {
            InetSocketAddress successor = local.getSuccessor();
            if (successor == null || successor.equals(local.getAddress())) {
                local.updateFingers(-3, null); //fill
            }
            successor = local.getSuccessor();
            if (successor != null && !successor.equals(local.getAddress())) {

                // try to get my successor's predecessor
                InetSocketAddress x = Util.requestAddress(successor, "YOURPRE");

                // if bad connection with successor, delete successor
                if (x == null) {
                    local.updateFingers(-1, null);
                }

                // else if successor's predecessor is not itself
                else if (!x.equals(successor)) {
                    long localId = Util.hashSocketAddress(local.getAddress());
                    long successorRelativeId = Util.computeRelativeId(Util.hashSocketAddress(successor), localId);
                    long xRelativeId = Util.computeRelativeId(Util.hashSocketAddress(x), localId);
                    if (xRelativeId > 0 && xRelativeId < successorRelativeId) {
                        local.updateFingers(1, x);
                    }
                }

                // successor's predecessor is successor itself, then notify successor
                else {
                    local.notify(successor);
                }
            }

            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void toDie() {
        alive = false;
    }
}
