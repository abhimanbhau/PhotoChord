package edu.scu.thread;

import edu.scu.core.Node;
import edu.scu.util.Constants;
import edu.scu.util.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;

public class MessagePassingThread extends Thread {
    private Node node;
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
        DataOutputStream out = null;
        while (_run) {
            try {
                socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Logger.log("Accepted curl req");
                String tag;
                tag = reader.readLine();

                out = new DataOutputStream(socket.getOutputStream());

                Logger.log("accp tag: " + tag);
                if (tag.equals("null")) return;
                int tagNode;
                try {
                    tagNode = Constants._refMap.get(tag);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (tagNode == node.getId()) {
                    // Send images
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    out.writeBytes("IMAGES\r\n");

                    for (File file : new File(Constants._photoStoragePath).listFiles()) {
                        if (!file.getName().contains("jpg"))
                            continue;
                        Logger.log("Sending files: " + file.getName());
                        String encoded = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
                        out.writeBytes(encoded + "\r\n");
                    }


                } else {
                    // Send back the IP of correct node

                    Logger.log("Not valid node send back correct IP");
                    out.writeBytes("IP\r\n");
                    out.writeBytes(Constants._nodeIpMap.get(tagNode) + "\r\n");
                }

                out.flush();
                out.close();
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
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
