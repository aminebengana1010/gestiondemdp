package ma.province.safi.passwordmanager.security;

import com.sun.net.httpserver.HttpExchange;

import java.util.List;

public class CookieUtil {

    private CookieUtil() {}

    public static String extraireCookie(HttpExchange exchange, String nom) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies == null) return null;

        for (String cookie : cookies) {
            String[] paires = cookie.split(";\\s*");
            for (String paire : paires) {
                String[] cleValeur = paire.split("=", 2);
                if (cleValeur.length == 2 && cleValeur[0].equals(nom)) {
                    return cleValeur[1];
                }
            }
        }
        return null;
    }

    public static void ajouterCookie(HttpExchange exchange, String nom, String valeur, int maxAgeSecondes) {
        exchange.getResponseHeaders().add("Set-Cookie",
            nom + "=" + valeur +
            "; HttpOnly" +
            "; Path=/" +
            "; Max-Age=" + maxAgeSecondes +
            "; SameSite=Lax");
    }

    public static void supprimerCookie(HttpExchange exchange, String nom) {
        ajouterCookie(exchange, nom, "", 0);
    }
}
