import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetSocketAddress;
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

    private void requestChord(String IP, String tag, PrintWriter out) throws IOException {
        Socket client = new Socket();

        long time = System.currentTimeMillis();

        try {
            client.connect(new InetSocketAddress(IP, 5678), 2000);
        }
        catch (Exception e) {
            ErrorHandler.ShowSocketError(out, e.getMessage() + " IP: " + IP);
            return;
        }
        BufferedWriter os = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        os.write(tag + "\r\n");
        os.flush();
        // String result = in.readLine();

        // byte[] res = new byte[1024 * 1024 * 8];

        String res = in.readLine();
        out.println("<style>");

        out.println("body {\n" +
                "    width: 100%;\n" +
                "    height: 100%;\n" +
                "    margin: 0px;\n" +
                "    padding: 0px;\n" +
                "}\n" +
                "\n" +
                ".picture-grid {\n" +
                "    display: grid;\n" +
                "    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));\n" +
                "    justify-items: center;\n" +
                "    grid-gap 5px;\n" +
                "    grid-row-gap: 0px;\n" +
                "}\n" +
                "\n" +
                ".grid-box img {\n" +
                "    width: 100%;\n" +
                "}");

        out.println("</style>");
        out.println("<div class=\"picture-grid\">");

        if (res.equals("IP")) {
            res = in.readLine();
            os.close();
            in.close();
            client.close();
            requestChord(res, tag, out);
        } else if (res.equals("IMAGES")) {
            while ((res = in.readLine()) != null) {
                System.out.println("Read file " + res);


                out.println("<div class=\"grid-box\"> <img src=\"data:image/png;base64, " + res + "\" alt=\"Red dot\" > </img> </div>");
            }
            out.println(" </div>");

            out.println("<br /><br /> <h3><b>Total time: " + (float)(System.currentTimeMillis() - time)/1000 +"s </b></h3>");
        }
    }


}
