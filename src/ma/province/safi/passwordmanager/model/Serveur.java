package ma.province.safi.passwordmanager.model;

public class Serveur extends CompteTechnique {
    private String adresseIP;

    public Serveur() {}

    public Serveur(String nom, String adresseIP, String login) {
        this.nom = nom;
        this.adresseIP = adresseIP;
        this.login = login;
    }

    public String getAdresseIP() { return adresseIP; }
    public void setAdresseIP(String adresseIP) { this.adresseIP = adresseIP; }
}
