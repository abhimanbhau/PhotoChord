import java.util.HashMap;

class Constants {
    public static final int _mpiPort = 5678;
    public static HashMap<String, Integer> _refMap = new HashMap<String, Integer>() {{
        put("Games", 1);
        put("Movies", 2);
        put("Cities", 3);
        put("Nature", 4);
        put("Car", 5);
    }};
}
