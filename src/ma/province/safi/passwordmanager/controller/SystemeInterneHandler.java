package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.model.SystemeInterne;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.service.ExportService;
import ma.province.safi.passwordmanager.service.SystemeInterneService;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class SystemeInterneHandler implements HttpHandler {

    private final SystemeInterneService service;
    private final SecurityInterceptor security;
    private final ExportService exportService;

    public SystemeInterneHandler(SystemeInterneService service, SecurityInterceptor security, ExportService exportService) {
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
                    String csv = exportService.exporterSystemesInternesCSV(session);
                    ResponseUtil.csv(exchange, csv, "systemes-internes.csv");
                } else {
                    String html = exportService.exporterSystemesInternesHTML(session);
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
        List<SystemeInterne> list = service.lister();
        ResponseUtil.json(exchange, 200, systemesToJson(list));
    }

    private void ajouter(HttpExchange exchange) throws Exception {
        Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
        if (session == null) return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
        Map<String, String> data = JsonUtil.parseObject(JsonUtil.lireCorps(reader));

        SystemeInterne si = new SystemeInterne();
        si.setNom(data.get("nom"));
        si.setUrl(data.get("url"));
        si.setLogin(data.get("login"));
        si.setIdDivisionInterne(Integer.parseInt(data.getOrDefault("idDivision", "0")));

        service.ajouter(si, data.get("motDePasse"), session);
        ResponseUtil.json(exchange, 201, JsonUtil.buildObject(
            JsonUtil.jsonString("succes", "true"),
            JsonUtil.jsonString("message", "Système interne ajouté"),
            JsonUtil.jsonInt("id", si.getId())
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
            SystemeInterne si = service.trouverParId(id);
            if (si == null) { ResponseUtil.json(exchange, 404, "{\"erreur\":\"Introuvable\"}"); return; }
            si.setNom(data.get("nom"));
            si.setUrl(data.get("url"));
            si.setLogin(data.get("login"));
            si.setIdDivisionInterne(Integer.parseInt(data.getOrDefault("idDivision", "0")));
            service.modifier(si, session);
            ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Système interne modifié\"}");
        }
    }

    private void supprimer(HttpExchange exchange) throws Exception {
        Session session = security.verifierAcces(exchange, "Administrateur");
        if (session == null) return;
        service.supprimer(extraireId(exchange.getRequestURI().getPath()), session);
        ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Système interne supprimé\"}");
    }

    private int extraireId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("systemes-internes") && i + 1 < parts.length) {
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

    private String systemesToJson(List<SystemeInterne> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (SystemeInterne si : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append(JsonUtil.jsonInt("id", si.getId())).append(",")
              .append(JsonUtil.jsonString("nom", si.getNom())).append(",")
              .append(JsonUtil.jsonString("url", si.getUrl())).append(",")
              .append(JsonUtil.jsonInt("idDivision", si.getIdDivisionInterne())).append(",")
              .append(JsonUtil.jsonString("login", si.getLogin()))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
