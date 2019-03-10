import java.net.InetSocketAddress;

/**
 * Ask predecessor thread that periodically asks for predecessor's keep-alive,
 * and delete predecessor if it's dead.
 *
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 */
public class PredecessorCheckThread extends Thread {

    private Node local;
    private boolean alive;

    public PredecessorCheckThread(Node _local) {
        local = _local;
        alive = true;
    }

    @Override
    public void run() {
        while (alive) {
            InetSocketAddress predecessor = local.getPredecessor();
            if (predecessor != null) {
                String response = Util.sendRequest(predecessor, "KEEP");
                if (response == null || !response.equals("ALIVE")) {
                    local.clearPredecessor();
                }

            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void toDie() {
        alive = false;
    }
}


