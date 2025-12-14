package tn.spring.autoecole.utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import tn.spring.autoecole.models.Vehicule;
import tn.spring.autoecole.models.enums.TypePermis;

import java.util.Optional;

public class VehiculeDialogUtil {

    public static Optional<Vehicule> showAddVehiculeDialog() {
        return showVehiculeDialog(null, "Ajouter un Véhicule");
    }

    public static Optional<Vehicule> showEditVehiculeDialog(Vehicule vehicule) {
        return showVehiculeDialog(vehicule, "Modifier le Véhicule");
    }

    private static Optional<Vehicule> showVehiculeDialog(Vehicule existingVehicule, String title) {
        Dialog<Vehicule> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(existingVehicule == null ?
                "Veuillez remplir tous les champs" :
                "Modifiez les informations du véhicule");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField matriculeField = new TextField();
        matriculeField.setPromptText("Matricule (ex: 1234 TU 5678)");
        TextField marqueField = new TextField();
        marqueField.setPromptText("Marque");
        TextField modeleField = new TextField();
        modeleField.setPromptText("Modèle");

        ComboBox<TypePermis> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TypePermis.values());
        typeCombo.setPromptText("Type de véhicule");

        CheckBox disponibleCheck = new CheckBox();
        disponibleCheck.setSelected(true);

        if (existingVehicule != null) {
            matriculeField.setText(existingVehicule.getMatricule());
            marqueField.setText(existingVehicule.getMarque());
            modeleField.setText(existingVehicule.getModele());
            typeCombo.setValue(existingVehicule.getType());
            disponibleCheck.setSelected(existingVehicule.isDisponible());
        }

        grid.add(new Label("Matricule:"), 0, 0);
        grid.add(matriculeField, 1, 0);
        grid.add(new Label("Marque:"), 0, 1);
        grid.add(marqueField, 1, 1);
        grid.add(new Label("Modèle:"), 0, 2);
        grid.add(modeleField, 1, 2);
        grid.add(new Label("Type:"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Disponible:"), 0, 4);
        grid.add(disponibleCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String matricule = matriculeField.getText().trim();
                    String marque = marqueField.getText().trim();
                    String modele = modeleField.getText().trim();
                    TypePermis type = typeCombo.getValue();
                    boolean disponible = disponibleCheck.isSelected();

                    if (matricule.isEmpty() || marque.isEmpty() || modele.isEmpty() || type == null) {
                        AlertUtils.showError("Erreur", "Tous les champs sont obligatoires");
                        return null;
                    }

                    Vehicule vehicule = existingVehicule != null ? existingVehicule : new Vehicule();
                    vehicule.setMatricule(matricule);
                    vehicule.setMarque(marque);
                    vehicule.setModele(modele);
                    vehicule.setType(type);
                    vehicule.setDisponible(disponible);

                    return vehicule;
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