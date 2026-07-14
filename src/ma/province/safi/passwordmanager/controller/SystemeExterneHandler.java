package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.model.SystemeExterne;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.service.ExportService;
import ma.province.safi.passwordmanager.service.SystemeExterneService;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class SystemeExterneHandler implements HttpHandler {

    private final SystemeExterneService service;
    private final SecurityInterceptor security;
    private final ExportService exportService;

    public SystemeExterneHandler(SystemeExterneService service, SecurityInterceptor security, ExportService exportService) {
        this.service = service;
        this.security = security;
        this.exportService = exportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String methode = exchange.getRequestMethod();

            if ("GET".equals(methode) && path.endsWith("/export")) {
                Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
                if (session == null) return;
                String format = extraireFormat(exchange);
                if ("csv".equals(format)) {
                    String csv = exportService.exporterSystemesExternesCSV(session);
                    ResponseUtil.csv(exchange, csv, "systemes-externes.csv");
                } else {
                    String html = exportService.exporterSystemesExternesHTML(session);
                    ResponseUtil.html(exchange, 200, html);
                }
                return;
            }

            if ("GET".equals(methode) && path.contains("/consulter")) {
                Session session = security.verifierRoleAdministrateur(exchange);
                if (session == null) return;
                String secret = service.consulterSecret(extraireId(path), session);
                ResponseUtil.json(exchange, 200, JsonUtil.buildObject(
                    JsonUtil.jsonString("succes", "true"),
                    JsonUtil.jsonString("motDePasse", secret != null ? secret : "")
                ));
                return;
            }

            switch (methode) {
                case "GET" -> lister(exchange);
                case "POST" -> ajouter(exchange);
                case "PUT" -> modifierOuSecret(exchange);
                case "DELETE" -> supprimer(exchange);
                default -> ResponseUtil.json(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", "Erreur interne"));
        }
    }

    private void lister(HttpExchange exchange) throws Exception {
        Session session = security.verifierAuthentification(exchange);
        if (session == null) return;
        List<SystemeExterne> list = service.lister();
        ResponseUtil.json(exchange, 200, systemesToJson(list));
    }

    private void ajouter(HttpExchange exchange) throws Exception {
        Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
        if (session == null) return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
        Map<String, String> data = JsonUtil.parseObject(JsonUtil.lireCorps(reader));

        SystemeExterne se = new SystemeExterne();
        se.setNom(data.get("nom"));
        se.setUrl(data.get("url"));
        se.setLogin(data.get("login"));
        se.setIdDivisionExterne(Integer.parseInt(data.getOrDefault("idDivision", "0")));
        String idLie = data.get("idSystemeInterneLie");
        se.setIdSystemeInterneLie(idLie != null && !idLie.isEmpty() ? Integer.parseInt(idLie) : null);

        service.ajouter(se, data.get("motDePasse"), session);
        ResponseUtil.json(exchange, 201, JsonUtil.buildObject(
            JsonUtil.jsonString("succes", "true"),
            JsonUtil.jsonString("message", "Système externe ajouté"),
            JsonUtil.jsonInt("id", se.getId())
        ));
    }

    private void modifierOuSecret(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
        Map<String, String> data = JsonUtil.parseObject(JsonUtil.lireCorps(reader));

        if (path.endsWith("/secret")) {
            Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
            if (session == null) return;
            service.modifierMotDePasse(extraireId(path), data.get("motDePasse"), session);
            ResponseUtil.json(exchange, 200, "{\"succes\":true}");
        } else {
            Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
            if (session == null) return;
            int id = extraireId(path);
            SystemeExterne se = service.trouverParId(id);
            if (se == null) { ResponseUtil.json(exchange, 404, "{\"erreur\":\"Introuvable\"}"); return; }
            se.setNom(data.get("nom"));
            se.setUrl(data.get("url"));
            se.setLogin(data.get("login"));
            se.setIdDivisionExterne(Integer.parseInt(data.getOrDefault("idDivision", "0")));
            String idLie = data.get("idSystemeInterneLie");
            se.setIdSystemeInterneLie(idLie != null && !idLie.isEmpty() ? Integer.parseInt(idLie) : null);
            service.modifier(se, session);
            ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Système externe modifié\"}");
        }
    }

    private void supprimer(HttpExchange exchange) throws Exception {
        Session session = security.verifierAcces(exchange, "Administrateur");
        if (session == null) return;
        service.supprimer(extraireId(exchange.getRequestURI().getPath()), session);
        ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Système externe supprimé\"}");
    }

    private int extraireId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("systemes-externes") && i + 1 < parts.length) {
                try { return Integer.parseInt(parts[i + 1]); } catch (NumberFormatException e) {}
            }
        }
        return 0;
    }

    private String extraireFormat(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("format=csv")) return "csv";
        return "html";
    }

    private String systemesToJson(List<SystemeExterne> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (SystemeExterne se : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append(JsonUtil.jsonInt("id", se.getId())).append(",")
              .append(JsonUtil.jsonString("nom", se.getNom())).append(",")
              .append(JsonUtil.jsonString("url", se.getUrl())).append(",")
              .append(JsonUtil.jsonInt("idDivision", se.getIdDivisionExterne())).append(",")
              .append(JsonUtil.jsonString("idSystemeInterneLie",
                  se.getIdSystemeInterneLie() != null ? se.getIdSystemeInterneLie().toString() : "null")).append(",")
              .append(JsonUtil.jsonString("login", se.getLogin()))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
