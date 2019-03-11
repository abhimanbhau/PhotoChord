import java.io.*;
import java.net.Socket;

public class ChordRequest {
    public static void main (String []args) throws IOException {
        Socket socket = new Socket("172.21.102.189", 5678);

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter( socket.getOutputStream()));

        out.write("Games\r\n");
        out.flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println(in.readLine());
        in.close();
        out.close();
        socket.close();
    }

}
