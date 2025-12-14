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
import java.util.List;
import java.util.Map;

public class ApprenantController {

    @FXML private Label welcomeLabel;
    @FXML private Label typePermisLabel;
    @FXML private Label niveauActuelLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;

    // Progression détaillée
    @FXML private Label heuresCodeLabel;
    @FXML private Label heuresConduiteLabel;
    @FXML private Label heuresParcLabel;
    @FXML private ProgressBar progressCodeBar;
    @FXML private ProgressBar progressConduiteBar;
    @FXML private ProgressBar progressParcBar;
    @FXML private Label coutCodeLabel;
    @FXML private Label coutConduiteLabel;
    @FXML private Label coutParcLabel;
    @FXML private Label totalHeuresLabel;
    @FXML private Label coutTotalLabel;
    @FXML private Label coutPrevuLabel;
    @FXML private Label coutRestantLabel;

    // Tab Séances
    @FXML private TableView<Seance> seancesTable;
    @FXML private TableColumn<Seance, String> seanceTypeCol;
    @FXML private TableColumn<Seance, String> seanceDateCol;
    @FXML private TableColumn<Seance, String> seanceHeureCol;
    @FXML private TableColumn<Seance, String> seanceDureeCol;
    @FXML private TableColumn<Seance, String> seanceMoniteurCol;
    @FXML private TableColumn<Seance, String> seanceVehiculeCol;
    @FXML private TableColumn<Seance, String> seanceStatutCol;

    // Tab Examens
    @FXML private TableView<Examen> examensTable;
    @FXML private TableColumn<Examen, String> examenTypeCol;
    @FXML private TableColumn<Examen, String> examenDateCol;
    @FXML private TableColumn<Examen, String> examenHeureCol;
    @FXML private TableColumn<Examen, String> examenResultatCol;
    @FXML private TableColumn<Examen, String> examenStatutCol;

    private final UserService userService;
    private final SeanceService seanceService;
    private final ExamenService examenService;
    private final FinanceService financeService;
    private final AuthService authService;

    private ObservableList<Seance> seancesData;
    private ObservableList<Examen> examensData;

    public ApprenantController() {
        this.userService = new UserService();
        this.seanceService = new SeanceService();
        this.examenService = new ExamenService();
        this.financeService = new FinanceService();
        this.authService = new AuthService();
    }

    @FXML
    private void initialize() {
        setupWelcomeLabel();
        setupParcours();
        setupSeancesTab();
        setupExamensTab();
        loadAllData();
        loadProgressionDetaillee();
        loadFinances();
    }

