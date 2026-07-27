package ma.province.safi.passwordmanager.model;

public class DivisionExterne {
    private int idDivisionExterne;
    private String nomDivision;           // BD: NomDivision (auto-généré)
    private TypeDivisionExterne type;     // BD: TypeDivision
    private String sousType;              // BD: SousType (District 1-3 pour AAL, Cercle pour Commune, nom Pashalik, Numéro pour District)
    private String caidatNom;             // BD: CaidatNom (uniquement pour Commune)

    public int getIdDivisionExterne() { return idDivisionExterne; }
    public void setIdDivisionExterne(int idDivisionExterne) { this.idDivisionExterne = idDivisionExterne; }

    public String getNomDivision() { return nomDivision; }
    public void setNomDivision(String nomDivision) { this.nomDivision = nomDivision; }

    public TypeDivisionExterne getType() { return type; }
    public void setType(TypeDivisionExterne type) { this.type = type; }

    public String getSousType() { return sousType; }
    public void setSousType(String sousType) { this.sousType = sousType; }

    public String getCaidatNom() { return caidatNom; }
    public void setCaidatNom(String caidatNom) { this.caidatNom = caidatNom; }

    public String genererNom() {
        if (type == TypeDivisionExterne.AAL) {
            return "AAL - " + (sousType != null ? sousType : "");
        } else if (type == TypeDivisionExterne.COMMUNE) {
            return "Commune " + (sousType != null ? sousType : "") + " - " + (caidatNom != null ? caidatNom : "");
        } else if (type == TypeDivisionExterne.PASHALIK) {
            return "Pashalik " + (sousType != null ? sousType : "");
        } else if (type == TypeDivisionExterne.DISTRICT) {
            return "District " + (sousType != null ? sousType : "");
        }
        return nomDivision != null ? nomDivision : "";
    }

    /** @deprecated utiliser getType() */
    @Deprecated
    public String getTypeDivision() { return type != null ? type.toString() : null; }
    /** @deprecated utiliser setType(TypeDivisionExterne) */
    @Deprecated
    public void setTypeDivision(String typeDivision) { this.type = TypeDivisionExterne.fromString(typeDivision); }
}
