import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MessagePassingThread extends Thread{
    ServerSocket serverSocket;
    Socket socket;
    boolean _run = true;

    public MessagePassingThread() {
        try {
            serverSocket = new ServerSocket(Constants._mpiPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(_run) {
            try {
                socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String ipOfClient = reader.readLine();
                InetAddress ip = InetAddress.getByName(ipOfClient);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeByte(1);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void finish() {
        _run = false;
    }
}
