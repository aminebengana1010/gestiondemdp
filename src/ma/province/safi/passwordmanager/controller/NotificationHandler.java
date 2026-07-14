package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.model.Notification;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.service.NotificationService;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.IOException;
import java.util.List;

public class NotificationHandler implements HttpHandler {

    private final NotificationService notificationService;
    private final SecurityInterceptor security;

    public NotificationHandler(NotificationService notificationService, SecurityInterceptor security) {
        this.notificationService = notificationService;
        this.security = security;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String methode = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(methode) && path.endsWith("/compteur")) {
                Session session = security.verifierAuthentification(exchange);
                if (session == null) return;
                int count = notificationService.compterNonLues(session.getIdUtilisateur());
                ResponseUtil.json(exchange, 200, JsonUtil.buildObject(
                    JsonUtil.jsonString("succes", "true"),
                    JsonUtil.jsonInt("nonLues", count)
                ));
                return;
            }

            if ("PUT".equals(methode) && path.contains("/lire")) {
                Session session = security.verifierAuthentification(exchange);
                if (session == null) return;
                long id = extraireId(path);
                notificationService.marquerLue(id);
                ResponseUtil.json(exchange, 200, "{\"succes\":true}");
                return;
            }

            if ("GET".equals(methode)) {
                Session session = security.verifierAuthentification(exchange);
                if (session == null) return;
                boolean toutes = path.endsWith("/toutes");
                List<Notification> list = toutes
                    ? notificationService.listerToutes(session.getIdUtilisateur())
                    : notificationService.listerNonLues(session.getIdUtilisateur());
                ResponseUtil.json(exchange, 200, notificationsToJson(list));
                return;
            }

            ResponseUtil.json(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", "Erreur interne"));
        }
    }

    private long extraireId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("notifications") && i + 1 < parts.length) {
                try { return Integer.parseInt(parts[i + 1]); } catch (NumberFormatException e) {}
            }
        }
        return 0;
    }

    private String notificationsToJson(List<Notification> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Notification n : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append(JsonUtil.jsonString("id", String.valueOf(n.getIdNotification()))).append(",")
              .append(JsonUtil.jsonString("message", n.getMessage())).append(",")
              .append(JsonUtil.jsonString("type", n.getTypeNotification())).append(",")
              .append(JsonUtil.jsonBool("lue", n.isLue())).append(",")
              .append(JsonUtil.jsonString("date",
                  n.getDateCreation() != null ? n.getDateCreation().toString() : ""))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
