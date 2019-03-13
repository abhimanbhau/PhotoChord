package edu.scu.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class PersistentLogger {
    static PersistentLogger _logger;
    static FileWriter _log;

    static {
        _logger = new PersistentLogger();
    }

    int random = new Random().nextInt(1000);

    {
        try {
            _log = new FileWriter("log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static void log(String s) {
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
