package edu.scu.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class ConfigHelper {

    private static HashMap<String, Integer> referenceMap;
    private static HashMap<Integer, String> ipMap;

    public static HashMap<String, Integer> getReferenceMap() {
        referenceMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("ref.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                referenceMap.put(line.split(",")[0], Integer.parseInt(line.split(",")[1]));
            }
        } catch (Exception e) {
            return null;
        }
        return referenceMap;
    }

    public static HashMap<Integer, String> getIpMap() {
        ipMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("ip.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                ipMap.put(Integer.parseInt(line.split(",")[0]), line.split(",")[1]);
            }
        } catch (Exception e) {
            return null;
        }
        return ipMap;
    }

}
