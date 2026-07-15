package ma.province.safi.passwordmanager.model;

public class DivisionExterne {
    private int idDivisionExterne;
    private String nomDivision;           // BD: NomDivision
    private TypeDivisionExterne type;     // BD: TypeDivision

    public int getIdDivisionExterne() { return idDivisionExterne; }
    public void setIdDivisionExterne(int idDivisionExterne) { this.idDivisionExterne = idDivisionExterne; }

    public String getNomDivision() { return nomDivision; }
    public void setNomDivision(String nomDivision) { this.nomDivision = nomDivision; }

    public TypeDivisionExterne getType() { return type; }
    public void setType(TypeDivisionExterne type) { this.type = type; }

    /** @deprecated utiliser getType() */
    @Deprecated
    public String getTypeDivision() { return type != null ? type.toString() : null; }
    /** @deprecated utiliser setType(TypeDivisionExterne) */
    @Deprecated
    public void setTypeDivision(String typeDivision) { this.type = TypeDivisionExterne.fromString(typeDivision); }
}
