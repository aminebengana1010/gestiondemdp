package ma.province.safi.passwordmanager.model;

public class SystemeExterne extends Systeme {
    private int idDivisionExterne;     // BD: IdDivisionExterne

    public SystemeExterne() {}

    public int getIdDivisionExterne() { return idDivisionExterne; }
    public void setIdDivisionExterne(int idDivisionExterne) { this.idDivisionExterne = idDivisionExterne; }
}
