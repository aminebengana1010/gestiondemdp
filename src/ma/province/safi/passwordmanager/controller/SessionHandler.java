package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.IOException;

public class SessionHandler implements HttpHandler {

    private final SecurityInterceptor security;

    public SessionHandler(SecurityInterceptor security) {
        this.security = security;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Session session = security.verifierAuthentification(exchange);
            if (session == null) return;

            String json = JsonUtil.buildObject(
                JsonUtil.jsonString("succes", "true"),
                JsonUtil.jsonString("nomUtilisateur", session.getNomUtilisateur()),
                JsonUtil.jsonString("role", session.getRole())
            );
            ResponseUtil.json(exchange, 200, json);
        } catch (Exception e) {
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", "Erreur interne"));
        }
    }
}
