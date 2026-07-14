package ma.province.safi.passwordmanager.util;

import java.io.BufferedReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitaire JSON minimaliste (sans bibliothèque externe).
 * Gère les besoins de l'application : parsing d'objets simples et génération JSON.
 */
public class JsonUtil {

    private JsonUtil() {}

    // --- GÉNÉRATION ---

    public static String json(String key, String value) {
        return "{\"" + escape(key) + "\":\"" + escape(value) + "\"}";
    }

    public static String json(String key, int value) {
        return "{\"" + escape(key) + "\":" + value + "}";
    }

    public static String jsonString(String key, String value) {
        return "\"" + escape(key) + "\":\"" + escape(value) + "\"";
    }

    public static String jsonInt(String key, int value) {
        return "\"" + escape(key) + "\":" + value;
    }

    public static String jsonBool(String key, boolean value) {
        return "\"" + escape(key) + "\":" + value;
    }

    public static String jsonNull(String key) {
        return "\"" + escape(key) + "\":null";
    }

    public static String buildObject(String... paires) {
        return "{" + String.join(",", paires) + "}";
    }

    public static String buildArray(List<String> elements) {
        return "[" + String.join(",", elements) + "]";
    }

    // --- PARSING ---

    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return map;
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return map;

        // Pattern pour extraire "key":"value" ou "key":number ou "key":true/false/null
        Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|true|false|null|\\d+)");
        Matcher m = p.matcher(json);
        while (m.find()) {
            String key = m.group(1);
            String val = m.group(2);
            if (val.startsWith("\"") && val.endsWith("\"")) {
                val = val.substring(1, val.length() - 1);
            }
            map.put(key, val);
        }
        return map;
    }

    // --- UTILITAIRE ---

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Lit le corps d'une requête HTTP en String.
     */
    public static String lireCorps(BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String ligne;
        while ((ligne = reader.readLine()) != null) {
            sb.append(ligne);
        }
        return sb.toString();
    }
}
