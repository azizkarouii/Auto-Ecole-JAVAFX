package tn.spring.autoecole.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tn.spring.autoecole.HelloApplication;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.enums.Role;
import tn.spring.autoecole.services.AuthService;
import tn.spring.autoecole.utils.AlertUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        try {
            Optional<User> userOpt = authService.login(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                redirectToRoleView(user.getRole());
            } else {
                showError("Email ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur de connexion",
                    "Impossible de se connecter à la base de données: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void redirectToRoleView(Role role) throws IOException {
        switch (role) {
            case SUPERADMIN -> HelloApplication.showSuperAdminView();
            case ADMIN -> HelloApplication.showAdminView();
            case MONITEUR -> HelloApplication.showMoniteurView();
            case APPRENANT -> HelloApplication.showApprenantView();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleCancel() {
        System.exit(0);
    }
}