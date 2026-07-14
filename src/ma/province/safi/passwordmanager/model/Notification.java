package ma.province.safi.passwordmanager.model;

import java.time.LocalDateTime;

public class Notification {
    private long idNotification;    // BD: IdNotification (bigint)
    private int idUtilisateur;      // BD: IdUtilisateur
    private String message;         // BD: Message
    private String typeNotification;// BD: TypeNotification
    private boolean lu;             // BD: Lu
    private LocalDateTime dateNotification; // BD: DateNotification
    private LocalDateTime dateLecture;      // BD: DateLecture

    public long getIdNotification() { return idNotification; }
    public void setIdNotification(long idNotification) { this.idNotification = idNotification; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTypeNotification() { return typeNotification; }
    public void setTypeNotification(String typeNotification) { this.typeNotification = typeNotification; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    public boolean isLue() { return lu; }

    public LocalDateTime getDateNotification() { return dateNotification; }
    public void setDateNotification(LocalDateTime dateNotification) { this.dateNotification = dateNotification; }
    public LocalDateTime getDateCreation() { return dateNotification; }

    public LocalDateTime getDateLecture() { return dateLecture; }
    public void setDateLecture(LocalDateTime dateLecture) { this.dateLecture = dateLecture; }
}
