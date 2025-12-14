package tn.spring.autoecole.services;

import tn.spring.autoecole.dao.ExamenDAO;
import tn.spring.autoecole.dao.UserDAO;
import tn.spring.autoecole.models.Examen;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.enums.Niveau;
import tn.spring.autoecole.models.enums.ResultatExamen;
import tn.spring.autoecole.models.enums.Role;
import tn.spring.autoecole.models.enums.TypeSeance;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExamenService {

    private final ExamenDAO examenDAO;
    private final UserDAO userDAO;

    public ExamenService() {
        this.examenDAO = new ExamenDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Récupère tous les examens
     */
    public List<Examen> getAllExamens() throws SQLException {
        return examenDAO.findAll();
    }

    /**
     * Récupère un examen par ID
     */
    public Optional<Examen> getExamenById(int id) throws SQLException {
        return examenDAO.findById(id);
    }

    /**
     * Récupère les examens d'un apprenant
     */
    public List<Examen> getExamensByApprenant(int apprenantId) throws SQLException {
        return examenDAO.findByApprenant(apprenantId);
    }

    /**
     * Récupère les examens à venir d'un apprenant
     */
    public List<Examen> getUpcomingExamensByApprenant(int apprenantId) throws SQLException {
        return examenDAO.findUpcomingByApprenant(apprenantId);
    }

    /**
     * Récupère les examens d'une date
     */
    public List<Examen> getExamensByDate(LocalDate date) throws SQLException {
        return examenDAO.findByDate(date);
    }

    /**
     * Récupère les examens par résultat
     */
    public List<Examen> getExamensByResultat(ResultatExamen resultat) throws SQLException {
        return examenDAO.findByResultat(resultat);
    }

    /**
     * Crée un nouvel examen
     */
    public Examen createExamen(Examen examen) throws SQLException, IllegalArgumentException {
        // Validations de base
        if (examen.getType() == null) {
            throw new IllegalArgumentException("Le type d'examen est obligatoire");
        }
        if (examen.getDateExamen() == null) {
            throw new IllegalArgumentException("La date de l'examen est obligatoire");
        }
        if (examen.getHeureExamen() == null) {
            throw new IllegalArgumentException("L'heure de l'examen est obligatoire");
        }

        // Vérifier que la date n'est pas dans le passé
        if (examen.getDateExamen().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de l'examen ne peut pas être dans le passé");
        }

        // Vérifier que l'apprenant existe
        Optional<User> apprenantOpt = userDAO.findById(examen.getApprenantId());
        if (apprenantOpt.isEmpty()) {
            throw new IllegalArgumentException("Apprenant non trouvé");
        }
        User apprenant = apprenantOpt.get();
        if (apprenant.getRole() != Role.APPRENANT) {
            throw new IllegalArgumentException("L'utilisateur spécifié n'est pas un apprenant");
        }

        // Vérifier que l'apprenant est au bon niveau pour cet examen
        Niveau niveauRequis = examen.getType().getCorrespondingNiveau();
        if (apprenant.getNiveau() != niveauRequis) {
            throw new IllegalArgumentException(
                    "L'apprenant n'est pas au niveau requis pour cet examen. " +
                            "Niveau actuel: " + (apprenant.getNiveau() != null ? apprenant.getNiveau().getDisplayName() : "Aucun") +
                            ", Niveau requis: " + niveauRequis.getDisplayName()
            );
        }

        // Initialiser le résultat à EN_ATTENTE si non défini
        if (examen.getResultat() == null) {
            examen.setResultat(ResultatExamen.EN_ATTENTE);
        }

        // Marquer les frais d'inscription comme payés lors de la première séance/examen
        if (!apprenant.isFraisInscriptionPaye()) {
            apprenant.setFraisInscriptionPaye(true);
            userDAO.update(apprenant);
        }

        // Sauvegarder
        return examenDAO.save(examen);
    }

    /**
     * Met à jour un examen
     */
    public void updateExamen(Examen examen) throws SQLException, IllegalArgumentException {
        // Vérifier que l'examen existe
        if (examenDAO.findById(examen.getId()).isEmpty()) {
            throw new IllegalArgumentException("Examen non trouvé");
        }

        // Validations de base
        if (examen.getType() == null) {
            throw new IllegalArgumentException("Le type d'examen est obligatoire");
        }
        if (examen.getDateExamen() == null) {
            throw new IllegalArgumentException("La date de l'examen est obligatoire");
        }
        if (examen.getHeureExamen() == null) {
            throw new IllegalArgumentException("L'heure de l'examen est obligatoire");
        }
        if (examen.getResultat() == null) {
            throw new IllegalArgumentException("Le résultat de l'examen est obligatoire");
        }

        examenDAO.update(examen);
    }

    /**
     * Met à jour le résultat d'un examen et fait progresser l'apprenant si réussi
     */
    public void updateResultatExamen(int examenId, ResultatExamen resultat)
            throws SQLException, IllegalArgumentException {

        Optional<Examen> examenOpt = examenDAO.findById(examenId);
        if (examenOpt.isEmpty()) {
            throw new IllegalArgumentException("Examen non trouvé");
        }

        Examen examen = examenOpt.get();

        // Mettre à jour le résultat
        examenDAO.updateResultat(examenId, resultat);

        // Si l'examen est réussi, faire progresser l'apprenant
        if (resultat == ResultatExamen.REUSSI) {
            progressApprenantAfterExam(examen.getApprenantId(), examen.getType());
        }
    }

    /**
     * Fait progresser un apprenant après la réussite d'un examen
     */
    private void progressApprenantAfterExam(int apprenantId, TypeSeance typeExamen)
            throws SQLException {

        Optional<User> apprenantOpt = userDAO.findById(apprenantId);
        if (apprenantOpt.isEmpty()) {
            return;
        }

        User apprenant = apprenantOpt.get();
        Niveau currentNiveau = apprenant.getNiveau();

        if (currentNiveau == null) {
            return;
        }

        // Déterminer le nouveau niveau basé sur le type d'examen réussi
        Niveau newNiveau = switch (typeExamen) {
            case CODE -> currentNiveau == Niveau.CODE ? Niveau.CONDUITE : currentNiveau;
            case CONDUITE -> currentNiveau == Niveau.CONDUITE ? Niveau.PARC : currentNiveau;
            case PARC -> currentNiveau == Niveau.PARC ? Niveau.OBTENU : currentNiveau;
        };

        // Mettre à jour le niveau si changement
        if (newNiveau != currentNiveau) {
            userDAO.updateNiveau(apprenantId, newNiveau);
        }
    }

    /**
     * Supprime un examen
     */
    public void deleteExamen(int id) throws SQLException, IllegalArgumentException {
        if (examenDAO.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Examen non trouvé");
        }
        examenDAO.delete(id);
    }

    /**
     * Compte le nombre d'examens d'un apprenant
     */
    public long countByApprenant(int apprenantId) throws SQLException {
        return examenDAO.countByApprenant(apprenantId);
    }

    /**
     * Récupère le nombre d'examens par résultat
     */
    public Map<ResultatExamen, Long> countByResultat() throws SQLException {
        return examenDAO.countByResultat();
    }

    /**
     * Calcule le taux de réussite global
     */
    public double getSuccessRate() throws SQLException {
        return examenDAO.getSuccessRate();
    }

    /**
     * Vérifie si un apprenant a réussi un examen d'un type donné
     */
    public boolean hasPassedExam(int apprenantId, TypeSeance typeExamen) throws SQLException {
        List<Examen> examens = examenDAO.findByApprenant(apprenantId);
        return examens.stream()
                .anyMatch(e -> e.getType() == typeExamen && e.getResultat() == ResultatExamen.REUSSI);
    }

    /**
     * Récupère le dernier examen d'un apprenant pour un type donné
     */
    public Optional<Examen> getLastExamByType(int apprenantId, TypeSeance typeExamen)
            throws SQLException {
        List<Examen> examens = examenDAO.findByApprenant(apprenantId);
        return examens.stream()
                .filter(e -> e.getType() == typeExamen)
                .max((e1, e2) -> {
                    int dateCompare = e1.getDateExamen().compareTo(e2.getDateExamen());
                    if (dateCompare != 0) return dateCompare;
                    return e1.getHeureExamen().compareTo(e2.getHeureExamen());
                });
    }
}