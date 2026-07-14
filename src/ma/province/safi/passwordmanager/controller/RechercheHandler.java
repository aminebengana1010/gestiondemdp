package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.service.RechercheService;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RechercheHandler implements HttpHandler {

    private final RechercheService rechercheService;
    private final SecurityInterceptor security;

    public RechercheHandler(RechercheService rechercheService, SecurityInterceptor security) {
        this.rechercheService = rechercheService;
        this.security = security;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (security.verifierAuthentification(exchange) == null) return;

            String query = exchange.getRequestURI().getQuery();
            String texte = "";
            String type = "";

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2) {
                        String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        switch (kv[0]) {
                            case "texte" -> texte = val;
                            case "type" -> type = val;
                        }
                    }
                }
            }

            Map<String, Object> resultats = rechercheService.rechercher(texte, type.isEmpty() ? null : type);
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : resultats.entrySet()) {
                if (!first) json.append(",");
                first = false;
                json.append("\"").append(entry.getKey()).append("\":").append(
                    entry.getValue().toString()
                );
            }
            json.append("}");
            ResponseUtil.json(exchange, 200, json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", "Erreur recherche"));
        }
    }
}
