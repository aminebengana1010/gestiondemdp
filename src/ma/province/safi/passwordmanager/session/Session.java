package ma.province.safi.passwordmanager.session;

public class Session {
    private final String token;
    private final int idUtilisateur;
    private final String nomUtilisateur;
    private final String role;
    private final long expiration;

    public Session(String token, int idUtilisateur, String nomUtilisateur, String role, long expiration) {
        this.token = token;
        this.idUtilisateur = idUtilisateur;
        this.nomUtilisateur = nomUtilisateur;
        this.role = role;
        this.expiration = expiration;
    }

    public boolean estExpiree() {
        return System.currentTimeMillis() > expiration;
    }

    public String getToken() { return token; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public String getNomUtilisateur() { return nomUtilisateur; }
    public String getRole() { return role; }
    public long getExpiration() { return expiration; }
}
