package edu.scu.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class PersistentLogger {
    static PersistentLogger _logger;

    static {
            _logger = new PersistentLogger();
    }

    static FileWriter _log;

    {
        try {
            _log = new FileWriter("log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int random = new Random().nextInt(1000);

    private PersistentLogger() {
        PersistentLogger.log("Logger started: log" + random + ".txt");
    }

    public static PersistentLogger getInstance() {
        return _logger;
    }

    public static void logE(String s) {
        try {
            _log.write("Error: " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String s)  {
        try {
            _log.write("PhotoChord Debug: " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        try {
            _log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
