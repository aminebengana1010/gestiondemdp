package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.model.Serveur;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.service.ExportService;
import ma.province.safi.passwordmanager.service.ServeurService;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class ServeurHandler implements HttpHandler {

    private final ServeurService serveurService;
    private final SecurityInterceptor security;
    private final ExportService exportService;

    public ServeurHandler(ServeurService serveurService, SecurityInterceptor security,
                          ExportService exportService) {
        this.serveurService = serveurService;
        this.security = security;
        this.exportService = exportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String methode = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            if ("GET".equals(methode) && path.endsWith("/export")) {
                Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
                if (session == null) return;
                boolean csv = query != null && query.contains("format=csv");
                if (csv) {
                    String csvData = exportService.exporterServeursCSV(session);
                    ResponseUtil.csv(exchange, csvData, "serveurs.csv");
                } else {
                    String html = exportService.exporterServeursHTML(session);
                    ResponseUtil.html(exchange, 200, html);
                }
                return;
            }

            if ("GET".equals(methode) && path.contains("/consulter")) {
                Session session = security.verifierRoleAdministrateur(exchange);
                if (session == null) return;
                int id = extraireId(path);
                String secret = serveurService.consulterSecret(id, session);
                String json = JsonUtil.buildObject(
                    JsonUtil.jsonString("succes", "true"),
                    JsonUtil.jsonString("motDePasse", secret != null ? secret : "")
                );
                ResponseUtil.json(exchange, 200, json);
                return;
            }

            if ("GET".equals(methode) && query != null && query.contains("recherche=")) {
                Session session = security.verifierAuthentification(exchange);
                if (session == null) return;
                String texte = extraireParam(query, "recherche");
                List<Serveur> resultats = serveurService.rechercher(texte);
                ResponseUtil.json(exchange, 200, serveursToJson(resultats));
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
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", "Erreur interne"));
        }
    }

    private void lister(HttpExchange exchange) throws Exception {
        Session session = security.verifierAuthentification(exchange);
        if (session == null) return;

        List<Serveur> list = serveurService.lister();
        ResponseUtil.json(exchange, 200, serveursToJson(list));
    }

    private void ajouter(HttpExchange exchange) throws Exception {
        Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
        if (session == null) return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
        Map<String, String> data = JsonUtil.parseObject(JsonUtil.lireCorps(reader));

        Serveur s = new Serveur();
        s.setNom(data.get("nom"));
        s.setAdresseIP(data.get("adresseIP"));
        s.setLogin(data.get("login"));
        String motDePasse = data.get("motDePasse");

        serveurService.ajouter(s, motDePasse, session);
        ResponseUtil.json(exchange, 201, JsonUtil.buildObject(
            JsonUtil.jsonString("succes", "true"),
            JsonUtil.jsonString("message", "Serveur ajouté avec succès"),
            JsonUtil.jsonInt("id", s.getId())
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
            String nouveauMotDePasse = data.get("motDePasse");
            serveurService.modifierMotDePasse(id, nouveauMotDePasse, session);
            ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Mot de passe modifié\"}");
        } else {
            Session session = security.verifierAcces(exchange, "Administrateur", "Agent SSICTD");
            if (session == null) return;
            int id = extraireId(path);
            serveurService.modifier(id, data.get("nom"), data.get("adresseIP"), data.get("login"), session);
            ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Serveur modifié\"}");
        }
    }

    private void supprimer(HttpExchange exchange) throws Exception {
        Session session = security.verifierAcces(exchange, "Administrateur");
        if (session == null) return;

        int id = extraireId(exchange.getRequestURI().getPath());
        serveurService.supprimer(id, session);
        ResponseUtil.json(exchange, 200, "{\"succes\":true,\"message\":\"Serveur supprimé\"}");
    }

    private int extraireId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("serveurs".equals(parts[i]) && i + 1 < parts.length) {
                try { return Integer.parseInt(parts[i + 1]); } catch (NumberFormatException e) { /* ignorer */ }
            }
        }
        return 0;
    }

    private String extraireParam(String query, String nom) {
        if (query == null) return "";
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(nom)) {
                return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private String serveursToJson(List<Serveur> serveurs) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Serveur s : serveurs) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append(JsonUtil.jsonInt("id", s.getId())).append(",")
              .append(JsonUtil.jsonString("nom", s.getNom())).append(",")
              .append(JsonUtil.jsonString("adresseIP", s.getAdresseIP())).append(",")
              .append(JsonUtil.jsonString("login", s.getLogin())).append(",")
              .append(JsonUtil.jsonString("dateDernierChangement",
                  s.getDateDernierChangement() != null ? s.getDateDernierChangement().toString() : ""))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
