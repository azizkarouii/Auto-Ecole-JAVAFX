package tn.spring.autoecole.models.enums;

public enum TypePermis {
    MOTO("Moto"),
    VOITURE("Voiture"),
    CAR("Car"),
    CAMION("Camion"),
    POIDS_LOURDS("Poids Lourds");

    private final String displayName;

    TypePermis(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static TypePermis fromString(String text) {
        for (TypePermis type : TypePermis.values()) {
            if (type.displayName.equalsIgnoreCase(text) || type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Aucun type de permis trouvé pour: " + text);
    }

    public String getIcon() {
        return switch (this) {
            case MOTO -> "🏍️";
            case VOITURE -> "🚗";
            case CAR -> "🚌";
            case CAMION -> "🚚";
            case POIDS_LOURDS -> "🚛";
        };
    }
}
