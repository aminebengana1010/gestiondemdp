package ma.province.safi.passwordmanager.model;

public enum TypeDivisionExterne {
    AAL,
    CAIDAT;

    public static TypeDivisionExterne fromString(String s) {
        if (s == null || s.isBlank()) return null;
        String upper = s.trim().toUpperCase();
        if ("AAL".equals(upper)) return AAL;
        if ("CAIDAT".equals(upper) || "CAÏDAT".equals(upper)) return CAIDAT;
        throw new IllegalArgumentException("Type de division invalide : " + s);
    }

    @Override
    public String toString() {
        // Format tel que stocké en BD
        return this == AAL ? "AAL" : "Caïdat";
    }
}
