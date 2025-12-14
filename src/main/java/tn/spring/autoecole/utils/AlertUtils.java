package tn.spring.autoecole.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertUtils {

    /**
     * Affiche une alerte d'information
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte de succès
     */
    public static void showSuccess(String message) {
        showInfo("Succès", message);
    }

    /**
     * Affiche une alerte d'erreur
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'erreur simple
     */
    public static void showError(String message) {
        showError("Erreur", message);
    }

    /**
     * Affiche une alerte d'avertissement
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'avertissement simple
     */
    public static void showWarning(String message) {
        showWarning("Attention", message);
    }

    /**
     * Affiche une boîte de dialogue de confirmation
     * @return true si l'utilisateur clique sur OK, false sinon
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Affiche une confirmation de suppression
     */
    public static boolean confirmDeletion(String itemName) {
        return showConfirmation(
                "Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer " + itemName + " ?\nCette action est irréversible."
        );
    }

    /**
     * Affiche une confirmation de déconnexion
     */
    public static boolean confirmLogout() {
        return showConfirmation(
                "Déconnexion",
                "Êtes-vous sûr de vouloir vous déconnecter ?"
        );
    }
}