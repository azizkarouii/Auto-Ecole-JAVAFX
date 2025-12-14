package tn.spring.autoecole.models.enums;

public enum Role {
    SUPERADMIN("Super Admin"),
    ADMIN("Administrateur"),
    MONITEUR("Moniteur"),
    APPRENANT("Apprenant");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Role fromString(String text) {
        for (Role role : Role.values()) {
            if (role.displayName.equalsIgnoreCase(text) || role.name().equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Aucun rôle trouvé pour: " + text);
    }
}
