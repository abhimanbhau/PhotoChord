import com.sun.corba.se.spi.activation.Server;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h3>Hello World!</h3>");

        Socket client = new Socket("172.21.102.189", 5678);
        System.out.println("new client");
        BufferedWriter os = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader( client.getInputStream()));
        System.out.println("Setup is and os");
        os.write("Games\r\n");
        os.flush();
        System.out.println("OS write bytes");
        String result = in.readLine();
        os.close();
        in.close();
        client.close();


        out.println("<h1>Accepted input from chord</h1>");
        out.println(result);
        out.close();
    }
}
