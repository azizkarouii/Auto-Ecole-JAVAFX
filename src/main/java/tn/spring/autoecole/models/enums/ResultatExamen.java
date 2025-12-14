package tn.spring.autoecole.models.enums;

public enum ResultatExamen {
    EN_ATTENTE("En attente", "⏳", "orange"),
    REUSSI("Réussi", "✓", "green"),
    ECHEC("Échec", "✗", "red");

    private final String displayName;
    private final String icon;
    private final String color;

    ResultatExamen(String displayName, String icon, String color) {
        this.displayName = displayName;
        this.icon = icon;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static ResultatExamen fromString(String text) {
        for (ResultatExamen resultat : ResultatExamen.values()) {
            if (resultat.displayName.equalsIgnoreCase(text) || resultat.name().equalsIgnoreCase(text)) {
                return resultat;
            }
        }
        throw new IllegalArgumentException("Aucun résultat trouvé pour: " + text);
    }

    public String getDisplayWithIcon() {
        return icon + " " + displayName;
    }

    public boolean isSuccess() {
        return this == REUSSI;
    }

    public boolean isFailed() {
        return this == ECHEC;
    }

    public boolean isPending() {
        return this == EN_ATTENTE;
    }
}