import java.io.PrintWriter;

public class ErrorHandler {

    public static void ShowSocketError(PrintWriter out, String errorMessage) {
        out.println("<h1>Can't connect to IP</h1> <br />");
        out.println(errorMessage);
    }
}
