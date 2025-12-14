package tn.spring.autoecole.utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.enums.*;

import java.time.LocalDate;
import java.util.Optional;

public class UserDialogUtil {

    public static Optional<User> showAddAdminDialog() {
        return showUserDialog(null, Role.ADMIN, "Ajouter un Administrateur");
    }

    public static Optional<User> showEditAdminDialog(User admin) {
        return showUserDialog(admin, Role.ADMIN, "Modifier l'Administrateur");
    }

    public static Optional<User> showAddMoniteurDialog() {
        return showUserDialog(null, Role.MONITEUR, "Ajouter un Moniteur");
    }

    public static Optional<User> showAddApprenantDialog() {
        return showUserDialog(null, Role.APPRENANT, "Ajouter un Apprenant");
    }

    public static Optional<User> showEditUserDialog(User user) {
        String title = "Modifier " + user.getRole().getDisplayName();
        return showUserDialog(user, user.getRole(), title);
    }

    private static Optional<User> showUserDialog(User existingUser, Role role, String title) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(existingUser == null ?
                "Veuillez remplir tous les champs" :
                "Modifiez les informations");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        TextField cinField = new TextField();
        cinField.setPromptText("CIN (8 chiffres)");
        DatePicker dateNaissancePicker = new DatePicker();
        dateNaissancePicker.setPromptText("Date de naissance");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField telField = new TextField();
        telField.setPromptText("Téléphone (8 chiffres)");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe (min 6 caractères)");

        ComboBox<TypePermis> typePermisCombo = new ComboBox<>();
        typePermisCombo.getItems().addAll(TypePermis.values());
        typePermisCombo.setPromptText("Type de permis");

        ComboBox<Niveau> niveauCombo = new ComboBox<>();
        niveauCombo.getItems().addAll(Niveau.values());
        niveauCombo.setPromptText("Niveau");
        niveauCombo.setValue(Niveau.CODE);

        // Champs pour les heures prévues (seulement pour APPRENANT)
        Spinner<Integer> heuresCodeSpinner = new Spinner<>(1, 50, 8);
        heuresCodeSpinner.setEditable(true);
        heuresCodeSpinner.setPrefWidth(100);

        Spinner<Integer> heuresConduiteSpinner = new Spinner<>(1, 100, 25);
        heuresConduiteSpinner.setEditable(true);
        heuresConduiteSpinner.setPrefWidth(100);

        Spinner<Integer> heuresParcSpinner = new Spinner<>(1, 30, 4);
        heuresParcSpinner.setEditable(true);
        heuresParcSpinner.setPrefWidth(100);

        CheckBox fraisInscriptionCheck = new CheckBox("Frais d'inscription payés");

        if (existingUser != null) {
            nomField.setText(existingUser.getNom());
            prenomField.setText(existingUser.getPrenom());
            cinField.setText(existingUser.getCin());
            dateNaissancePicker.setValue(existingUser.getDateNaissance());
            emailField.setText(existingUser.getMail());
            telField.setText(existingUser.getNumTel());
            passwordField.setPromptText("Laisser vide pour ne pas changer");

            if (existingUser.getTypePermis() != null) {
                typePermisCombo.setValue(existingUser.getTypePermis());
            }
            if (existingUser.getNiveau() != null) {
                niveauCombo.setValue(existingUser.getNiveau());
            }

            // Charger les heures prévues
            heuresCodeSpinner.getValueFactory().setValue(existingUser.getHeuresPreveuesCode());
            heuresConduiteSpinner.getValueFactory().setValue(existingUser.getHeuresPreveuesConduite());
            heuresParcSpinner.getValueFactory().setValue(existingUser.getHeuresPreveuesParc());
            fraisInscriptionCheck.setSelected(existingUser.isFraisInscriptionPaye());
        }

