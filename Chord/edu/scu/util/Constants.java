package edu.scu.util;

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
    public static HashMap<String, Integer> refMap = new HashMap<String, Integer>() {{
        put("Games", 1);
        put("Movies", 2);
        put("Cities", 3);
        put("Nature", 4);
        put("Car", 5);
    }};

}
