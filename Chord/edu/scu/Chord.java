package edu.scu;

import edu.scu.util.Logger;
import edu.scu.util.Util;

import java.net.*;
import java.util.Scanner;

/**
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 *
 * Driver class for Chord application.
 */
public class Chord {
    private static String port = "";
    private static String ipAddressPort = "";
    private static Node node;
    private static InetSocketAddress contactNode;
    private Util util = new Util();

//    private static String getOwnIp() {
//        try {
//            URL whatismyip = new URL("http://checkip.amazonaws.com");
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    whatismyip.openStream()));
//
//            String ip = in.readLine(); //you get the IP as a String
//            return InetAddress.getByName(ip).getHostAddress();
//        } catch (UnknownHostException error) {
//            error.printStackTrace();
//            return null;
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    private static String getOwnIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException error) {
            error.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        Chord chord = new Chord();
        int value = chord.parseArguments(args);

        if (value == 1) {
            //todo
            //list chord ring
        }

        //create chord ring
        else if (value == 2) {
            port = args[1];
            String ipPort = getOwnIp() + ":" + port;

            node = new Node(Util.createSocketAddress(ipPort));
            contactNode = node.getAddress();
        }

        //join existing chord ring
        else if (value == 3) {
            port = args[1];
            ipAddressPort = args[2];

            node = new Node(Util.createSocketAddress(getOwnIp() + ":" + port));
            contactNode = Util.createSocketAddress(ipAddressPort);

            if (contactNode == null) {
                Logger.log("Address of contact node not resolved, cannot join ring, exiting.");
                System.exit(0);
            }
        } else {
            Logger.log("Could not parse arguments, exiting");
            System.exit(0);
        }

        boolean joinedSuccessfully = node.join(contactNode);

        if (!joinedSuccessfully) {
            Logger.log("Could not connect with node, exiting.");
            System.exit(0);
        }

        Logger.log("Joining the chord ring, with local ip: " + getOwnIp());

        Scanner in = new Scanner(System.in);
        while (true) {
            Logger.log("Select from the options below: ");
            Logger.log("- Data");
            Logger.log("- Exit");

            String userCommand = in.next();

            if (userCommand.equalsIgnoreCase("data")) {
                node.printDataStructure();
                node.printNeighbors();
            } else if (userCommand.equalsIgnoreCase("exit")) {
                Logger.log("");
                node.stopAllThreads();
                System.exit(0);
            }
        }
    }

    private int parseArguments(String[] args) {
        int returnValue = 0;

        //list nodes in chord ring
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                returnValue = 1;
            } else {
                printHelp();
            }
        }

        //create a chord ring
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                returnValue = 2;
            } else {
                printHelp();
            }
        }

        //join an existing chord ring
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("join")) {
                returnValue = 3;
            } else {
                printHelp();
            }
        } else {
            printHelp();
        }

        return returnValue;
    }

    private void printHelp() {
        Logger.log("Cannot determine command, will not start\n");
        Logger.log("Usage: java Chord <list> <create> <join>\n");
        Logger.log("Please specify a command from one of the formats below.");
        Logger.log("    1) java list");
        Logger.log("    2) java create <port>");
        Logger.log("    3) java join <port> <ip:port>");
    }
}