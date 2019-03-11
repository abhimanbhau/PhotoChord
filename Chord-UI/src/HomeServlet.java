import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.Socket;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    PrintWriter out;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        out = response.getWriter();
        String node = request.getParameter("node");
        String tag = request.getParameter("tag");

        if (node.equals("null") || node.equals("null")) {
            return;
        }
        requestChord(node, tag, out);

    }

    private void handleImages(OutputStream out, String res) throws IOException {

    }

    private void requestChord(String IP, String tag, PrintWriter out) throws IOException {
        Socket client = new Socket(IP, 5678);
        BufferedWriter os = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        os.write(tag + "\r\n");
        os.flush();
        // String result = in.readLine();

        // byte[] res = new byte[1024 * 1024 * 8];

        String res = in.readLine();

        if (res.equals("IP")) {
            res = in.readLine();
            os.close();
            in.close();
            client.close();
            requestChord(res, tag, out);
        } else if (res.equals("IMAGES")) {
            while ((res = in.readLine()) != null) {
                System.out.println("Read file " + res);
                out.println("<img src=\"data:image/png;base64, " + res + "\" alt=\"Red dot\" />");
            }
        }
    }
}
