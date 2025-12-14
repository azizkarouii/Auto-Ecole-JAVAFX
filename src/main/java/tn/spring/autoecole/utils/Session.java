package tn.spring.autoecole.utils;

import tn.spring.autoecole.models.User;

public class Session {
    private static Session instance;
    private User currentUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void clearSession() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getNomComplet() : "Invité";
    }

    public boolean hasRole(tn.spring.autoecole.models.enums.Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean isSuperAdmin() {
        return currentUser != null && currentUser.isSuperAdmin();
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isMoniteur() {
        return currentUser != null && currentUser.isMoniteur();
    }

    public boolean isApprenant() {
        return currentUser != null && currentUser.isApprenant();
    }
}