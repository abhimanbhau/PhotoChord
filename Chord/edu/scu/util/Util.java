package edu.scu.util;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * A Util class which provide methods for hashing, sending requests, and more.
 *
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 */
public class Util {

    private static HashMap<Integer, Long> powerOfTwo = null;

    public Util() {
        powerOfTwo = new HashMap<>();
        long base = 1;
        for (int i = 0; i <= 32; i++) {
            powerOfTwo.put(i, base);
            base *= 2;
        }
    }

    /**
     * Compute a socket address' 32 bit identifier
     */
    public static long hashSocketAddress(InetSocketAddress address) {
        //        return hashHashCode(address.hashCode());
        //        Logger.log(address.getHostString());
        String lastTriplet =
                address.getHostString().substring(address.getHostString().lastIndexOf(".") + 1);
        //        Logger.log(lastTriplet);
        return Integer.parseInt(lastTriplet) % 32;
    }

    /**
     * Compute a string's 32 bit identifier
     */
    public static long hashString(String str) {
        return hashHashCode(str.hashCode());
    }

    /**
     * Compute a 32 bit integer's identifier
     */
    private static long hashHashCode(int i) {

        // 32 bit regular hash code -> byte[4]
        byte[] hashBytes = new byte[4];
        hashBytes[0] = (byte) (i >> 24);
        hashBytes[1] = (byte) (i >> 16);
        hashBytes[2] = (byte) (i >> 8);
        hashBytes[3] = (byte) (i);

        // try to create SHA1 digest
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            Logger.log("SHA-1 not found");
            e.printStackTrace();
        }

        // successfully created SHA1 digest
        // try to convert byte[4]
        if (md != null) {
            md.reset();
            md.update(hashBytes);
            byte[] result = md.digest();

            byte[] compressed = new byte[4];
            for (int j = 0; j < 4; j++) {
                byte temp = result[j];
                for (int k = 1; k < 5; k++) {
                    temp = (byte) (temp ^ result[j + k]);
                }
                compressed[j] = temp;
            }

            long ret =
                    (compressed[0] & 0xFF) << 24
                            | (compressed[1] & 0xFF) << 16
                            | (compressed[2] & 0xFF) << 8
                            | (compressed[3] & 0xFF);
            ret = ret & 0xFFFFFFFFL;
            return ret;
        }
        return 0;
    }

    /**
     * Normalization, compute universal id's value relative to local id, considering local node with
     * an id of 0.
     */
    public static long computeRelativeId(long universal, long local) {
        long ret = universal - local;
        if (ret < 0) {
            ret += powerOfTwo.get(32);
        }
        return ret;
    }

    //    /**
    //     * Compute a socket address' SHA-1 hash in hex
    //     * and its approximate position in string
    //     */
    //    public static String hexIdAndPosition(InetSocketAddress address) {
    //        long hash = hashSocketAddress(address);
    //        return (longTo8DigitHex(hash) + " (" + hash * 100 / Util.getPowerOfTwo(32) + "%)");
    //    }
    //
    //    /**
    //     * @return
    //     */
    //    public static String longTo8DigitHex(long l) {
    //        String hex = Long.toHexString(l);
    //        int lack = 8 - hex.length();
    //        StringBuilder sb = new StringBuilder();
    //        for (int i = lack; i > 0; i--) {
    //            sb.append("0");
    //        }
    //        sb.append(hex);
    //        return sb.toString();
    //    }

    /**
     * Return a node's finger[i].start, universal
     */
    public static long ithStart(long nodeId, int i) {
        return (nodeId + powerOfTwo.get(i - 1)) % powerOfTwo.get(32);
    }

    /**
     * Get power of 2
     */
    public static long getPowerOfTwo(int k) {
        return powerOfTwo.get(k);
    }

    /**
     * Generate requested address by sending request to server
     */
    public static InetSocketAddress requestAddress(InetSocketAddress server, String req) {

        // invalid input, return null
        if (server == null || req == null) {
            return null;
        }

        // send request to server
        String response = sendRequest(server, req);

        if (response == null) {
            return null;
        } else if (response.startsWith("NOTHING")) return server;

            // server found something, use response to create
        else {
            return Util.createSocketAddress(response.split("_")[1]);
        }
    }

    /**
     * Sends request to server and reads response.
     */
    public static String sendRequest(InetSocketAddress server, String req) {

        if (server == null || req == null) return null;

        Socket talkSocket = null;

        // try to open talkSocket, output request to this socket
        try {
            talkSocket = new Socket(server.getAddress(), server.getPort());
            PrintStream output = new PrintStream(talkSocket.getOutputStream());
            output.println(req);
        } catch (IOException e) {
            // Logger.log("\nCannot send request to "+server.toString()+"\nRequest is: "+req+"\n");
            return null;
        }

        try {
            Thread.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // get input stream, try to read something from it
        InputStream input = null;
        try {
            input = talkSocket.getInputStream();
        } catch (IOException e) {
            Logger.log(
                    "Cannot get input stream from " + server.toString() + "\nRequest is: " + req + "\n");
        }

        String response = Util.inputStreamToString(input);

        try {
            talkSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot close socket", e);
        }

        return response;
    }

    /**
     * Create InetSocketAddress using ip address and port number
     */
    public static InetSocketAddress createSocketAddress(String addr) {

        if (addr == null) {
            return null;
        }

        // split input into ip string and port string
        String[] parts = addr.split(":");

        // two parts to the split string
        if (parts.length >= 2) {

            String ip = parts[0];
            if (ip.startsWith("/")) {
                ip = ip.substring(1);
            }

            // parse ip address
            InetAddress mIp = null;
            try {
                mIp = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                Logger.log("Cannot create ip address: " + ip);
                return null;
            }

            // parse port number
            int port = Integer.parseInt(parts[1]);

            // combine ip address and port in socket address
            return new InetSocketAddress(mIp, port);
        }

        // cannot split string
        else {
            return null;
        }
    }

    /**
     * Read one line from input stream.
     */
    public static String inputStreamToString(InputStream in) {

        if (in == null) {
            return null;
        }

        // try to read line from input stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            Logger.log("Cannot read line from input stream.");
            return null;
        }

        return line;
    }
}
