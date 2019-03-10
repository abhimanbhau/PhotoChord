package edu.scu.thread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import edu.scu.core.Node;

/**
 * ListenerThread thread that keeps listening to a port and asks talker thread to process
 * when a request is accepted.
 *
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 */

public class ListenerThread extends Thread {

    private Node local;
    private ServerSocket serverSocket;
    private boolean alive;

    public ListenerThread(Node n) {
        local = n;
        alive = true;
        InetSocketAddress localAddress = local.getAddress();
        int port = localAddress.getPort();

        //open server/listener socket
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException("\nCannot open listener port " + port + ", exiting.\n", e);
        }
    }

    @Override
    public void run() {
        while (alive) {
            Socket talkSocket = null;
            try {
                talkSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(
                        "Cannot accept connection", e);
            }

            //new communicator thread
            new Thread(new CommunicatorThread(talkSocket, local)).start();
        }
    }

    public void toDie() {
        alive = false;
    }
}
