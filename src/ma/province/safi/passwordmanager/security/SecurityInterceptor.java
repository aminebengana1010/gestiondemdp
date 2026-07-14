package ma.province.safi.passwordmanager.security;

import com.sun.net.httpserver.HttpExchange;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.session.SessionManager;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SecurityInterceptor {

    private final SessionManager sessionManager;

    public SecurityInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public Session verifierAcces(HttpExchange exchange, String... rolesAutorises)
            throws IOException {

        String token = CookieUtil.extraireCookie(exchange, "SESSION_ID");
        Session session = sessionManager.obtenirSession(token);

        if (session == null) {
            ResponseUtil.json(exchange, 401, "{\"erreur\":\"Session invalide ou expirée\"}");
            return null;
        }

        if (rolesAutorises.length == 0) {
            return session;
        }

        List<String> roles = Arrays.asList(rolesAutorises);
        if (roles.contains(session.getRole())) {
            return session;
        }

        ResponseUtil.json(exchange, 403, "{\"erreur\":\"Accès refusé. Rôle insuffisant.\"}");
        return null;
    }

    public Session verifierAuthentification(HttpExchange exchange) throws IOException {
        String token = CookieUtil.extraireCookie(exchange, "SESSION_ID");
        Session session = sessionManager.obtenirSession(token);
        if (session == null) {
            ResponseUtil.json(exchange, 401, "{\"erreur\":\"Session invalide ou expirée\"}");
            return null;
        }
        return session;
    }

    public Session verifierRoleAdministrateur(HttpExchange exchange) throws IOException {
        return verifierAcces(exchange, "Administrateur");
    }
}
