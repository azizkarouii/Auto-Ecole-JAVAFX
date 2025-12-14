package tn.spring.autoecole.utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import tn.spring.autoecole.models.Examen;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.enums.Niveau;
import tn.spring.autoecole.models.enums.ResultatExamen;
import tn.spring.autoecole.models.enums.TypeSeance;
import tn.spring.autoecole.services.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class ExamenDialogUtil {

    private static final UserService userService = new UserService();

    public static Optional<Examen> showAddExamenDialog() {
        return showExamenDialog(null, "Programmer un Examen");
    }

    public static Optional<Examen> showEditExamenDialog(Examen examen) {
        return showExamenDialog(examen, "Modifier l'Examen");
    }

    public static Optional<ResultatExamen> showResultatDialog(Examen examen) {
        Dialog<ResultatExamen> dialog = new Dialog<>();
        dialog.setTitle("Saisir le Résultat");
        dialog.setHeaderText("Examen " + examen.getType().getDisplayName() +
                "\nApprenant: " + examen.getApprenantNom() +
                "\nDate: " + examen.getDateFormatee());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<ResultatExamen> resultatCombo = new ComboBox<>();
        resultatCombo.getItems().addAll(
                ResultatExamen.REUSSI,
                ResultatExamen.ECHEC
        );
        resultatCombo.setPromptText("Sélectionner le résultat");
        resultatCombo.setPrefWidth(200);

        if (examen.getResultat() != null && examen.getResultat() != ResultatExamen.EN_ATTENTE) {
            resultatCombo.setValue(examen.getResultat());
        }

        grid.add(new Label("Résultat:"), 0, 0);
        grid.add(resultatCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                ResultatExamen resultat = resultatCombo.getValue();
                if (resultat == null) {
                    AlertUtils.showError("Erreur", "Veuillez sélectionner un résultat");
                    return null;
                }
                return resultat;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static Optional<Examen> showExamenDialog(Examen existingExamen, String title) {
        Dialog<Examen> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(existingExamen == null ?
                "Veuillez remplir tous les champs" :
                "Modifiez les informations de l'examen");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Type d'examen
        ComboBox<TypeSeance> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TypeSeance.values());
        typeCombo.setPromptText("Type d'examen");

        // Date
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date de l'examen");
        datePicker.setValue(LocalDate.now().plusDays(7)); // Par défaut dans 7 jours

        // Heure
        ComboBox<String> heureCombo = new ComboBox<>();
        heureCombo.getItems().addAll("08:00", "09:00", "10:00", "11:00",
                "13:00", "14:00", "15:00", "16:00");
        heureCombo.setPromptText("Heure");

        // Apprenant
        ComboBox<User> apprenantCombo = new ComboBox<>();
        apprenantCombo.setPromptText("Sélectionner un apprenant");

        // Charger initialement tous les apprenants
        try {
            List<User> apprenants = userService.getAllApprenants();
            apprenants.forEach(a -> apprenantCombo.getItems().add(a));
            apprenantCombo.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText("");
                    } else {
                        setText(user.getNomComplet() + " - Niveau: " +
                                (user.getNiveau() != null ? user.getNiveau().getDisplayName() : "N/A"));
                    }
                }
            });
            apprenantCombo.setButtonCell(new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null ? "" : user.getNomComplet());
                }
            });
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les apprenants: " + e.getMessage());
        }

        // Filtrer les apprenants selon le type d'examen sélectionné
        typeCombo.setOnAction(e -> {
            TypeSeance selectedType = typeCombo.getValue();
            if (selectedType != null) {
                try {
                    List<User> allApprenants = userService.getAllApprenants();
                    apprenantCombo.getItems().clear();

                    // Filtrer selon le niveau correspondant au type d'examen
                    Niveau niveauRequis = selectedType.getCorrespondingNiveau();
                    List<User> apprenantsFiltered = allApprenants.stream()
                            .filter(a -> a.getNiveau() == niveauRequis)
                            .toList();

                    apprenantCombo.getItems().addAll(apprenantsFiltered);

                    if (apprenantsFiltered.isEmpty()) {
                        AlertUtils.showWarning("Aucun apprenant",
                                "Aucun apprenant n'est au niveau " + niveauRequis.getDisplayName() +
                                        " pour passer cet examen.");
                    }
                } catch (SQLException ex) {
                    AlertUtils.showError("Erreur", "Impossible de filtrer les apprenants: " + ex.getMessage());
                }
            }
        });

        // Résultat (seulement en modification)
        ComboBox<ResultatExamen> resultatCombo = new ComboBox<>();
        resultatCombo.getItems().addAll(ResultatExamen.values());
        resultatCombo.setPromptText("Résultat");
        resultatCombo.setValue(ResultatExamen.EN_ATTENTE);

        // Pré-remplir si modification
        if (existingExamen != null) {
            typeCombo.setValue(existingExamen.getType());
            datePicker.setValue(existingExamen.getDateExamen());
            heureCombo.setValue(existingExamen.getHeureExamen().toString());
            resultatCombo.setValue(existingExamen.getResultat());

            // Trouver et sélectionner l'apprenant
            apprenantCombo.getItems().stream()
                    .filter(a -> a.getId() == existingExamen.getApprenantId())
                    .findFirst()
                    .ifPresent(apprenantCombo::setValue);
        }

        // Construction de la grille
        int row = 0;
        grid.add(new Label("Type d'examen:"), 0, row);
        grid.add(typeCombo, 1, row++);
        grid.add(new Label("Date:"), 0, row);
        grid.add(datePicker, 1, row++);
        grid.add(new Label("Heure:"), 0, row);
        grid.add(heureCombo, 1, row++);
        grid.add(new Label("Apprenant:"), 0, row);
        grid.add(apprenantCombo, 1, row++);

        if (existingExamen != null) {
            grid.add(new Label("Résultat:"), 0, row);
            grid.add(resultatCombo, 1, row++);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    TypeSeance type = typeCombo.getValue();
                    LocalDate date = datePicker.getValue();
                    String heureStr = heureCombo.getValue();
                    User apprenant = apprenantCombo.getValue();
                    ResultatExamen resultat = resultatCombo.getValue();

                    // Validations
                    if (type == null) {
                        AlertUtils.showError("Erreur", "Le type d'examen est obligatoire");
                        return null;
                    }
                    if (date == null) {
                        AlertUtils.showError("Erreur", "La date est obligatoire");
                        return null;
                    }
                    if (heureStr == null || heureStr.isEmpty()) {
                        AlertUtils.showError("Erreur", "L'heure est obligatoire");
                        return null;
                    }
                    if (apprenant == null) {
                        AlertUtils.showError("Erreur", "L'apprenant est obligatoire");
                        return null;
                    }

                    LocalTime heure = LocalTime.parse(heureStr);

                    Examen examen = existingExamen != null ? existingExamen : new Examen();
                    examen.setType(type);
                    examen.setDateExamen(date);
                    examen.setHeureExamen(heure);
                    examen.setApprenantId(apprenant.getId());
                    examen.setResultat(resultat != null ? resultat : ResultatExamen.EN_ATTENTE);

                    return examen;
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