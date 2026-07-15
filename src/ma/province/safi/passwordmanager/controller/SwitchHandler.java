package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.model.SwitchReseau;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.service.ExportService;
import ma.province.safi.passwordmanager.service.SwitchService;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class SwitchHandler implements HttpHandler {

    private final SwitchService switchService;
    private final SecurityInterceptor security;
    private final ExportService exportService;

    public SwitchHandler(SwitchService switchService, SecurityInterceptor security,
                         ExportService exportService) {
        this.switchService = switchService;
        this.security = security;
        this.exportService = exportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String methode = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(methode) && path.endsWith("/export")) {
                Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
                if (session == null) return;
                String query = exchange.getRequestURI().getQuery();
                boolean csv = query != null && query.contains("format=csv");
                if (csv) {
                    String csvData = exportService.exporterSwitchesCSV(session);
                    ResponseUtil.csv(exchange, csvData, "switches.csv");
                } else {
                    String html = exportService.exporterSwitchesHTML(session);
                    ResponseUtil.html(exchange, 200, html);
                }
                return;
            }

            if ("GET".equals(methode) && path.contains("/consulter")) {
                Session session = security.verifierRoleAdministrateur(exchange);
                if (session == null) return;
                int id = extraireId(path);
                String secret = switchService.consulterSecret(id, session);
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
        } catch (IllegalArgumentException e) {
            ResponseUtil.json(exchange, 400, JsonUtil.json("erreur", e.getMessage()));
        } catch (SecurityException e) {
            ResponseUtil.json(exchange, 403, JsonUtil.json("erreur", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", e.getMessage() != null ? e.getMessage() : "Erreur interne"));
        }
    }

    private void lister(HttpExchange exchange) throws Exception {
        Session session = security.verifierAuthentification(exchange);
        if (session == null) return;
        List<SwitchReseau> list = switchService.lister();
        ResponseUtil.json(exchange, 200, switchesToJson(list));
    }

    private void ajouter(HttpExchange exchange) throws Exception {
        Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
        if (session == null) return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
        Map<String, String> data = JsonUtil.parseObject(JsonUtil.lireCorps(reader));

        SwitchReseau sw = new SwitchReseau();
        sw.setNom(data.get("nom"));
        sw.setAdresseMAC(data.get("adresseMac"));
        sw.setEmplacement(data.get("emplacement"));
        sw.setLogin(data.get("login"));
        String motDePasse = data.get("motDePasse");

        switchService.ajouter(sw, motDePasse, session);
        ResponseUtil.json(exchange, 201, JsonUtil.buildObject(
            JsonUtil.jsonString("succes", "true"),
            JsonUtil.jsonString("message", "Switch ajouté"),
            JsonUtil.jsonInt("id", sw.getId())
        ));
    }

    private void modifierOuSecret(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
        Map<String, String> data = JsonUtil.parseObject(JsonUtil.lireCorps(reader));

        if (path.endsWith("/secret")) {
            Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
            if (session == null) return;
            int id = extraireId(path);
            switchService.modifierMotDePasse(id, data.get("motDePasse"), session);
            ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Mot de passe modifié\"}");
        } else {
            Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
            if (session == null) return;
            int id = extraireId(path);
            SwitchReseau sw = switchService.trouverParId(id);
            if (sw == null) { ResponseUtil.json(exchange, 404, "{\"erreur\":\"Switch introuvable\"}"); return; }
            sw.setNom(data.get("nom"));
            sw.setAdresseMAC(data.get("adresseMac"));
            sw.setEmplacement(data.get("emplacement"));
            sw.setLogin(data.get("login"));
            sw.setId(id);
            switchService.modifier(sw, session);
            ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Switch modifié\"}");
        }
    }

    private void supprimer(HttpExchange exchange) throws Exception {
        Session session = security.verifierAcces(exchange, "Administrateur");
        if (session == null) return;
        int id = extraireId(exchange.getRequestURI().getPath());
        switchService.supprimer(id, session);
        ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Switch supprimé\"}");
    }

    private int extraireId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("switches") && i + 1 < parts.length) {
                try { return Integer.parseInt(parts[i + 1]); } catch (NumberFormatException e) {}
            }
        }
        return 0;
    }

    private String switchesToJson(List<SwitchReseau> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (SwitchReseau sw : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append(JsonUtil.jsonInt("id", sw.getId())).append(",")
              .append(JsonUtil.jsonString("nom", sw.getNom())).append(",")
              .append(JsonUtil.jsonString("adresseMac", sw.getAdresseMAC())).append(",")
              .append(JsonUtil.jsonString("emplacement", sw.getEmplacement())).append(",")
              .append(JsonUtil.jsonString("login", sw.getLogin())).append(",")
              .append(JsonUtil.jsonString("motDePasse", sw.getMotDePasseClair() != null ? sw.getMotDePasseClair() : "")).append(",")
              .append(JsonUtil.jsonString("dateDernierChangement",
                  sw.getDateDernierChangement() != null ? sw.getDateDernierChangement().toString() : ""))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
