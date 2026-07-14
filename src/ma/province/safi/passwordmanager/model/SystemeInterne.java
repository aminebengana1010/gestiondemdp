package ma.province.safi.passwordmanager.model;

public class SystemeInterne extends Systeme {
    private int idDivisionInterne;  // BD: IdDivisionInterne

    public SystemeInterne() {}

    public int getIdDivisionInterne() { return idDivisionInterne; }
    public void setIdDivisionInterne(int idDivisionInterne) { this.idDivisionInterne = idDivisionInterne; }
}
