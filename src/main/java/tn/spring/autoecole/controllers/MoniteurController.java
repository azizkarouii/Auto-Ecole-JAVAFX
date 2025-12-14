package tn.spring.autoecole.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.spring.autoecole.HelloApplication;
import tn.spring.autoecole.models.*;
import tn.spring.autoecole.models.enums.*;
import tn.spring.autoecole.services.*;
import tn.spring.autoecole.utils.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MoniteurController {

    @FXML private Label welcomeLabel;

    // Tab Séances
    @FXML private TableView<Seance> seancesTable;
    @FXML private TableColumn<Seance, Integer> seanceIdCol;
    @FXML private TableColumn<Seance, String> seanceTypeCol;
    @FXML private TableColumn<Seance, String> seanceDateCol;
    @FXML private TableColumn<Seance, String> seanceHeureCol;
    @FXML private TableColumn<Seance, String> seanceApprenantCol;
    @FXML private TableColumn<Seance, String> seanceVehiculeCol;
    @FXML private TableColumn<Seance, String> seanceStatutCol;

    // Tab Apprenants
    @FXML private TableView<User> apprenantsTable;
    @FXML private TableColumn<User, Integer> apprenantIdCol;
    @FXML private TableColumn<User, String> apprenantNomCol;
    @FXML private TableColumn<User, String> apprenantPrenomCol;
    @FXML private TableColumn<User, String> apprenantCinCol;
    @FXML private TableColumn<User, String> apprenantEmailCol;
    @FXML private TableColumn<User, String> apprenantTelCol;
    @FXML private TableColumn<User, String> apprenantTypePermisCol;
    @FXML private TableColumn<User, String> apprenantNiveauCol;
    @FXML private TextField searchApprenantField;
    @FXML private ComboBox<Niveau> filterNiveauCombo;

    // Tab Véhicules
    @FXML private TableView<Vehicule> vehiculesTable;
    @FXML private TableColumn<Vehicule, Integer> vehiculeIdCol;
    @FXML private TableColumn<Vehicule, String> vehiculeMatriculeCol;
    @FXML private TableColumn<Vehicule, String> vehiculeMarqueCol;
    @FXML private TableColumn<Vehicule, String> vehiculeModeleCol;
    @FXML private TableColumn<Vehicule, String> vehiculeTypeCol;
    @FXML private TableColumn<Vehicule, Boolean> vehiculeDispoCol;
    @FXML private ComboBox<TypePermis> filterVehiculeTypeCombo;

    private final UserService userService;
    private final VehiculeService vehiculeService;
    private final SeanceService seanceService;
    private final AuthService authService;

    private ObservableList<Seance> seancesData;
    private ObservableList<User> apprenantsData;
    private ObservableList<Vehicule> vehiculesData;

    public MoniteurController() {
        this.userService = new UserService();
        this.vehiculeService = new VehiculeService();
        this.seanceService = new SeanceService();
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        setupWelcomeLabel();
        setupSeancesTab();
        setupApprenantsTab();
        setupVehiculesTab();
        loadAllData();
    }

    private void setupWelcomeLabel() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getNomComplet() + " (Moniteur)");
        }
    }

    private void setupSeancesTab() {
        seanceIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        seanceTypeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getType().getDisplayName()
                )
        );
        seanceDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateFormatee()
                )
        );
        seanceHeureCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getHeureFormatee()
                )
        );
        seanceApprenantCol.setCellValueFactory(new PropertyValueFactory<>("apprenantNom"));
        seanceVehiculeCol.setCellValueFactory(new PropertyValueFactory<>("vehiculeInfo"));
        seanceStatutCol.setCellValueFactory(cellData -> {
            Seance seance = cellData.getValue();
            String statut = seance.isPassed() ? "Passée" :
                    seance.isToday() ? "Aujourd'hui" : "À venir";
            return new javafx.beans.property.SimpleStringProperty(statut);
        });
    }

    private void setupApprenantsTab() {
        apprenantIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        apprenantNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        apprenantPrenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        apprenantCinCol.setCellValueFactory(new PropertyValueFactory<>("cin"));
        apprenantEmailCol.setCellValueFactory(new PropertyValueFactory<>("mail"));
        apprenantTelCol.setCellValueFactory(new PropertyValueFactory<>("numTel"));
        apprenantTypePermisCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTypePermis() != null ?
                                cellData.getValue().getTypePermis().getDisplayName() : "N/A"
                )
        );
        apprenantNiveauCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getNiveau() != null ?
                                cellData.getValue().getNiveau().getDisplayName() : "N/A"
                )
        );

        filterNiveauCombo.getItems().add(null);
        filterNiveauCombo.getItems().addAll(Niveau.values());
        filterNiveauCombo.setPromptText("Tous les niveaux");
    }

    private void setupVehiculesTab() {
        vehiculeIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        vehiculeMatriculeCol.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        vehiculeMarqueCol.setCellValueFactory(new PropertyValueFactory<>("marque"));
        vehiculeModeleCol.setCellValueFactory(new PropertyValueFactory<>("modele"));
        vehiculeTypeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getType().getDisplayName()
                )
        );
        vehiculeDispoCol.setCellValueFactory(new PropertyValueFactory<>("disponible"));

        filterVehiculeTypeCombo.getItems().add(null);
        filterVehiculeTypeCombo.getItems().addAll(TypePermis.values());
        filterVehiculeTypeCombo.setPromptText("Tous les types");
    }

    private void loadAllData() {
        loadSeances();
        loadApprenants();
        loadVehicules();
    }

    @FXML
    private void loadSeances() {
        try {
            User currentUser = Session.getInstance().getCurrentUser();
            if (currentUser != null) {
                List<Seance> seances = seanceService.getSeancesByMoniteur(currentUser.getId());
                seancesData = FXCollections.observableArrayList(seances);
                seancesTable.setItems(seancesData);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les séances: " + e.getMessage());
        }
    }

    @FXML
    private void loadApprenants() {
        try {
            Niveau niveauFilter = filterNiveauCombo.getValue();
            List<User> apprenants = userService.getApprenantsFiltered(null, niveauFilter);
            apprenantsData = FXCollections.observableArrayList(apprenants);
            apprenantsTable.setItems(apprenantsData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les apprenants: " + e.getMessage());
        }
    }

    @FXML
    private void searchApprenants() {
        String query = searchApprenantField.getText();
        try {
            List<User> apprenants = userService.getAllApprenants().stream()
                    .filter(u -> query == null || query.isEmpty() ||
                            u.getNom().toLowerCase().contains(query.toLowerCase()) ||
                            u.getPrenom().toLowerCase().contains(query.toLowerCase()) ||
                            u.getCin().contains(query))
                    .toList();
            apprenantsData = FXCollections.observableArrayList(apprenants);
            apprenantsTable.setItems(apprenantsData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    @FXML
    private void loadVehicules() {
        try {
            TypePermis typeFilter = filterVehiculeTypeCombo.getValue();
            List<Vehicule> vehicules = typeFilter != null ?
                    vehiculeService.getVehiculesByType(typeFilter) :
                    vehiculeService.getAllVehicules();

            vehiculesData = FXCollections.observableArrayList(vehicules);
            vehiculesTable.setItems(vehiculesData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les véhicules: " + e.getMessage());
        }
    }

    @FXML
    private void viewApprenantDetails() {
        User selected = apprenantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un apprenant");
            return;
        }

        String details = "Détails de l'apprenant:\n\n" +
                "Nom: " + selected.getNomComplet() + "\n" +
                "CIN: " + selected.getCin() + "\n" +
                "Email: " + selected.getMail() + "\n" +
                "Téléphone: " + selected.getNumTel() + "\n" +
                "Type de Permis: " + (selected.getTypePermis() != null ? selected.getTypePermis().getDisplayName() : "N/A") + "\n" +
                "Niveau: " + (selected.getNiveau() != null ? selected.getNiveau().getDisplayName() : "N/A");

        AlertUtils.showInfo("Parcours de l'apprenant", details);
    }

    @FXML
    private void handleLogout() {
        if (AlertUtils.confirmLogout()) {
            authService.logout();
            try {
                HelloApplication.showLoginView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}