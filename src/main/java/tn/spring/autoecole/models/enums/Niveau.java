package tn.spring.autoecole.models.enums;

public enum Niveau {
    CODE("Code de la route", 1),
    CONDUITE("Conduite", 2),
    PARC("Parc", 3),
    OBTENU("Permis obtenu", 4);

    private final String displayName;
    private final int ordre;

    Niveau(String displayName, int ordre) {
        this.displayName = displayName;
        this.ordre = ordre;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getOrdre() {
        return ordre;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Niveau getNext() {
        return switch (this) {
            case CODE -> CONDUITE;
            case CONDUITE -> PARC;
            case PARC -> OBTENU;
            case OBTENU -> null;
        };
    }

    public Niveau getPrevious() {
        return switch (this) {
            case OBTENU -> PARC;
            case PARC -> CONDUITE;
            case CONDUITE -> CODE;
            case CODE -> null;
        };
    }

    public boolean isAvant(Niveau autre) {
        return this.ordre < autre.ordre;
    }

    public boolean isApres(Niveau autre) {
        return this.ordre > autre.ordre;
    }

    public boolean isTerminal() {
        return this == OBTENU;
    }

    public static Niveau fromString(String text) {
        for (Niveau niveau : Niveau.values()) {
            if (niveau.displayName.equalsIgnoreCase(text) || niveau.name().equalsIgnoreCase(text)) {
                return niveau;
            }
        }
        throw new IllegalArgumentException("Aucun niveau trouvé pour: " + text);
    }

    public String getProgressPercentage() {
        return switch (this) {
            case CODE -> "25%";
            case CONDUITE -> "50%";
            case PARC -> "75%";
            case OBTENU -> "100%";
        };
    }

    public String getIcon() {
        return switch (this) {
            case CODE -> "📚";
            case CONDUITE -> "🚗";
            case PARC -> "🅿️";
            case OBTENU -> "✓";
        };
    }
}
