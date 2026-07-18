package ma.province.safi.passwordmanager.model;

public enum TypeDivisionExterne {
    AAL,
    CAIDAT,
    PASHALIK,
    CERCLE,
    DISTRICT_1,
    DISTRICT_2,
    DISTRICT_3;

    public static TypeDivisionExterne fromString(String s) {
        if (s == null || s.isBlank()) return null;
        String upper = s.trim().toUpperCase();
        if ("AAL".equals(upper)) return AAL;
        if ("CAIDAT".equals(upper) || "CAÏDAT".equals(upper)) return CAIDAT;
        if ("PASHALIK".equals(upper)) return PASHALIK;
        if ("CERCLE".equals(upper)) return CERCLE;
        if ("DISTRICT 1".equals(upper)) return DISTRICT_1;
        if ("DISTRICT 2".equals(upper)) return DISTRICT_2;
        if ("DISTRICT 3".equals(upper)) return DISTRICT_3;
        throw new IllegalArgumentException("Type de division invalide : " + s);
    }

    @Override
    public String toString() {
        if (this == AAL) return "AAL";
        if (this == CAIDAT) return "Caïdat";
        if (this == PASHALIK) return "Pashalik";
        if (this == CERCLE) return "Cercle";
        if (this == DISTRICT_1) return "District 1";
        if (this == DISTRICT_2) return "District 2";
        return "District 3";
    }
}
