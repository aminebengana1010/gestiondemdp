package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.dao.DivisionDAO;
import ma.province.safi.passwordmanager.model.DivisionExterne;
import ma.province.safi.passwordmanager.model.DivisionInterne;
import ma.province.safi.passwordmanager.model.TypeDivisionExterne;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DivisionHandler implements HttpHandler {

    private final DivisionDAO divisionDAO;
    private final SecurityInterceptor security;

    public DivisionHandler(DivisionDAO divisionDAO, SecurityInterceptor security) {
        this.divisionDAO = divisionDAO;
        this.security = security;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (security.verifierAuthentification(exchange) == null) return;

            if ("GET".equals(method)) {
                if (path.endsWith("/internes")) {
                    List<DivisionInterne> list = divisionDAO.listerInternes();
                    ResponseUtil.json(exchange, 200, divisionsInternesToJson(list));
                } else {
                    List<DivisionExterne> list = divisionDAO.listerExternes();
                    ResponseUtil.json(exchange, 200, divisionsExternesToJson(list));
                }
            } else if ("POST".equals(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
                Map<String, String> params = JsonUtil.parseObject(body);

                if (path.endsWith("/internes")) {
                    String nom = params.get("nom");
                    if (nom == null || nom.isBlank()) {
                        ResponseUtil.json(exchange, 400, "{\"erreur\":\"Le nom est requis\"}");
                        return;
                    }
                    divisionDAO.ajouterInterne(nom);
                    ResponseUtil.json(exchange, 201, "{\"succes\":true,\"message\":\"Division interne ajoutée\"}");
                } else {
                    String nom = params.get("nom");
                    String type = params.get("type");
                    if (nom == null || nom.isBlank() || type == null || type.isBlank()) {
                        ResponseUtil.json(exchange, 400, "{\"erreur\":\"Le nom et le type (AAL ou Caïdat) sont requis\"}");
                        return;
                    }
                    try {
                        TypeDivisionExterne.fromString(type);
                    } catch (IllegalArgumentException e) {
                        ResponseUtil.json(exchange, 400, JsonUtil.json("erreur", e.getMessage()));
                        return;
                    }
                    divisionDAO.ajouterExterne(nom, type);
                    ResponseUtil.json(exchange, 201, "{\"succes\":true,\"message\":\"Division externe ajoutée\"}");
                }
            } else if ("PUT".equals(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
                Map<String, String> params = JsonUtil.parseObject(body);
                int id = extraireId(path);

                if (id < 0) {
                    ResponseUtil.json(exchange, 400, "{\"erreur\":\"ID invalide\"}");
                    return;
                }

                if (path.contains("/internes")) {
                    String nom = params.get("nom");
                    if (nom == null || nom.isBlank()) {
                        ResponseUtil.json(exchange, 400, "{\"erreur\":\"Le nom est requis\"}");
                        return;
                    }
                    divisionDAO.modifierInterne(id, nom);
                    ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Division interne modifiée\"}");
                } else {
                    String nom = params.get("nom");
                    String type = params.get("type");
                    if (nom == null || nom.isBlank() || type == null || type.isBlank()) {
                        ResponseUtil.json(exchange, 400, "{\"erreur\":\"Le nom et le type (AAL ou Caïdat) sont requis\"}");
                        return;
                    }
                    try {
                        TypeDivisionExterne.fromString(type);
                    } catch (IllegalArgumentException e) {
                        ResponseUtil.json(exchange, 400, JsonUtil.json("erreur", e.getMessage()));
                        return;
                    }
                    divisionDAO.modifierExterne(id, nom, type);
                    ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Division externe modifiée\"}");
                }
            } else if ("DELETE".equals(method)) {
                int id = extraireId(path);
                if (id < 0) {
                    ResponseUtil.json(exchange, 400, "{\"erreur\":\"ID invalide\"}");
                    return;
                }
                if (path.contains("/internes")) {
                    divisionDAO.supprimerInterne(id);
                    ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Division interne supprimée\"}");
                } else {
                    divisionDAO.supprimerExterne(id);
                    ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Division externe supprimée\"}");
                }
            } else {
                ResponseUtil.json(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", "Erreur interne"));
        }
    }

    private int extraireId(String path) {
        String[] parts = path.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                return Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) { }
        }
        return -1;
    }

    private String divisionsInternesToJson(List<DivisionInterne> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (DivisionInterne d : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append(JsonUtil.jsonInt("id", d.getIdDivisionInterne())).append(",")
              .append(JsonUtil.jsonString("nom", d.getNomDivision()))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String divisionsExternesToJson(List<DivisionExterne> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (DivisionExterne d : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append(JsonUtil.jsonInt("id", d.getIdDivisionExterne())).append(",")
              .append(JsonUtil.jsonString("nom", d.getNomDivision())).append(",")
              .append(JsonUtil.jsonString("type", d.getType() != null ? d.getType().toString() : ""))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
