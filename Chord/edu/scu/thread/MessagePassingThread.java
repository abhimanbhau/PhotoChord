package edu.scu.thread;

import edu.scu.core.Node;
import edu.scu.util.Constants;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MessagePassingThread extends Thread {
    Node node;
    private ServerSocket serverSocket;
    private Socket socket;
    private boolean _run = true;

    public MessagePassingThread(Node node) {
        try {
            serverSocket = new ServerSocket(Constants._mpiPort);
            this.node = node;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (_run) {
            try {
                socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                String tag = reader.readLine();
                int tagNode = Constants.refMap.get(tag);
                if (tagNode == node.getId()) {
                    // Send images
                } else {
                    // Send back the IP of correct node
                    out.writeBytes(Constants._nodeIpMap.get(tagNode));
                }
                out.flush();
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void finish() {
        _run = false;
        try {
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
