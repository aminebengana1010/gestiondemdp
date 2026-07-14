package ma.province.safi.passwordmanager.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final long DUREE_SESSION_MS = 30 * 60 * 1000; // 30 minutes

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public String creerSession(int idUtilisateur, String nomUtilisateur, String role) {
        String token = UUID.randomUUID().toString();
        long expiration = System.currentTimeMillis() + DUREE_SESSION_MS;
        Session session = new Session(token, idUtilisateur, nomUtilisateur, role, expiration);
        sessions.put(token, session);
        return token;
    }

    public Session obtenirSession(String token) {
        if (token == null) return null;
        Session session = sessions.get(token);
        if (session == null || session.estExpiree()) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    public void detruireSession(String token) {
        if (token != null) sessions.remove(token);
    }

    public int getNombreSessions() {
        nettoyerSessionsExpirees();
        return sessions.size();
    }

    private void nettoyerSessionsExpirees() {
        sessions.values().removeIf(Session::estExpiree);
    }
}