        int row = 0;
        grid.add(new Label("Nom:"), 0, row);
        grid.add(nomField, 1, row++);
        grid.add(new Label("Prénom:"), 0, row);
        grid.add(prenomField, 1, row++);
        grid.add(new Label("CIN:"), 0, row);
        grid.add(cinField, 1, row++);
        grid.add(new Label("Date de naissance:"), 0, row);
        grid.add(dateNaissancePicker, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Téléphone:"), 0, row);
        grid.add(telField, 1, row++);
        grid.add(new Label("Mot de passe:"), 0, row);
        grid.add(passwordField, 1, row++);

        if (role == Role.APPRENANT) {
            grid.add(new Label("Type de permis:"), 0, row);
            grid.add(typePermisCombo, 1, row++);
            grid.add(new Label("Niveau:"), 0, row);
            grid.add(niveauCombo, 1, row++);

            // Séparateur
            Separator sep = new Separator();
            GridPane.setColumnSpan(sep, 2);
            grid.add(sep, 0, row++);

            // Titre heures prévues
            Label heuresTitle = new Label("Heures de pratique prévues:");
            heuresTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            GridPane.setColumnSpan(heuresTitle, 2);
            grid.add(heuresTitle, 0, row++);

            // Heures Code
            grid.add(new Label("📚 Code:"), 0, row);
            HBox codeBox = new HBox(5);
            codeBox.getChildren().addAll(heuresCodeSpinner, new Label("heures"));
            grid.add(codeBox, 1, row++);

            // Heures Conduite
            grid.add(new Label("🚗 Conduite:"), 0, row);
            HBox conduiteBox = new HBox(5);
            conduiteBox.getChildren().addAll(heuresConduiteSpinner, new Label("heures"));
            grid.add(conduiteBox, 1, row++);

            // Heures Parc
            grid.add(new Label("🅿️ Parc:"), 0, row);
            HBox parcBox = new HBox(5);
            parcBox.getChildren().addAll(heuresParcSpinner, new Label("heures"));
            grid.add(parcBox, 1, row++);

            // Frais inscription
            GridPane.setColumnSpan(fraisInscriptionCheck, 2);
            grid.add(fraisInscriptionCheck, 0, row++);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String nom = nomField.getText().trim();
                    String prenom = prenomField.getText().trim();
                    String cin = cinField.getText().trim();
                    LocalDate dateNaissance = dateNaissancePicker.getValue();
                    String email = emailField.getText().trim();
                    String tel = telField.getText().trim();
                    String password = passwordField.getText();

                    if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() ||
                            dateNaissance == null || email.isEmpty() || tel.isEmpty()) {
                        AlertUtils.showError("Erreur", "Tous les champs sont obligatoires");
                        return null;
                    }

                    if (existingUser == null && password.isEmpty()) {
                        AlertUtils.showError("Erreur", "Le mot de passe est obligatoire");
                        return null;
                    }

                    if (role == Role.APPRENANT && typePermisCombo.getValue() == null) {
                        AlertUtils.showError("Erreur", "Le type de permis est obligatoire pour un apprenant");
                        return null;
                    }

                    User user = existingUser != null ? existingUser : new User();
                    user.setNom(nom);
                    user.setPrenom(prenom);
                    user.setCin(cin);
                    user.setDateNaissance(dateNaissance);
                    user.setMail(email);
                    user.setNumTel(tel);
                    user.setRole(role);

                    if (!password.isEmpty()) {
                        user.setMotDePasse(password);
                    } else if (existingUser == null) {
                        AlertUtils.showError("Erreur", "Le mot de passe est obligatoire");
                        return null;
                    }

                    if (role == Role.APPRENANT) {
                        user.setTypePermis(typePermisCombo.getValue());
                        user.setNiveau(niveauCombo.getValue());

                        // Sauvegarder les heures prévues
                        user.setHeuresPreveuesCode(heuresCodeSpinner.getValue());
                        user.setHeuresPreveuesConduite(heuresConduiteSpinner.getValue());
                        user.setHeuresPreveuesParc(heuresParcSpinner.getValue());
                        user.setFraisInscriptionPaye(fraisInscriptionCheck.isSelected());
                    }

                    return user;
                } catch (Exception e) {
                    AlertUtils.showError("Erreur", "Données invalides: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }
}