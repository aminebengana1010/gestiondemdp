package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.security.CookieUtil;
import ma.province.safi.passwordmanager.service.AuthService;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.IOException;

public class LogoutHandler implements HttpHandler {

    private final AuthService authService;

    public LogoutHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String token = CookieUtil.extraireCookie(exchange, "SESSION_ID");
            if (token != null) {
                authService.deconnecter(token);
            }
            CookieUtil.supprimerCookie(exchange, "SESSION_ID");
            ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Déconnexion réussie\"}");
        } catch (Exception e) {
            ResponseUtil.json(exchange, 500, "{\"erreur\":\"Erreur lors de la déconnexion\"}");
        }
    }
}
