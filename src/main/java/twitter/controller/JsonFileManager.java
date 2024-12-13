package twitter.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Map;

@Getter
@Setter
public class JsonFileManager {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T fromJson(String file, boolean isPrint, Type type) {
        T value = null;
        try {
            if (isPrint) {
                System.out.println("Finding '" + file + "'...");
            }
            JsonReader reader = new JsonReader(new FileReader(file));
            value = gson.fromJson(reader, type);
        }
        catch (Exception e) {
            System.out.println("Accessing '" + file + "' error.");
            e.printStackTrace();
            return value;
        }
        return value;
    }

    public static <T> Map<String, T> fromJsonToMap(String file, boolean isPrint) {
        Map<String, T> value = null;
        try {
            if (isPrint) {
                System.out.println("Finding '" + file + "'...");
            }
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            JsonReader reader = new JsonReader(new FileReader(file));
            value = gson.fromJson(reader, mapType);
        }
        catch (Exception e) {
            System.out.println("Accessing '" + file + "' error.");
            return value;
        }
        return value;
    }

    public static String toJsonString(String file, Object value) {
        return gson.toJson(value);
    }

    public static void toJson(String file, Object value, boolean isPrint) {
        try {
            PrintWriter out = new PrintWriter(file);
            if (isPrint) {
                System.out.println("Importing to '" + file + "'...");
            }
            out.print(gson.toJson(value));
            out.close();
        }
        catch (Exception e) {
            System.err.println("Accessing '" + file + "' error!");
        }
    }

    public static<T> void toJsonFromMap(String file, Map<String, T> value, boolean isPrint) {
        try {
            PrintWriter out = new PrintWriter(file);
            if (isPrint) {
                System.out.println("Importing to '" + file + "'...");
            }
            out.print(gson.toJson(value));
            out.close();
        }
        catch (Exception e) {
            System.err.println("Accessing '" + file + "' error!");
        }
    }
}
