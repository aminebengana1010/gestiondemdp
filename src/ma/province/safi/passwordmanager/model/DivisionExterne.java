package ma.province.safi.passwordmanager.model;

public class DivisionExterne {
    private int idDivisionExterne;
    private String nomDivision;    // BD: NomDivision
    private String typeDivision;   // BD: TypeDivision

    public int getIdDivisionExterne() { return idDivisionExterne; }
    public void setIdDivisionExterne(int idDivisionExterne) { this.idDivisionExterne = idDivisionExterne; }

    public String getNomDivision() { return nomDivision; }
    public void setNomDivision(String nomDivision) { this.nomDivision = nomDivision; }

    public String getTypeDivision() { return typeDivision; }
    public void setTypeDivision(String typeDivision) { this.typeDivision = typeDivision; }
}
