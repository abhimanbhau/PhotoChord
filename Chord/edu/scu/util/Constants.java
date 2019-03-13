package edu.scu.util;

import edu.scu.config.ConfigHelper;

import java.util.HashMap;

/**
 * Constants class that holds all the constants.
 *
 * @author Raghav Bhandari
 * @author Krishna Kandhani
 * @author Abhiman Kolte
 * @author Dhruv Mevada
 */
public class Constants {
    public static final int _mpiPort = 5678;
    public static final String _photoStoragePath = System.getProperty("user.home");
    ;

    public static HashMap<String, Integer> _refMap;
    public static HashMap<Integer, String> _nodeIpMap;

//
//    public static final HashMap<String, Integer> _refMap =
//            new HashMap<String, Integer>() {
//                {
//                    put("Games", 6);
//                    put("Movies", 2);
//                    put("Cities", 3);
//                    put("Nature", 4);
//                    put("Car", 5);
//                }
//            };
//
//    public static final HashMap<Integer, String> _nodeIpMap = new HashMap<Integer, String>() {
//        {
//            put(25, "172.21.102.189");
//            put(2, "121.2.11.1");
//            put(3, "121.2.4.6");
//            put(4, "121.1.3.5");
//            put(5, "121.1.7.8");
//        }
//    };

    public static boolean initConfig() {
        try {
            _refMap = ConfigHelper.getReferenceMap();
            _nodeIpMap = ConfigHelper.getIpMap();
            if (_refMap == null || _nodeIpMap == null) {
                System.exit(-1);
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