    private void setupWelcomeLabel() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getNomComplet());
        }
    }

    private void setupParcours() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Type de permis
            if (currentUser.getTypePermis() != null) {
                typePermisLabel.setText(currentUser.getTypePermis().getDisplayName() + " " +
                        currentUser.getTypePermis().getIcon());
            } else {
                typePermisLabel.setText("Non défini");
            }

            // Niveau actuel
            if (currentUser.getNiveau() != null) {
                niveauActuelLabel.setText(currentUser.getNiveau().getIcon() + " " +
                        currentUser.getNiveau().getDisplayName());

                // Progress bar
                double progress = switch (currentUser.getNiveau()) {
                    case CODE -> 0.25;
                    case CONDUITE -> 0.50;
                    case PARC -> 0.75;
                    case OBTENU -> 1.0;
                };
                progressBar.setProgress(progress);
                progressLabel.setText(currentUser.getNiveau().getProgressPercentage());

                // Couleur du niveau
                String color = currentUser.getNiveau() == Niveau.OBTENU ? "#27ae60" : "#f39c12";
                niveauActuelLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");
            } else {
                niveauActuelLabel.setText("Non défini");
                progressBar.setProgress(0);
                progressLabel.setText("0%");
            }
        }
    }

    private void setupSeancesTab() {
        seanceTypeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getType().getIcon() + " " +
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

        // Nouvelle colonne durée
        if (seanceDureeCol != null) {
            seanceDureeCol.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getDureeFormatee()
                    )
            );
        }

        seanceMoniteurCol.setCellValueFactory(new PropertyValueFactory<>("moniteurNom"));
        seanceVehiculeCol.setCellValueFactory(new PropertyValueFactory<>("vehiculeInfo"));
        seanceStatutCol.setCellValueFactory(cellData -> {
            Seance seance = cellData.getValue();
            String statut = seance.isPassed() ? "✓ Passée" :
                    seance.isToday() ? "⏰ Aujourd'hui" : "📅 À venir";
            return new javafx.beans.property.SimpleStringProperty(statut);
        });
    }

    private void setupExamensTab() {
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
        examenResultatCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getResultatComplet()
                )
        );
        examenStatutCol.setCellValueFactory(cellData -> {
            Examen examen = cellData.getValue();
            String statut = examen.isPassed() ? "Terminé" :
                    examen.isToday() ? "Aujourd'hui" : "À venir";
            return new javafx.beans.property.SimpleStringProperty(statut);
        });
    }

    private void loadProgressionDetaillee() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            // Calculer les heures effectuées
            Map<String, Double> heuresEffectuees = financeService.calculerHeuresEffectuees(currentUser.getId());

            // Calculer la progression
            Map<String, Double> progression = financeService.calculerProgressionHeures(currentUser, currentUser.getId());

            // Mise à jour Code
            if (heuresCodeLabel != null) {
                double heuresCode = heuresEffectuees.get("code");
                heuresCodeLabel.setText(String.format("%.1fh sur %dh",
                        heuresCode, currentUser.getHeuresPreveuesCode()));
            }
            if (progressCodeBar != null) {
                progressCodeBar.setProgress(progression.get("code") / 100.0);
            }
            if (coutCodeLabel != null) {
                double cout = heuresEffectuees.get("code") * FinanceService.TARIF_HEURE_CODE;
                coutCodeLabel.setText(String.format("%.2f DT", cout));
            }

            // Mise à jour Conduite
            if (heuresConduiteLabel != null) {
                double heuresConduite = heuresEffectuees.get("conduite");
                heuresConduiteLabel.setText(String.format("%.1fh sur %dh",
                        heuresConduite, currentUser.getHeuresPreveuesConduite()));
            }
            if (progressConduiteBar != null) {
                progressConduiteBar.setProgress(progression.get("conduite") / 100.0);
            }
            if (coutConduiteLabel != null) {
                double cout = heuresEffectuees.get("conduite") * FinanceService.TARIF_HEURE_CONDUITE;
                coutConduiteLabel.setText(String.format("%.2f DT", cout));
            }

            // Mise à jour Parc
            if (heuresParcLabel != null) {
                double heuresParc = heuresEffectuees.get("parc");
                heuresParcLabel.setText(String.format("%.1fh sur %dh",
                        heuresParc, currentUser.getHeuresPreveuesParc()));
            }
            if (progressParcBar != null) {
                progressParcBar.setProgress(progression.get("parc") / 100.0);
            }
            if (coutParcLabel != null) {
                double cout = heuresEffectuees.get("parc") * FinanceService.TARIF_HEURE_PARC;
                coutParcLabel.setText(String.format("%.2f DT", cout));
            }

            // Total heures
            if (totalHeuresLabel != null) {
                double totalEffectue = heuresEffectuees.get("total");
                int totalPrevu = currentUser.getHeuresPreveuesCode() +
                        currentUser.getHeuresPreveuesConduite() +
                        currentUser.getHeuresPreveuesParc();
                totalHeuresLabel.setText(String.format("%.1fh / %dh", totalEffectue, totalPrevu));
            }

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger la progression: " + e.getMessage());
        }
    }

    private void loadFinances() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            // Calculer les revenus (ce que l'apprenant a déjà payé)
            Map<String, Double> revenus = financeService.calculerRevenuApprenant(currentUser.getId());

            // Calculer le coût prévu total
            Map<String, Double> coutPrevu = financeService.calculerCoutPrevu(currentUser);

            // Coût total payé
            if (coutTotalLabel != null) {
                double total = revenus.get("total");
                coutTotalLabel.setText(String.format("%.2f DT", total));
            }

            // Coût prévu
            if (coutPrevuLabel != null) {
                double prevu = coutPrevu.get("total_prevu");
                coutPrevuLabel.setText(String.format("%.2f DT", prevu));
            }

            // Coût restant
            if (coutRestantLabel != null) {
                double total = revenus.get("total");
                double prevu = coutPrevu.get("total_prevu");
                double restant = prevu - total;
                coutRestantLabel.setText(String.format("%.2f DT", restant));

                // Couleur selon le montant
                if (restant > 0) {
                    coutRestantLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else {
                    coutRestantLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les finances: " + e.getMessage());
        }
    }

    private void loadAllData() {
        loadSeances();
        loadExamens();
    }

    @FXML
    private void loadSeances() {
        try {
            User currentUser = Session.getInstance().getCurrentUser();
            if (currentUser != null) {
                List<Seance> seances = seanceService.getUpcomingSeancesByApprenant(currentUser.getId());
                seancesData = FXCollections.observableArrayList(seances);
                seancesTable.setItems(seancesData);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les séances: " + e.getMessage());
        }
    }

    @FXML
    private void loadExamens() {
        try {
            User currentUser = Session.getInstance().getCurrentUser();
            if (currentUser != null) {
                List<Examen> examens = examenService.getExamensByApprenant(currentUser.getId());
                examensData = FXCollections.observableArrayList(examens);
                examensTable.setItems(examensData);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les examens: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllData();
        loadProgressionDetaillee();
        loadFinances();
        AlertUtils.showSuccess("Données actualisées");
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