package ma.province.safi.passwordmanager.model;

public class SystemeExterne extends Systeme {
    private int idDivisionExterne;     // BD: IdDivisionExterne
    private Integer idSystemeInterneLie; // BD: IdSystemeInterne (nullable)

    public SystemeExterne() {}

    public int getIdDivisionExterne() { return idDivisionExterne; }
    public void setIdDivisionExterne(int idDivisionExterne) { this.idDivisionExterne = idDivisionExterne; }

    public Integer getIdSystemeInterneLie() { return idSystemeInterneLie; }
    public void setIdSystemeInterneLie(Integer idSystemeInterneLie) { this.idSystemeInterneLie = idSystemeInterneLie; }
}
