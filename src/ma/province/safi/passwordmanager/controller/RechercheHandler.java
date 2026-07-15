package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.model.*;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.service.RechercheService;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
                json.append("\"").append(entry.getKey()).append("\":");
                Object val = entry.getValue();
                if (val instanceof List) {
                    json.append(listToJson((List<?>) val));
                } else {
                    json.append(val.toString());
                }
            }
            json.append("}");
            ResponseUtil.json(exchange, 200, json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", e.getMessage() != null ? e.getMessage() : "Erreur recherche"));
        }
    }

    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            first = false;
            if (item instanceof Serveur s) {
                sb.append("{")
                  .append(JsonUtil.jsonInt("id", s.getId())).append(",")
                  .append(JsonUtil.jsonString("nom", s.getNom())).append(",")
                  .append(JsonUtil.jsonString("adresseIP", s.getAdresseIP() != null ? s.getAdresseIP() : "")).append(",")
                  .append(JsonUtil.jsonString("login", s.getLogin())).append(",")
                  .append(JsonUtil.jsonString("motDePasse", s.getMotDePasseClair() != null ? s.getMotDePasseClair() : ""))
                  .append("}");
            } else if (item instanceof SwitchReseau sw) {
                sb.append("{")
                  .append(JsonUtil.jsonInt("id", sw.getId())).append(",")
                  .append(JsonUtil.jsonString("nom", sw.getNom())).append(",")
                  .append(JsonUtil.jsonString("adresseMac", sw.getAdresseMAC() != null ? sw.getAdresseMAC() : "")).append(",")
                  .append(JsonUtil.jsonString("emplacement", sw.getEmplacement() != null ? sw.getEmplacement() : "")).append(",")
                  .append(JsonUtil.jsonString("login", sw.getLogin())).append(",")
                  .append(JsonUtil.jsonString("motDePasse", sw.getMotDePasseClair() != null ? sw.getMotDePasseClair() : ""))
                  .append("}");
            } else if (item instanceof SystemeInterne si) {
                sb.append("{")
                  .append(JsonUtil.jsonInt("id", si.getId())).append(",")
                  .append(JsonUtil.jsonString("nom", si.getNom())).append(",")
                  .append(JsonUtil.jsonString("url", si.getUrl() != null ? si.getUrl() : "")).append(",")
                  .append(JsonUtil.jsonString("login", si.getLogin())).append(",")
                  .append(JsonUtil.jsonString("motDePasse", si.getMotDePasseClair() != null ? si.getMotDePasseClair() : ""))
                  .append("}");
            } else if (item instanceof SystemeExterne se) {
                sb.append("{")
                  .append(JsonUtil.jsonInt("id", se.getId())).append(",")
                  .append(JsonUtil.jsonString("nom", se.getNom())).append(",")
                  .append(JsonUtil.jsonString("url", se.getUrl() != null ? se.getUrl() : "")).append(",")
                  .append(JsonUtil.jsonString("login", se.getLogin())).append(",")
                  .append(JsonUtil.jsonString("motDePasse", se.getMotDePasseClair() != null ? se.getMotDePasseClair() : ""))
                  .append("}");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
