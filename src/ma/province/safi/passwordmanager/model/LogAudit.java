package ma.province.safi.passwordmanager.model;

import java.time.LocalDateTime;

public class LogAudit {
    private long idLog;          // BD: IdLog (bigint)
    private String action;       // BD: Action
    private String entite;       // BD: Entite
    private Integer idCible;     // BD: IdCible (nullable)
    private String cible;        // BD: Cible
    private String details;      // BD: Details
    private String adresseIP;    // BD: AdresseIP
    private int idUtilisateur;   // BD: IdUtilisateur
    private LocalDateTime dateAction; // BD: DateAction

    public long getIdLog() { return idLog; }
    public void setIdLog(long idLog) { this.idLog = idLog; }
    public int getIdAudit() { return (int) idLog; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntite() { return entite; }
    public void setEntite(String entite) { this.entite = entite; }

    public Integer getIdCible() { return idCible; }
    public void setIdCible(Integer idCible) { this.idCible = idCible; }
    public Integer getIdEntite() { return idCible; }

    public String getCible() { return cible; }
    public void setCible(String cible) { this.cible = cible; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getAdresseIP() { return adresseIP; }
    public void setAdresseIP(String adresseIP) { this.adresseIP = adresseIP; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
}
