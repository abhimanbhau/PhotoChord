package edu.scu.thread;

import edu.scu.core.Node;
import edu.scu.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * CommunicatorThread thread that processes request accepted by listener and writes response to
 * socket.
 *
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 */
class CommunicatorThread implements Runnable {

    private final Socket talkSocket;
    private final Node local;

    public CommunicatorThread(Socket socket, Node local) {
        talkSocket = socket;
        this.local = local;
    }

    public void run() {
        InputStream input;
        OutputStream output;
        try {
            input = talkSocket.getInputStream();
            String request = Util.inputStreamToString(input);
            String response = processRequest(request);
            if (response != null) {
                output = talkSocket.getOutputStream();
                output.write(response.getBytes());
            }
            input.close();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Cannot talk.\nServer port: "
                            + local.getAddress().getPort()
                            + "; CommunicatorThread port: "
                            + talkSocket.getPort(),
                    e);
        }
    }

    private String processRequest(String request) {
        InetSocketAddress result;
        String ret = null;

        if (request == null) {
            return null;
        }

        if (request.startsWith("CLOSEST")) {
            long id = Long.parseLong(request.split("_")[1]);
            result = local.findClosestPrecedingFinger(id);
            String ip = result.getAddress().toString();
            int port = result.getPort();
            ret = "MYCLOSEST_" + ip + ":" + port;
        } else if (request.startsWith("YOURSUCC")) {
            result = local.getSuccessor();
            if (result != null) {
                String ip = result.getAddress().toString();
                int port = result.getPort();
                ret = "MYSUCC_" + ip + ":" + port;
            } else {
                ret = "NOTHING";
            }
        } else if (request.startsWith("YOURPRE")) {
            result = local.getPredecessor();
            if (result != null) {
                String ip = result.getAddress().toString();
                int port = result.getPort();
                ret = "MYPRE_" + ip + ":" + port;
            } else {
                ret = "NOTHING";
            }
        } else if (request.startsWith("FINDSUCC")) {
            long id = Long.parseLong(request.split("_")[1]);
            result = local.findSuccessor(id);
            String ip = result.getAddress().toString();
            int port = result.getPort();
            ret = "FOUNDSUCC_" + ip + ":" + port;
        } else if (request.startsWith("IAMPRE")) {
            InetSocketAddress newPredecessor = Util.createSocketAddress(request.split("_")[1]);
            local.notified(newPredecessor);
            ret = "NOTIFIED";
        } else if (request.startsWith("KEEP")) {
            ret = "ALIVE";
        }

        return ret;
    }
}
