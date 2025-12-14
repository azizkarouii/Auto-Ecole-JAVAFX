package tn.spring.autoecole.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import tn.spring.autoecole.HelloApplication;
import tn.spring.autoecole.models.*;
import tn.spring.autoecole.models.enums.*;
import tn.spring.autoecole.services.*;
import tn.spring.autoecole.utils.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminController {

    @FXML private Label welcomeLabel;

    // Dashboard Stats
    @FXML private Label totalApprenantsLabel;
    @FXML private Label totalMoniteursLabel;
    @FXML private Label totalVehiculesLabel;
    @FXML private Label totalSeancesLabel;
    @FXML private Label totalExamensLabel;
    @FXML private Label tauxReussiteLabel;
    @FXML private Label apprenantsNiveauCodeLabel;
    @FXML private Label apprenantsNiveauConduiteLabel;
    @FXML private Label apprenantsNiveauParcLabel;
    @FXML private Label apprenantsPermisObtenusLabel;

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
    @FXML private ComboBox<TypePermis> filterTypePermisCombo;
    @FXML private ComboBox<Niveau> filterNiveauCombo;

    // Tab Moniteurs
    @FXML private TableView<User> moniteursTable;
    @FXML private TableColumn<User, Integer> moniteurIdCol;
    @FXML private TableColumn<User, String> moniteurNomCol;
    @FXML private TableColumn<User, String> moniteurPrenomCol;
    @FXML private TableColumn<User, String> moniteurCinCol;
    @FXML private TableColumn<User, String> moniteurEmailCol;
    @FXML private TableColumn<User, String> moniteurTelCol;

    // Tab Véhicules
    @FXML private TableView<Vehicule> vehiculesTable;
    @FXML private TableColumn<Vehicule, Integer> vehiculeIdCol;
    @FXML private TableColumn<Vehicule, String> vehiculeMatriculeCol;
    @FXML private TableColumn<Vehicule, String> vehiculeMarqueCol;
    @FXML private TableColumn<Vehicule, String> vehiculeModeleCol;
    @FXML private TableColumn<Vehicule, String> vehiculeTypeCol;
    @FXML private TableColumn<Vehicule, Boolean> vehiculeDispoCol;
    @FXML private ComboBox<TypePermis> filterVehiculeTypeCombo;

    // Tab Séances
    @FXML private TableView<Seance> seancesTable;
    @FXML private TableColumn<Seance, Integer> seanceIdCol;
    @FXML private TableColumn<Seance, String> seanceTypeCol;
    @FXML private TableColumn<Seance, String> seanceDateCol;
    @FXML private TableColumn<Seance, String> seanceHeureCol;
    @FXML private TableColumn<Seance, String> seanceApprenantCol;
    @FXML private TableColumn<Seance, String> seanceMoniteurCol;
    @FXML private TableColumn<Seance, String> seanceVehiculeCol;

    // Tab Examens
    @FXML private TableView<Examen> examensTable;
    @FXML private TableColumn<Examen, Integer> examenIdCol;
    @FXML private TableColumn<Examen, String> examenTypeCol;
    @FXML private TableColumn<Examen, String> examenDateCol;
    @FXML private TableColumn<Examen, String> examenHeureCol;
    @FXML private TableColumn<Examen, String> examenApprenantCol;
    @FXML private TableColumn<Examen, String> examenResultatCol;
    @FXML private ComboBox<ResultatExamen> filterResultatCombo;

    // Tab Finances
    @FXML private Label revenuInscriptionsLabel;
    @FXML private Label revenuCodeLabel;
    @FXML private Label revenuConduiteLabel;
    @FXML private Label revenuParcLabel;
    @FXML private Label revenuExamensLabel;
    @FXML private Label revenuAutoEcoleLabel;
    @FXML private Label revenuServiceMinesLabel;
    @FXML private Label revenuTotalLabel;
    @FXML private ComboBox<User> apprenantFinanceCombo;
    @FXML private VBox detailsApprenantBox;

    private final UserService userService;
    private final VehiculeService vehiculeService;
    private final SeanceService seanceService;
    private final ExamenService examenService;
    private final FinanceService financeService;
    private final AuthService authService;

    private ObservableList<User> apprenantsData;
    private ObservableList<User> moniteursData;
    private ObservableList<Vehicule> vehiculesData;
    private ObservableList<Seance> seancesData;
    private ObservableList<Examen> examensData;

    public AdminController() {
        this.userService = new UserService();
        this.vehiculeService = new VehiculeService();
        this.seanceService = new SeanceService();
        this.examenService = new ExamenService();
        this.financeService = new FinanceService();
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        setupWelcomeLabel();
        setupApprenantsTab();
        setupMoniteursTab();
        setupVehiculesTab();
        setupSeancesTab();
        setupExamensTab();
        setupFinancesTab();
        loadAllData();
        loadDashboardStats();
        loadFinances();
    }

    private void setupWelcomeLabel() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getNomComplet() + " (Administrateur)");
        }
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

        if (filterTypePermisCombo != null) {
            filterTypePermisCombo.getItems().add(null);
            filterTypePermisCombo.getItems().addAll(TypePermis.values());
            filterTypePermisCombo.setPromptText("Tous les types");
        }

        if (filterNiveauCombo != null) {
            filterNiveauCombo.getItems().add(null);
            filterNiveauCombo.getItems().addAll(Niveau.values());
            filterNiveauCombo.setPromptText("Tous les niveaux");
        }
    }

    private void setupMoniteursTab() {
        moniteurIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        moniteurNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        moniteurPrenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        moniteurCinCol.setCellValueFactory(new PropertyValueFactory<>("cin"));
        moniteurEmailCol.setCellValueFactory(new PropertyValueFactory<>("mail"));
        moniteurTelCol.setCellValueFactory(new PropertyValueFactory<>("numTel"));
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

        if (filterVehiculeTypeCombo != null) {
            filterVehiculeTypeCombo.getItems().add(null);
            filterVehiculeTypeCombo.getItems().addAll(TypePermis.values());
            filterVehiculeTypeCombo.setPromptText("Tous les types");
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
        seanceMoniteurCol.setCellValueFactory(new PropertyValueFactory<>("moniteurNom"));
        seanceVehiculeCol.setCellValueFactory(new PropertyValueFactory<>("vehiculeInfo"));
    }

    private void setupExamensTab() {
        if (examensTable == null) return;

        examenIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        examenTypeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getType().getIcon() + " " +
                                cellData.getValue().getType().getDisplayName()
                )
        );
        examenDateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateFormatee()
                )
        );
        examenHeureCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getHeureFormatee()
                )
        );
        examenApprenantCol.setCellValueFactory(new PropertyValueFactory<>("apprenantNom"));
        examenResultatCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getResultatComplet()
                )
        );

        if (filterResultatCombo != null) {
            filterResultatCombo.getItems().add(null);
            filterResultatCombo.getItems().addAll(ResultatExamen.values());
            filterResultatCombo.setPromptText("Tous les résultats");
        }
    }

    private void loadDashboardStats() {
        try {
            // Compter les utilisateurs
            long apprenants = userService.countByRole(Role.APPRENANT);
            long moniteurs = userService.countByRole(Role.MONITEUR);

            // Compter les véhicules
            long vehicules = vehiculeService.getAllVehicules().size();

            // Compter les séances
            long seances = seanceService.getAllSeances().size();

            // Compter les examens
            long examens = examenService.getAllExamens().size();

            // Taux de réussite
            double tauxReussite = examenService.getSuccessRate();

            // Apprenants par niveau
            List<User> allApprenants = userService.getAllApprenants();
            long niveauCode = allApprenants.stream().filter(a -> a.getNiveau() == Niveau.CODE).count();
            long niveauConduite = allApprenants.stream().filter(a -> a.getNiveau() == Niveau.CONDUITE).count();
            long niveauParc = allApprenants.stream().filter(a -> a.getNiveau() == Niveau.PARC).count();
            long permisObtenu = allApprenants.stream().filter(a -> a.getNiveau() == Niveau.OBTENU).count();

            // Mettre à jour les labels si ils existent
            if (totalApprenantsLabel != null) totalApprenantsLabel.setText(String.valueOf(apprenants));
            if (totalMoniteursLabel != null) totalMoniteursLabel.setText(String.valueOf(moniteurs));
            if (totalVehiculesLabel != null) totalVehiculesLabel.setText(String.valueOf(vehicules));
            if (totalSeancesLabel != null) totalSeancesLabel.setText(String.valueOf(seances));
            if (totalExamensLabel != null) totalExamensLabel.setText(String.valueOf(examens));
            if (tauxReussiteLabel != null) tauxReussiteLabel.setText(String.format("%.1f%%", tauxReussite));
            if (apprenantsNiveauCodeLabel != null) apprenantsNiveauCodeLabel.setText(String.valueOf(niveauCode));
            if (apprenantsNiveauConduiteLabel != null) apprenantsNiveauConduiteLabel.setText(String.valueOf(niveauConduite));
            if (apprenantsNiveauParcLabel != null) apprenantsNiveauParcLabel.setText(String.valueOf(niveauParc));
            if (apprenantsPermisObtenusLabel != null) apprenantsPermisObtenusLabel.setText(String.valueOf(permisObtenu));

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private void loadAllData() {
        loadApprenants();
        loadMoniteurs();
        loadVehicules();
        loadSeances();
        loadExamens();
    }

    @FXML
    private void loadApprenants() {
        try {
            TypePermis typeFilter = filterTypePermisCombo != null ? filterTypePermisCombo.getValue() : null;
            Niveau niveauFilter = filterNiveauCombo != null ? filterNiveauCombo.getValue() : null;

            List<User> apprenants = userService.getApprenantsFiltered(typeFilter, niveauFilter);
            apprenantsData = FXCollections.observableArrayList(apprenants);
            apprenantsTable.setItems(apprenantsData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les apprenants: " + e.getMessage());
        }
    }

    @FXML
    private void loadMoniteurs() {
        try {
            List<User> moniteurs = userService.getAllMoniteurs();
            moniteursData = FXCollections.observableArrayList(moniteurs);
            moniteursTable.setItems(moniteursData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les moniteurs: " + e.getMessage());
        }
    }

    @FXML
    private void loadVehicules() {
        try {
            TypePermis typeFilter = filterVehiculeTypeCombo != null ? filterVehiculeTypeCombo.getValue() : null;
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
    private void loadSeances() {
        try {
            List<Seance> seances = seanceService.getAllSeances();
            seancesData = FXCollections.observableArrayList(seances);
            seancesTable.setItems(seancesData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les séances: " + e.getMessage());
        }
    }

    @FXML
    private void loadExamens() {
        if (examensTable == null) return;

        try {
            ResultatExamen resultatFilter = filterResultatCombo != null ? filterResultatCombo.getValue() : null;
            List<Examen> examens = resultatFilter != null ?
                    examenService.getExamensByResultat(resultatFilter) :
                    examenService.getAllExamens();

            examensData = FXCollections.observableArrayList(examens);
            examensTable.setItems(examensData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les examens: " + e.getMessage());
        }
    }

    // ========== Gestion Apprenants ==========
    @FXML
    private void handleAddApprenant() {
        Optional<User> result = UserDialogUtil.showAddApprenantDialog();
        if (result.isPresent()) {
            try {
                userService.createUser(result.get());
                AlertUtils.showSuccess("Apprenant ajouté avec succès");
                loadApprenants();
                loadDashboardStats();
                refreshApprenantFinanceCombo();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditApprenant() {
        User selected = apprenantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un apprenant");
            return;
        }

        Optional<User> result = UserDialogUtil.showEditUserDialog(selected);
        if (result.isPresent()) {
            try {
                userService.updateUser(result.get());
                AlertUtils.showSuccess("Apprenant modifié avec succès");
                loadApprenants();
                loadDashboardStats();
                apprenantsTable.refresh();
                refreshApprenantFinanceCombo();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteApprenant() {
        User selected = apprenantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un apprenant");
            return;
        }

        if (AlertUtils.confirmDeletion(selected.getNomComplet())) {
            try {
                userService.deleteUser(selected.getId());
                AlertUtils.showSuccess("Apprenant supprimé avec succès");
                loadApprenants();
                loadDashboardStats();
                refreshApprenantFinanceCombo();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    // ========== Gestion Moniteurs ==========
    @FXML
    private void handleAddMoniteur() {
        Optional<User> result = UserDialogUtil.showAddMoniteurDialog();
        if (result.isPresent()) {
            try {
                userService.createUser(result.get());
                AlertUtils.showSuccess("Moniteur ajouté avec succès");
                loadMoniteurs();
                loadDashboardStats();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditMoniteur() {
        User selected = moniteursTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un moniteur");
            return;
        }

        Optional<User> result = UserDialogUtil.showEditUserDialog(selected);
        if (result.isPresent()) {
            try {
                userService.updateUser(result.get());
                AlertUtils.showSuccess("Moniteur modifié avec succès");
                loadMoniteurs();
                moniteursTable.refresh();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteMoniteur() {
        User selected = moniteursTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un moniteur");
            return;
        }

        if (AlertUtils.confirmDeletion(selected.getNomComplet())) {
            try {
                userService.deleteUser(selected.getId());
                AlertUtils.showSuccess("Moniteur supprimé avec succès");
                loadMoniteurs();
                loadDashboardStats();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    // ========== Gestion Véhicules ==========
    @FXML
    private void handleAddVehicule() {
        Optional<Vehicule> result = VehiculeDialogUtil.showAddVehiculeDialog();
        if (result.isPresent()) {
            try {
                vehiculeService.createVehicule(result.get());
                AlertUtils.showSuccess("Véhicule ajouté avec succès");
                loadVehicules();
                loadDashboardStats();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditVehicule() {
        Vehicule selected = vehiculesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un véhicule");
            return;
        }

        Optional<Vehicule> result = VehiculeDialogUtil.showEditVehiculeDialog(selected);
        if (result.isPresent()) {
            try {
                vehiculeService.updateVehicule(result.get());
                AlertUtils.showSuccess("Véhicule modifié avec succès");
                vehiculesTable.refresh();
                loadVehicules();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteVehicule() {
        Vehicule selected = vehiculesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un véhicule");
            return;
        }

        if (AlertUtils.confirmDeletion(selected.getMatricule())) {
            try {
                vehiculeService.deleteVehicule(selected.getId());
                AlertUtils.showSuccess("Véhicule supprimé avec succès");
                loadVehicules();
                loadDashboardStats();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    // ========== Gestion Séances ==========
    @FXML
    private void handleAddSeance() {
        Optional<Seance> result = SeanceDialogUtil.showAddSeanceDialog();
        if (result.isPresent()) {
            try {
                seanceService.createSeance(result.get());
                AlertUtils.showSuccess("Séance ajoutée avec succès");
                loadSeances();
                loadDashboardStats();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditSeance() {
        Seance selected = seancesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner une séance");
            return;
        }

        Optional<Seance> result = SeanceDialogUtil.showEditSeanceDialog(selected);
        if (result.isPresent()) {
            try {
                seanceService.updateSeance(result.get());
                AlertUtils.showSuccess("Séance modifiée avec succès");
                loadSeances();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteSeance() {
        Seance selected = seancesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner une séance");
            return;
        }

        if (AlertUtils.confirmDeletion("la séance du " + selected.getDateFormatee())) {
            try {
                seanceService.deleteSeance(selected.getId());
                AlertUtils.showSuccess("Séance supprimée avec succès");
                loadSeances();
                loadDashboardStats();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    // ========== Gestion Examens ==========
    @FXML
    private void handleAddExamen() {
        Optional<Examen> result = ExamenDialogUtil.showAddExamenDialog();
        if (result.isPresent()) {
            try {
                examenService.createExamen(result.get());
                AlertUtils.showSuccess("Examen programmé avec succès");
                loadExamens();
                loadDashboardStats();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditExamen() {
        if (examensTable == null) return;

        Examen selected = examensTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un examen");
            return;
        }

        Optional<Examen> result = ExamenDialogUtil.showEditExamenDialog(selected);
        if (result.isPresent()) {
            try {
                examenService.updateExamen(result.get());
                AlertUtils.showSuccess("Examen modifié avec succès");
                loadExamens();
                loadDashboardStats();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaisirResultat() {
        if (examensTable == null) return;

        Examen selected = examensTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un examen");
            return;
        }

        Optional<ResultatExamen> result = ExamenDialogUtil.showResultatDialog(selected);
        if (result.isPresent()) {
            try {
                examenService.updateResultatExamen(selected.getId(), result.get());
                AlertUtils.showSuccess("Résultat enregistré avec succès");
                loadExamens();
                loadApprenants(); // Rafraîchir car le niveau peut avoir changé
                loadDashboardStats();
            } catch (SQLException | IllegalArgumentException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteExamen() {
        if (examensTable == null) return;

        Examen selected = examensTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un examen");
            return;
        }

        if (AlertUtils.confirmDeletion("l'examen du " + selected.getDateFormatee())) {
            try {
                examenService.deleteExamen(selected.getId());
                AlertUtils.showSuccess("Examen supprimé avec succès");
                loadExamens();
                loadDashboardStats();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefreshStats() {
        loadDashboardStats();
        loadFinances();
        AlertUtils.showSuccess("Statistiques actualisées");
    }

    private void setupFinancesTab() {
        if (apprenantFinanceCombo != null) {
            try {
                List<User> apprenants = userService.getAllApprenants();
                apprenants.forEach(a -> apprenantFinanceCombo.getItems().add(a));
                apprenantFinanceCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        setText(empty || user == null ? "" : user.getNomComplet() + " - " +
                                (user.getNiveau() != null ? user.getNiveau().getDisplayName() : "N/A"));
                    }
                });
                apprenantFinanceCombo.setButtonCell(new javafx.scene.control.ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        setText(empty || user == null ? "" : user.getNomComplet());
                    }
                });
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de charger les apprenants: " + e.getMessage());
            }
        }
    }

    private void refreshApprenantFinanceCombo() {
        if (apprenantFinanceCombo != null) {
            try {
                // Sauvegarder la sélection actuelle
                User selectedUser = apprenantFinanceCombo.getValue();

                // Vider et recharger la liste
                apprenantFinanceCombo.getItems().clear();
                List<User> apprenants = userService.getAllApprenants();
                apprenantFinanceCombo.getItems().addAll(apprenants);

                // Restaurer la sélection si elle existe encore
                if (selectedUser != null) {
                    apprenantFinanceCombo.getItems().stream()
                            .filter(u -> u.getId() == selectedUser.getId())
                            .findFirst()
                            .ifPresent(apprenantFinanceCombo::setValue);
                }
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de recharger les apprenants: " + e.getMessage());
            }
        }
    }

    private void loadFinances() {
        try {
            Map<String, Double> revenus = financeService.calculerRevenuTotal();

            if (revenuInscriptionsLabel != null) {
                revenuInscriptionsLabel.setText(String.format("%.2f DT", revenus.get("total_inscriptions")));
            }
            if (revenuCodeLabel != null) {
                revenuCodeLabel.setText(String.format("%.2f DT", revenus.get("total_code")));
            }
            if (revenuConduiteLabel != null) {
                revenuConduiteLabel.setText(String.format("%.2f DT", revenus.get("total_conduite")));
            }
            if (revenuParcLabel != null) {
                revenuParcLabel.setText(String.format("%.2f DT", revenus.get("total_parc")));
            }
            if (revenuExamensLabel != null) {
                revenuExamensLabel.setText(String.format("%.2f DT", revenus.get("total_examens")));
            }
            if (revenuAutoEcoleLabel != null) {
                revenuAutoEcoleLabel.setText(String.format("%.2f DT", revenus.get("revenu_auto_ecole")));
            }
            if (revenuServiceMinesLabel != null) {
                revenuServiceMinesLabel.setText(String.format("%.2f DT", revenus.get("revenu_service_mines")));
            }
            if (revenuTotalLabel != null) {
                revenuTotalLabel.setText(String.format("%.2f DT", revenus.get("total_general")));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les finances: " + e.getMessage());
        }
    }

    @FXML
    private void handleAfficherDetailsApprenant() {
        if (apprenantFinanceCombo == null || detailsApprenantBox == null) return;

        User selected = apprenantFinanceCombo.getValue();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un apprenant");
            return;
        }

        try {
            detailsApprenantBox.getChildren().clear();

            // Titre
            Label titre = new Label("💰 Détails Financiers - " + selected.getNomComplet());
            titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            detailsApprenantBox.getChildren().add(titre);

            Separator sep1 = new Separator();
            detailsApprenantBox.getChildren().add(sep1);

            // Calculer les données
            Map<String, Double> revenus = financeService.calculerRevenuApprenant(selected.getId());
            Map<String, Double> coutPrevu = financeService.calculerCoutPrevu(selected);
            Map<String, Double> heures = financeService.calculerHeuresEffectuees(selected.getId());

            // Créer la grille
            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(10);
            grid.setStyle("-fx-padding: 10;");

            int row = 0;

            // Frais d'inscription
            grid.add(new Label("Frais d'inscription:"), 0, row);
            Label inscriptionLabel = new Label(String.format("%.2f DT", revenus.get("frais_inscription")));
            inscriptionLabel.setStyle("-fx-font-weight: bold;");
            grid.add(inscriptionLabel, 1, row++);

            // Heures et revenus Code
            grid.add(new Label("📚 Code (" + String.format("%.1fh/%dh", heures.get("code"), selected.getHeuresPreveuesCode()) + "):"), 0, row);
            grid.add(new Label(String.format("%.2f DT", revenus.get("revenu_code"))), 1, row++);

            // Heures et revenus Conduite
            grid.add(new Label("🚗 Conduite (" + String.format("%.1fh/%dh", heures.get("conduite"), selected.getHeuresPreveuesConduite()) + "):"), 0, row);
            grid.add(new Label(String.format("%.2f DT", revenus.get("revenu_conduite"))), 1, row++);

            // Heures et revenus Parc
            grid.add(new Label("🅿️ Parc (" + String.format("%.1fh/%dh", heures.get("parc"), selected.getHeuresPreveuesParc()) + "):"), 0, row);
            grid.add(new Label(String.format("%.2f DT", revenus.get("revenu_parc"))), 1, row++);

            // Examens
            grid.add(new Label("📝 Frais d'examens:"), 0, row);
            grid.add(new Label(String.format("%.2f DT (Code: %.0f, Conduite: %.0f, Parc: %.0f)",
                    revenus.get("frais_examens"),
                    revenus.get("nb_examens_code"),
                    revenus.get("nb_examens_conduite"),
                    revenus.get("nb_examens_parc"))), 1, row++);

            detailsApprenantBox.getChildren().add(grid);

            Separator sep2 = new Separator();
            detailsApprenantBox.getChildren().add(sep2);

            // Totaux
            GridPane totaux = new GridPane();
            totaux.setHgap(20);
            totaux.setVgap(10);
            totaux.setStyle("-fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 5;");

            totaux.add(new Label("Total Payé:"), 0, 0);
            Label totalLabel = new Label(String.format("%.2f DT", revenus.get("total")));
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #27ae60;");
            totaux.add(totalLabel, 1, 0);

            totaux.add(new Label("Coût Total Prévu:"), 0, 1);
            Label prevuLabel = new Label(String.format("%.2f DT", coutPrevu.get("total_prevu")));
            prevuLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            totaux.add(prevuLabel, 1, 1);

            totaux.add(new Label("Reste à Payer:"), 0, 2);
            double restant = coutPrevu.get("total_prevu") - revenus.get("total");
            Label restantLabel = new Label(String.format("%.2f DT", restant));
            restantLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " +
                    (restant > 0 ? "#e74c3c" : "#27ae60") + ";");
            totaux.add(restantLabel, 1, 2);

            detailsApprenantBox.getChildren().add(totaux);

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les détails: " + e.getMessage());
        }
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