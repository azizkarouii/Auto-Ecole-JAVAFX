package tn.spring.autoecole.utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import tn.spring.autoecole.models.Seance;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.Vehicule;
import tn.spring.autoecole.models.enums.Niveau;
import tn.spring.autoecole.models.enums.TypeSeance;
import tn.spring.autoecole.services.UserService;
import tn.spring.autoecole.services.VehiculeService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class SeanceDialogUtil {

    private static final UserService userService = new UserService();
    private static final VehiculeService vehiculeService = new VehiculeService();

    public static Optional<Seance> showAddSeanceDialog() {
        return showSeanceDialog(null, "Ajouter une Séance");
    }

    public static Optional<Seance> showEditSeanceDialog(Seance seance) {
        return showSeanceDialog(seance, "Modifier la Séance");
    }

    private static Optional<Seance> showSeanceDialog(Seance existingSeance, String title) {
        Dialog<Seance> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(existingSeance == null ?
                "Veuillez remplir tous les champs" :
                "Modifiez les informations de la séance");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Type de séance
        ComboBox<TypeSeance> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TypeSeance.values());
        typeCombo.setPromptText("Type de séance");

        // Date
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date de la séance");
        datePicker.setValue(LocalDate.now().plusDays(1)); // Par défaut demain

        // Heure début
        ComboBox<String> heureDebutCombo = new ComboBox<>();
        for (int h = 8; h <= 18; h++) {
            heureDebutCombo.getItems().add(String.format("%02d:00", h));
            if (h < 18) {
                heureDebutCombo.getItems().add(String.format("%02d:30", h));
            }
        }
        heureDebutCombo.setPromptText("Heure de début");

        // Heure fin
        ComboBox<String> heureFinCombo = new ComboBox<>();
        for (int h = 8; h <= 19; h++) {
            heureFinCombo.getItems().add(String.format("%02d:00", h));
            if (h < 19) {
                heureFinCombo.getItems().add(String.format("%02d:30", h));
            }
        }
        heureFinCombo.setPromptText("Heure de fin");

        // Label pour afficher la durée
        Label dureeLabel = new Label("Durée: --");
        dureeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

        // Calculer la durée automatiquement
        Runnable updateDuree = () -> {
            String debutStr = heureDebutCombo.getValue();
            String finStr = heureFinCombo.getValue();
            if (debutStr != null && finStr != null) {
                try {
                    LocalTime debut = LocalTime.parse(debutStr);
                    LocalTime fin = LocalTime.parse(finStr);
                    if (fin.isAfter(debut)) {
                        long minutes = java.time.Duration.between(debut, fin).toMinutes();
                        long heures = minutes / 60;
                        long mins = minutes % 60;
                        dureeLabel.setText(String.format("Durée: %dh%02d", heures, mins));
                        dureeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                    } else {
                        dureeLabel.setText("Durée: Heure fin doit être après heure début");
                        dureeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                    }
                } catch (Exception e) {
                    dureeLabel.setText("Durée: --");
                }
            }
        };

        heureDebutCombo.setOnAction(e -> updateDuree.run());
        heureFinCombo.setOnAction(e -> updateDuree.run());

        // Apprenant
        ComboBox<User> apprenantCombo = new ComboBox<>();
        apprenantCombo.setPromptText("Sélectionner un apprenant");
        apprenantCombo.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText("");
                } else {
                    setText(user.getNomComplet() + " - Niveau: " +
                            (user.getNiveau() != null ? user.getNiveau().getDisplayName() : "N/A") +
                            " - " + (user.getTypePermis() != null ? user.getTypePermis().getDisplayName() : "N/A"));
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

        // Charger initialement tous les apprenants
        try {
            List<User> apprenants = userService.getAllApprenants();
            apprenants.forEach(a -> apprenantCombo.getItems().add(a));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les apprenants: " + e.getMessage());
        }

        // Moniteur
        ComboBox<User> moniteurCombo = new ComboBox<>();
        moniteurCombo.setPromptText("Sélectionner un moniteur");
        // Ajouter l'option "Aucun"
        moniteurCombo.getItems().add(null);
        try {
            List<User> moniteurs = userService.getAllMoniteurs();
            moniteurs.forEach(m -> moniteurCombo.getItems().add(m));
            moniteurCombo.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty) {
                        setText("");
                    } else if (user == null) {
                        setText("-- Aucun --");
                    } else {
                        setText(user.getNomComplet());
                    }
                }
            });
            moniteurCombo.setButtonCell(new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty) {
                        setText("Sélectionner un moniteur");
                    } else if (user == null) {
                        setText("Aucun");
                    } else {
                        setText(user.getNomComplet());
                    }
                }
            });
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les moniteurs: " + e.getMessage());
        }

        // Véhicule
        ComboBox<Vehicule> vehiculeCombo = new ComboBox<>();
        vehiculeCombo.setPromptText("Sélectionner un véhicule");
        // Ajouter l'option "Aucun"
        vehiculeCombo.getItems().add(null);
        vehiculeCombo.setCellFactory(lv -> new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setText("");
                } else if (v == null) {
                    setText("-- Aucun --");
                } else {
                    setText(v.getMatricule() + " - " + v.getMarque() + " " + v.getModele() +
                            " [" + v.getType().getDisplayName() + "]");
                }
            }
        });
        vehiculeCombo.setButtonCell(new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setText("Sélectionner un véhicule");
                } else if (v == null) {
                    setText("Aucun");
                } else {
                    setText(v.getMatricule() + " - " + v.getMarque() + " " + v.getModele());
                }
            }
        });

        // Logique pour afficher/masquer moniteur et véhicule selon le type
        typeCombo.setOnAction(e -> {
            TypeSeance selectedType = typeCombo.getValue();
            boolean needsMoniteurAndVehicule = selectedType != null &&
                    (selectedType == TypeSeance.CONDUITE || selectedType == TypeSeance.PARC);

            moniteurCombo.setDisable(!needsMoniteurAndVehicule);
            vehiculeCombo.setDisable(!needsMoniteurAndVehicule);

            if (!needsMoniteurAndVehicule) {
                moniteurCombo.setValue(null);
                vehiculeCombo.setValue(null);
            }

            // Filtrer les apprenants selon le type de séance sélectionné
            if (selectedType != null) {
                try {
                    List<User> allApprenants = userService.getAllApprenants();
                    apprenantCombo.getItems().clear();

                    // Filtrer selon le niveau correspondant au type de séance
                    Niveau niveauRequis = selectedType.getCorrespondingNiveau();
                    List<User> apprenantsFiltered = allApprenants.stream()
                            .filter(a -> a.getNiveau() == niveauRequis)
                            .toList();

                    apprenantCombo.getItems().addAll(apprenantsFiltered);

                    if (apprenantsFiltered.isEmpty()) {
                        AlertUtils.showWarning("Aucun apprenant",
                                "Aucun apprenant n'est au niveau " + niveauRequis.getDisplayName() +
                                        " pour cette séance.");
                    }
                } catch (SQLException ex) {
                    AlertUtils.showError("Erreur", "Impossible de filtrer les apprenants: " + ex.getMessage());
                }
            }
        });

        // Charger les véhicules disponibles selon l'apprenant sélectionné
        apprenantCombo.setOnAction(e -> {
            User selectedApprenant = apprenantCombo.getValue();
            if (selectedApprenant != null && selectedApprenant.getTypePermis() != null) {
                try {
                    List<Vehicule> vehicules = vehiculeService.getAvailableVehiculesByType(
                            selectedApprenant.getTypePermis());
                    vehiculeCombo.getItems().clear();
                    vehiculeCombo.getItems().add(null); // Option "Aucun"
                    vehicules.forEach(v -> vehiculeCombo.getItems().add(v));
                } catch (SQLException ex) {
                    AlertUtils.showError("Erreur", "Impossible de charger les véhicules: " + ex.getMessage());
                }
            }
        });

        // Pré-remplir si modification
        if (existingSeance != null) {
            typeCombo.setValue(existingSeance.getType());
            datePicker.setValue(existingSeance.getDateSeance());
            if (existingSeance.getHeureDebut() != null) {
                heureDebutCombo.setValue(existingSeance.getHeureDebut().toString());
            }
            if (existingSeance.getHeureFin() != null) {
                heureFinCombo.setValue(existingSeance.getHeureFin().toString());
            }
            updateDuree.run();

            // Trouver et sélectionner l'apprenant
            apprenantCombo.getItems().stream()
                    .filter(a -> a.getId() == existingSeance.getApprenantId())
                    .findFirst()
                    .ifPresent(apprenant -> {
                        apprenantCombo.setValue(apprenant);
                        // Charger les véhicules pour cet apprenant
                        if (apprenant.getTypePermis() != null) {
                            try {
                                List<Vehicule> vehicules = vehiculeService.getAvailableVehiculesByType(
                                        apprenant.getTypePermis());
                                vehiculeCombo.getItems().clear();
                                vehiculeCombo.getItems().add(null);
                                vehicules.forEach(v -> vehiculeCombo.getItems().add(v));
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

            // Configurer moniteur et véhicule selon le type
            TypeSeance existingType = existingSeance.getType();
            boolean needsMoniteurAndVehicule = existingType == TypeSeance.CONDUITE ||
                    existingType == TypeSeance.PARC;

            moniteurCombo.setDisable(!needsMoniteurAndVehicule);
            vehiculeCombo.setDisable(!needsMoniteurAndVehicule);

            // Trouver et sélectionner le moniteur si présent
            if (existingSeance.getMoniteurId() != null) {
                moniteurCombo.getItems().stream()
                        .filter(m -> m != null && m.getId() == existingSeance.getMoniteurId())
                        .findFirst()
                        .ifPresent(moniteurCombo::setValue);
            } else {
                moniteurCombo.setValue(null);
            }

            // Trouver et sélectionner le véhicule si présent
            if (existingSeance.getVehiculeId() != null) {
                try {
                    Optional<Vehicule> vehiculeOpt = vehiculeService.getVehiculeById(existingSeance.getVehiculeId());
                    if (vehiculeOpt.isPresent()) {
                        // S'assurer que le véhicule est dans la liste
                        Vehicule vehicule = vehiculeOpt.get();
                        if (!vehiculeCombo.getItems().contains(vehicule)) {
                            vehiculeCombo.getItems().add(vehicule);
                        }
                        vehiculeCombo.setValue(vehicule);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                vehiculeCombo.setValue(null);
            }
        }

        // Construction de la grille
        int row = 0;
        grid.add(new Label("Type de séance:"), 0, row);
        grid.add(typeCombo, 1, row++);
        grid.add(new Label("Date:"), 0, row);
        grid.add(datePicker, 1, row++);
        grid.add(new Label("Heure de début:"), 0, row);
        grid.add(heureDebutCombo, 1, row++);
        grid.add(new Label("Heure de fin:"), 0, row);
        grid.add(heureFinCombo, 1, row++);
        grid.add(new Label(""), 0, row);
        grid.add(dureeLabel, 1, row++);
        grid.add(new Label("Apprenant:"), 0, row);
        grid.add(apprenantCombo, 1, row++);
        grid.add(new Label("Moniteur:"), 0, row);
        grid.add(moniteurCombo, 1, row++);
        grid.add(new Label("Véhicule:"), 0, row);
        grid.add(vehiculeCombo, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    TypeSeance type = typeCombo.getValue();
                    LocalDate date = datePicker.getValue();
                    String heureDebutStr = heureDebutCombo.getValue();
                    String heureFinStr = heureFinCombo.getValue();
                    User apprenant = apprenantCombo.getValue();
                    User moniteur = moniteurCombo.getValue();
                    Vehicule vehicule = vehiculeCombo.getValue();

                    // Validations
                    if (type == null) {
                        AlertUtils.showError("Erreur", "Le type de séance est obligatoire");
                        return null;
                    }
                    if (date == null) {
                        AlertUtils.showError("Erreur", "La date est obligatoire");
                        return null;
                    }
                    if (heureDebutStr == null || heureDebutStr.isEmpty()) {
                        AlertUtils.showError("Erreur", "L'heure de début est obligatoire");
                        return null;
                    }
                    if (heureFinStr == null || heureFinStr.isEmpty()) {
                        AlertUtils.showError("Erreur", "L'heure de fin est obligatoire");
                        return null;
                    }
                    if (apprenant == null) {
                        AlertUtils.showError("Erreur", "L'apprenant est obligatoire");
                        return null;
                    }

                    LocalTime heureDebut = LocalTime.parse(heureDebutStr);
                    LocalTime heureFin = LocalTime.parse(heureFinStr);

                    // Vérifier que l'heure de fin est après l'heure de début
                    if (!heureFin.isAfter(heureDebut)) {
                        AlertUtils.showError("Erreur", "L'heure de fin doit être après l'heure de début");
                        return null;
                    }

                    // Vérifier moniteur et véhicule si nécessaire
                    if (type == TypeSeance.CONDUITE || type == TypeSeance.PARC) {
                        if (moniteur == null) {
                            AlertUtils.showError("Erreur", "Un moniteur est obligatoire pour ce type de séance");
                            return null;
                        }
                        if (vehicule == null) {
                            AlertUtils.showError("Erreur", "Un véhicule est obligatoire pour ce type de séance");
                            return null;
                        }
                    }

                    Seance seance = existingSeance != null ? existingSeance : new Seance();
                    seance.setType(type);
                    seance.setDateSeance(date);
                    seance.setHeureDebut(heureDebut);
                    seance.setHeureFin(heureFin);
                    seance.setApprenantId(apprenant.getId());
                    seance.setMoniteurId(moniteur != null ? moniteur.getId() : null);
                    seance.setVehiculeId(vehicule != null ? vehicule.getId() : null);

                    return seance;
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