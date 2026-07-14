package ma.province.safi.passwordmanager.model;

public class SwitchReseau extends CompteTechnique {
    private String adresseMAC;   // BD: AdresseMAC
    private String emplacement;  // BD: Emplacement

    public SwitchReseau() {}

    public String getAdresseMAC() { return adresseMAC; }
    public void setAdresseMAC(String adresseMAC) { this.adresseMAC = adresseMAC; }

    public String getEmplacement() { return emplacement; }
    public void setEmplacement(String emplacement) { this.emplacement = emplacement; }
}
