package tn.spring.autoecole.services;

import tn.spring.autoecole.dao.UserDAO;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.utils.PasswordUtils;
import tn.spring.autoecole.utils.Session;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;
    private static final String SUPERADMIN_EMAIL = "superadmin@gmail.com";
    private static final String SUPERADMIN_PASSWORD = "superadmin123";

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authentifie un utilisateur
     * @return L'utilisateur authentifié ou Optional.empty() si échec
     */
    public Optional<User> login(String email, String password) throws SQLException {
        // Vérification des champs vides
        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return Optional.empty();
        }

        email = email.trim();

        // Vérification super admin
        if (SUPERADMIN_EMAIL.equals(email) && SUPERADMIN_PASSWORD.equals(password)) {
            Optional<User> superAdmin = userDAO.findByEmail(email);
            if (superAdmin.isPresent()) {
                Session.getInstance().setCurrentUser(superAdmin.get());
                return superAdmin;
            }
        }

        // Vérification utilisateur normal
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtils.checkPassword(password, user.getMotDePasse())) {
                Session.getInstance().setCurrentUser(user);
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        Session.getInstance().clearSession();
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isAuthenticated() {
        return Session.getInstance().isLoggedIn();
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    public User getCurrentUser() {
        return Session.getInstance().getCurrentUser();
    }

    /**
     * Change le mot de passe d'un utilisateur
     */
    public void changePassword(int userId, String oldPassword, String newPassword)
            throws SQLException, IllegalArgumentException {

        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        // Vérifier l'ancien mot de passe
        if (!PasswordUtils.checkPassword(oldPassword, user.getMotDePasse())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        // Valider le nouveau mot de passe
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 6 caractères");
        }

        // Hasher et sauvegarder le nouveau mot de passe
        user.setMotDePasse(PasswordUtils.hashPassword(newPassword));
        userDAO.update(user);
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur (admin uniquement)
     */
    public String resetPassword(int userId) throws SQLException {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        User user = userOpt.get();
        String newPassword = PasswordUtils.generateTemporaryPassword(8);
        user.setMotDePasse(PasswordUtils.hashPassword(newPassword));
        userDAO.update(user);

        return newPassword;
    }
}