package edu.scu.thread;

import edu.scu.core.Node;
import edu.scu.util.Util;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * UpdateFingers thread that periodically accesses a random entry in finger table and fixes it.
 *
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 */
public class UpdateFingersThread extends Thread {

    private final Random random;
    private final Node local;
    private boolean alive;
    private int lastFinger = 0;

    public UpdateFingersThread(Node node) {
        local = node;
        alive = true;
        random = new Random();
    }

    @Override
    public void run() {
        while (alive) {
            int i = random.nextInt(31) + 2;
            while (i == lastFinger) {
                i = random.nextInt(31) + 2;
            }
            lastFinger = i;
            InetSocketAddress ithfinger = local.findSuccessor(Util.ithStart(local.getId(), i));
            local.updateFingers(i, ithfinger);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void toDie() {
        alive = false;
    }
}
