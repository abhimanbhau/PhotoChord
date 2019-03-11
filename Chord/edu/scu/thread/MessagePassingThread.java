package edu.scu.thread;

import edu.scu.core.Node;
import edu.scu.util.Constants;
import edu.scu.util.Logger;
import sun.rmi.runtime.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        DataOutputStream out = null;
        while (_run) {
            try {
                socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader( new InputStreamReader(socket.getInputStream()));

                Logger.log("Accepted curl req");
                String tag;
                tag = reader.readLine();

                out = new DataOutputStream(socket.getOutputStream());

                Logger.log("accp tag: " + tag);

                int tagNode = Constants.refMap.get(tag);
                if (tagNode == node.getId()) {
                    // Send images

                    ZipOutputStream zout = new ZipOutputStream(out);

                    // Recurse through files
                    for(File file : new File(Constants._photoStoragePath).listFiles()) {
                        byte[] fileData = Files.readAllBytes(file.toPath());
                        zout.putNextEntry(new ZipEntry(file.getName()));
                        zout.write(fileData);
                        zout.closeEntry();
                    }
                    zout.close();

                } else {
                    // Send back the IP of correct node

                    Logger.log("Not valid node send back correct IP");
                    out.writeBytes(Constants._nodeIpMap.get(tagNode));
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
