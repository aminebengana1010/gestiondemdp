package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.dao.AuditDAO;
import ma.province.safi.passwordmanager.model.LogAudit;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.util.JsonUtil;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.IOException;
import java.util.List;

public class AuditHandler implements HttpHandler {

    private final AuditDAO auditDAO;
    private final SecurityInterceptor security;

    public AuditHandler(AuditDAO auditDAO, SecurityInterceptor security) {
        this.auditDAO = auditDAO;
        this.security = security;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (security.verifierRoleAdministrateur(exchange) == null) return;

            if (!"GET".equals(exchange.getRequestMethod())) {
                ResponseUtil.json(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
                return;
            }

            List<LogAudit> list = auditDAO.listerDerniers(100);
            ResponseUtil.json(exchange, 200, auditToJson(list));
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, JsonUtil.json("erreur", "Erreur interne"));
        }
    }

    private String auditToJson(List<LogAudit> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (LogAudit a : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{")
              .append(JsonUtil.jsonInt("id", a.getIdAudit())).append(",")
              .append(JsonUtil.jsonString("action", a.getAction())).append(",")
              .append(JsonUtil.jsonString("entite", a.getEntite())).append(",")
              .append(JsonUtil.jsonString("idEntite",
                  a.getIdEntite() != null ? a.getIdEntite().toString() : "")).append(",")
              .append(JsonUtil.jsonInt("idUtilisateur", a.getIdUtilisateur())).append(",")
              .append(JsonUtil.jsonString("date",
                  a.getDateAction() != null ? a.getDateAction().toString() : "")).append(",")
              .append(JsonUtil.jsonString("details", a.getDetails()))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
