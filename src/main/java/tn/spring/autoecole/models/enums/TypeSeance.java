package tn.spring.autoecole.models.enums;

public enum TypeSeance {
    CODE("Code", "📚"),
    CONDUITE("Conduite", "🚗"),
    PARC("Parc", "🅿️");

    private final String displayName;
    private final String icon;

    TypeSeance(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static TypeSeance fromString(String text) {
        for (TypeSeance type : TypeSeance.values()) {
            if (type.displayName.equalsIgnoreCase(text) || type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Aucun type de séance trouvé pour: " + text);
    }

    public boolean requiresMoniteur() {
        return this == CONDUITE || this == PARC;
    }

    public boolean requiresVehicule() {
        return this == CONDUITE || this == PARC;
    }

    public Niveau getCorrespondingNiveau() {
        return switch (this) {
            case CODE -> Niveau.CODE;
            case CONDUITE -> Niveau.CONDUITE;
            case PARC -> Niveau.PARC;
        };
    }
}
