package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.security.CookieUtil;
import ma.province.safi.passwordmanager.service.AuthService;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LoginHandler implements HttpHandler {

    private final AuthService authService;

    public LoginHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                ResponseUtil.json(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
                return;
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            String body = JsonUtil.lireCorps(reader);
            Map<String, String> data = JsonUtil.parseObject(body);

            String login = data.get("login");
            String motDePasse = data.get("motDePasse");

            if (login == null || motDePasse == null || login.isEmpty() || motDePasse.isEmpty()) {
                ResponseUtil.json(exchange, 400, "{\"erreur\":\"Login et mot de passe requis\"}");
                return;
            }

            Session session = authService.authentifier(login, motDePasse);

            CookieUtil.ajouterCookie(exchange, "SESSION_ID", session.getToken(), 1800);

            String json = JsonUtil.buildObject(
                JsonUtil.jsonString("succes", "true"),
                JsonUtil.jsonString("message", "Connexion réussie"),
                JsonUtil.jsonString("nomUtilisateur", session.getNomUtilisateur()),
                JsonUtil.jsonString("role", session.getRole())
            );
            ResponseUtil.json(exchange, 200, json);

        } catch (SecurityException e) {
            ResponseUtil.json(exchange, 401, JsonUtil.json("erreur", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", "Erreur interne"));
        }
    }
}
