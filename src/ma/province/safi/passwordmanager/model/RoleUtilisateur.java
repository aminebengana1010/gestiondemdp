package ma.province.safi.passwordmanager.model;

public enum RoleUtilisateur {
    Administrateur,
    Agent_SSICTD,
    Utilisateur_consultation;

    public static RoleUtilisateur fromString(String s) {
        for (RoleUtilisateur r : values()) {
            if (r.name().equals(s) || r.name().replace("_", " ").equals(s)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Role inconnu: " + s);
    }
}
