package ma.province.safi.passwordmanager.model;

import java.util.List;

public enum TypeDivisionExterne {
    AAL,
    COMMUNE,
    PASHALIK,
    DISTRICT;

    public static TypeDivisionExterne fromString(String s) {
        if (s == null || s.isBlank()) return null;
        String upper = s.trim().toUpperCase();
        if ("AAL".equals(upper)) return AAL;
        if ("COMMUNE".equals(upper) || "COMMUNE".equals(upper)) return COMMUNE;
        if ("PASHALIK".equals(upper)) return PASHALIK;
        if ("DISTRICT".equals(upper)) return DISTRICT;
        throw new IllegalArgumentException("Type de division invalide : " + s);
    }

    @Override
    public String toString() {
        if (this == AAL) return "AAL";
        if (this == COMMUNE) return "Commune";
        if (this == PASHALIK) return "Pashalik";
        return "District";
    }

    public boolean aSousTypes() {
        return true;
    }

    public boolean aSousType2() {
        return this == COMMUNE;
    }

    public List<String> sousTypes() {
        return switch (this) {
            case AAL -> List.of("District 1", "District 2", "District 3");
            case COMMUNE -> List.of("Abda", "Gzoula", "Hrara");
            case PASHALIK -> List.of("SAFI", "Gzoula", "jemaa shaim");
            case DISTRICT -> List.of("District 1", "District 2", "District 3");
        };
    }

    public static List<TypeDivisionExterne> typesPrincipaux() {
        return List.of(AAL, COMMUNE, PASHALIK, DISTRICT);
    }
}
