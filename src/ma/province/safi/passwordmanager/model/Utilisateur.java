package ma.province.safi.passwordmanager.model;

import java.time.LocalDateTime;

public class Utilisateur {
    private int idUtilisateur;
    private String nom;          // BD: Nom
    private String email;        // BD: Email
    private String login;        // BD: Login
    private String motPasseHash; // BD: MotPasseHash
    private String selMotPasse;  // BD: SelMotPasse
    private int idRole;          // BD: IdRole
    private boolean estActif;    // BD: EstActif
    private LocalDateTime dateCreation;

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNomComplet() { return nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotPasseHash() { return motPasseHash; }
    public void setMotPasseHash(String motPasseHash) { this.motPasseHash = motPasseHash; }

    public String getSelMotPasse() { return selMotPasse; }
    public void setSelMotPasse(String selMotPasse) { this.selMotPasse = selMotPasse; }

    public int getIdRole() { return idRole; }
    public void setIdRole(int idRole) { this.idRole = idRole; }
    public String getRoleName() {
        switch (idRole) {
            case 1: return "Administrateur";
            case 2: return "Agent SSICTD";
            case 3: return "Utilisateur consultation";
            default: return "Inconnu";
        }
    }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }
    public boolean isActif() { return estActif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
