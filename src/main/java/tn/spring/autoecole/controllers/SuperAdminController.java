package tn.spring.autoecole.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.spring.autoecole.HelloApplication;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.enums.Role;
import tn.spring.autoecole.services.*;
import tn.spring.autoecole.utils.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SuperAdminController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalApprenantsLabel;
    @FXML private Label totalMoniteursLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalVehiculesLabel;

    @FXML private TableView<User> adminsTable;
    @FXML private TableColumn<User, Integer> adminIdCol;
    @FXML private TableColumn<User, String> adminNomCol;
    @FXML private TableColumn<User, String> adminPrenomCol;
    @FXML private TableColumn<User, String> adminCinCol;
    @FXML private TableColumn<User, String> adminEmailCol;
    @FXML private TableColumn<User, String> adminTelCol;
    @FXML private TextField searchAdminField;

    @FXML private TableView<User> allUsersTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> userNomCol;
    @FXML private TableColumn<User, String> userPrenomCol;
    @FXML private TableColumn<User, String> userCinCol;
    @FXML private TableColumn<User, String> userEmailCol;
    @FXML private TableColumn<User, String> userTelCol;
    @FXML private TableColumn<User, String> userRoleCol;
    @FXML private TextField searchAllUsersField;
    @FXML private ComboBox<Role> filterRoleCombo;

    private final UserService userService;
    private final VehiculeService vehiculeService;
    private final AuthService authService;

    private ObservableList<User> adminsData;
    private ObservableList<User> allUsersData;

    public SuperAdminController() {
        this.userService = new UserService();
        this.vehiculeService = new VehiculeService();
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        setupWelcomeLabel();
        setupAdminsTab();
        setupAllUsersTab();
        setupSearchListeners();
        loadDashboardStats();
        loadAdmins();
        loadAllUsers();
    }

    private void setupSearchListeners() {
        // Recherche admins en temps réel
        if (searchAdminField != null) {
            searchAdminField.textProperty().addListener((observable, oldValue, newValue) -> {
                searchAdmins();
            });
        }

        // Recherche tous utilisateurs en temps réel
        if (searchAllUsersField != null) {
            searchAllUsersField.textProperty().addListener((observable, oldValue, newValue) -> {
                searchAllUsers();
            });
        }

        // Filtre rôle
        if (filterRoleCombo != null) {
            filterRoleCombo.setOnAction(e -> loadAllUsers());
        }
    }

    private void searchAdmins() {
        if (searchAdminField == null || adminsData == null) return;

        String searchText = searchAdminField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            loadAdmins();
            return;
        }

        try {
            List<User> filteredList = userService.getAllAdmins()
                    .stream()
                    .filter(admin ->
                            admin.getNom().toLowerCase().contains(searchText) ||
                                    admin.getPrenom().toLowerCase().contains(searchText) ||
                                    admin.getCin().toLowerCase().contains(searchText) ||
                                    admin.getMail().toLowerCase().contains(searchText) ||
                                    admin.getNumTel().contains(searchText)
                    )
                    .toList();

            adminsData = FXCollections.observableArrayList(filteredList);
            adminsTable.setItems(adminsData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private void searchAllUsers() {
        if (searchAllUsersField == null || allUsersData == null) return;

        String searchText = searchAllUsersField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            loadAllUsers();
            return;
        }

        try {
            Role roleFilter = filterRoleCombo != null ? filterRoleCombo.getValue() : null;

            List<User> allUsers = roleFilter != null ?
                    userService.getAllUsers().stream()
                            .filter(u -> u.getRole() == roleFilter)
                            .toList() :
                    userService.getAllUsers();

            List<User> filteredList = allUsers.stream()
                    .filter(user ->
                            user.getNom().toLowerCase().contains(searchText) ||
                                    user.getPrenom().toLowerCase().contains(searchText) ||
                                    user.getCin().toLowerCase().contains(searchText) ||
                                    user.getMail().toLowerCase().contains(searchText) ||
                                    user.getNumTel().contains(searchText)
                    )
                    .toList();

            allUsersData = FXCollections.observableArrayList(filteredList);
            allUsersTable.setItems(allUsersData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private void setupWelcomeLabel() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getNomComplet() + " (Super Admin)");
        }
    }

    private void setupAdminsTab() {
        adminIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        adminNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adminPrenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        adminCinCol.setCellValueFactory(new PropertyValueFactory<>("cin"));
        adminEmailCol.setCellValueFactory(new PropertyValueFactory<>("mail"));
        adminTelCol.setCellValueFactory(new PropertyValueFactory<>("numTel"));
    }

    private void setupAllUsersTab() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        userPrenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        userCinCol.setCellValueFactory(new PropertyValueFactory<>("cin"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("mail"));
        userTelCol.setCellValueFactory(new PropertyValueFactory<>("numTel"));
        userRoleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRole().getDisplayName()
                )
        );

        filterRoleCombo.getItems().add(null);
        filterRoleCombo.getItems().addAll(Role.values());
        filterRoleCombo.setPromptText("Tous les rôles");
    }

    private void loadDashboardStats() {
        try {
            long apprenants = userService.countByRole(Role.APPRENANT);
            long moniteurs = userService.countByRole(Role.MONITEUR);
            long admins = userService.countByRole(Role.ADMIN);
            long vehicules = vehiculeService.getAllVehicules().size();

            totalApprenantsLabel.setText(String.valueOf(apprenants));
            totalMoniteursLabel.setText(String.valueOf(moniteurs));
            totalAdminsLabel.setText(String.valueOf(admins));
            totalVehiculesLabel.setText(String.valueOf(vehicules));

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    @FXML
    private void loadAdmins() {
        try {
            List<User> admins = userService.getAllAdmins();
            adminsData = FXCollections.observableArrayList(admins);
            adminsTable.setItems(adminsData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les administrateurs: " + e.getMessage());
        }
    }

    @FXML
    private void loadAllUsers() {
        try {
            Role roleFilter = filterRoleCombo.getValue();
            List<User> users;

            if (roleFilter != null) {
                users = userService.getAllUsers().stream()
                        .filter(u -> u.getRole() == roleFilter)
                        .toList();
            } else {
                users = userService.getAllUsers();
            }

            allUsersData = FXCollections.observableArrayList(users);
            allUsersTable.setItems(allUsersData);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddAdmin() {
        Optional<User> result = UserDialogUtil.showAddAdminDialog();

        if (result.isPresent()) {
            try {
                User newAdmin = result.get();
                userService.createUser(newAdmin);
                AlertUtils.showSuccess("Administrateur ajouté avec succès");
                loadAdmins();
                loadAllUsers();
                loadDashboardStats();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible d'ajouter l'administrateur: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                AlertUtils.showError("Erreur de validation", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditAdmin() {
        User selected = adminsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un administrateur");
            return;
        }

        Optional<User> result = UserDialogUtil.showEditAdminDialog(selected);

        if (result.isPresent()) {
            try {
                User updatedAdmin = result.get();
                userService.updateUser(updatedAdmin);
                AlertUtils.showSuccess("Administrateur modifié avec succès");
                loadAdmins();
                loadAllUsers();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de modifier l'administrateur: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                AlertUtils.showError("Erreur de validation", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteAdmin() {
        User selected = adminsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un administrateur");
            return;
        }

        if (AlertUtils.confirmDeletion(selected.getNomComplet())) {
            try {
                userService.deleteUser(selected.getId());
                AlertUtils.showSuccess("Administrateur supprimé avec succès");
                loadAdmins();
                loadAllUsers();
                loadDashboardStats();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer l'administrateur: " + e.getMessage());
            }
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