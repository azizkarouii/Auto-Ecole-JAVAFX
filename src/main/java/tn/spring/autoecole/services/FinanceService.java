package tn.spring.autoecole.services;

import tn.spring.autoecole.dao.UserDAO;
import tn.spring.autoecole.dao.SeanceDAO;
import tn.spring.autoecole.dao.ExamenDAO;
import tn.spring.autoecole.models.*;
import tn.spring.autoecole.models.enums.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceService {

    // Constantes de tarification
    public static final double FRAIS_INSCRIPTION = 60.0;
    public static final double TARIF_HEURE_CODE = 10.0;
    public static final double TARIF_HEURE_CONDUITE = 25.0;
    public static final double TARIF_HEURE_PARC = 25.0;
    public static final double FRAIS_EXAMEN_CODE = 60.0;
    public static final double FRAIS_EXAMEN_CONDUITE = 150.0;
    public static final double FRAIS_EXAMEN_PARC = 150.0;

    private final UserDAO userDAO;
    private final SeanceDAO seanceDAO;
    private final ExamenDAO examenDAO;

    public FinanceService() {
        this.userDAO = new UserDAO();
        this.seanceDAO = new SeanceDAO();
        this.examenDAO = new ExamenDAO();
    }

    /**
     * Calcule le revenu total pour un apprenant donné
     */
    public Map<String, Double> calculerRevenuApprenant(int apprenantId) throws SQLException {
        Map<String, Double> revenus = new HashMap<>();

        // 1. Frais d'inscription
        User apprenant = userDAO.findById(apprenantId).orElse(null);
        double fraisInscription = (apprenant != null && apprenant.isFraisInscriptionPaye()) ?
                FRAIS_INSCRIPTION : 0.0;
        revenus.put("frais_inscription", fraisInscription);

        // 2. Revenus des séances
        List<Seance> seances = seanceDAO.findByApprenant(apprenantId);
        double revenuCode = 0.0;
        double revenuConduite = 0.0;
        double revenuParc = 0.0;
        double heuresCode = 0.0;
        double heuresConduite = 0.0;
        double heuresParc = 0.0;

        for (Seance seance : seances) {
            if (seance.isPassed()) { // Seulement les séances passées
                double duree = seance.getDureeEnHeures();
                switch (seance.getType()) {
                    case CODE:
                        heuresCode += duree;
                        revenuCode += duree * TARIF_HEURE_CODE;
                        break;
                    case CONDUITE:
                        heuresConduite += duree;
                        revenuConduite += duree * TARIF_HEURE_CONDUITE;
                        break;
                    case PARC:
                        heuresParc += duree;
                        revenuParc += duree * TARIF_HEURE_PARC;
                        break;
                }
            }
        }

        revenus.put("revenu_code", revenuCode);
        revenus.put("revenu_conduite", revenuConduite);
        revenus.put("revenu_parc", revenuParc);
        revenus.put("heures_code", heuresCode);
        revenus.put("heures_conduite", heuresConduite);
        revenus.put("heures_parc", heuresParc);

        // 3. Frais d'examens
        List<Examen> examens = examenDAO.findByApprenant(apprenantId);
        double fraisExamens = 0.0;
        int nbExamensCode = 0;
        int nbExamensConduite = 0;
        int nbExamensParc = 0;

        for (Examen examen : examens) {
            if (examen.isPassed()) {
                switch (examen.getType()) {
                    case CODE:
                        fraisExamens += FRAIS_EXAMEN_CODE;
                        nbExamensCode++;
                        break;
                    case CONDUITE:
                        fraisExamens += FRAIS_EXAMEN_CONDUITE;
                        nbExamensConduite++;
                        break;
                    case PARC:
                        fraisExamens += FRAIS_EXAMEN_PARC;
                        nbExamensParc++;
                        break;
                }
            }
        }

        revenus.put("frais_examens", fraisExamens);
        revenus.put("nb_examens_code", (double) nbExamensCode);
        revenus.put("nb_examens_conduite", (double) nbExamensConduite);
        revenus.put("nb_examens_parc", (double) nbExamensParc);

        // 4. Total
        double total = fraisInscription + revenuCode + revenuConduite + revenuParc + fraisExamens;
        revenus.put("total", total);

        return revenus;
    }

    /**
     * Calcule le coût total prévu pour un apprenant selon ses heures prévues
     */
    public Map<String, Double> calculerCoutPrevu(User apprenant) {
        Map<String, Double> couts = new HashMap<>();

        // Frais d'inscription
        couts.put("frais_inscription", FRAIS_INSCRIPTION);

        // Coût des séances prévues
        double coutCode = apprenant.getHeuresPreveuesCode() * TARIF_HEURE_CODE;
        double coutConduite = apprenant.getHeuresPreveuesConduite() * TARIF_HEURE_CONDUITE;
        double coutParc = apprenant.getHeuresPreveuesParc() * TARIF_HEURE_PARC;

        couts.put("cout_code", coutCode);
        couts.put("cout_conduite", coutConduite);
        couts.put("cout_parc", coutParc);

        // Frais d'examens (1 examen par phase minimum)
        double fraisExamens = FRAIS_EXAMEN_CODE + FRAIS_EXAMEN_CONDUITE + FRAIS_EXAMEN_PARC;
        couts.put("frais_examens_prevus", fraisExamens);

        // Total prévu
        double total = FRAIS_INSCRIPTION + coutCode + coutConduite + coutParc + fraisExamens;
        couts.put("total_prevu", total);

        return couts;
    }

    /**
     * Calcule le revenu total de l'auto-école
     */
    public Map<String, Double> calculerRevenuTotal() throws SQLException {
        Map<String, Double> revenus = new HashMap<>();

        // Total frais d'inscription
        List<User> apprenants = userDAO.findApprenants();
        double totalInscriptions = apprenants.stream()
                .filter(User::isFraisInscriptionPaye)
                .count() * FRAIS_INSCRIPTION;
        revenus.put("total_inscriptions", totalInscriptions);

        // Total séances
        List<Seance> seances = seanceDAO.findAll();
        double totalCode = 0.0;
        double totalConduite = 0.0;
        double totalParc = 0.0;

        for (Seance seance : seances) {
            if (seance.isPassed()) {
                double duree = seance.getDureeEnHeures();
                switch (seance.getType()) {
                    case CODE:
                        totalCode += duree * TARIF_HEURE_CODE;
                        break;
                    case CONDUITE:
                        totalConduite += duree * TARIF_HEURE_CONDUITE;
                        break;
                    case PARC:
                        totalParc += duree * TARIF_HEURE_PARC;
                        break;
                }
            }
        }

        revenus.put("total_code", totalCode);
        revenus.put("total_conduite", totalConduite);
        revenus.put("total_parc", totalParc);

        // Total examens
        List<Examen> examens = examenDAO.findAll();
        double totalExamens = 0.0;

        for (Examen examen : examens) {
            if (examen.isPassed()) {
                switch (examen.getType()) {
                    case CODE:
                        totalExamens += FRAIS_EXAMEN_CODE;
                        break;
                    case CONDUITE:
                        totalExamens += FRAIS_EXAMEN_CONDUITE;
                        break;
                    case PARC:
                        totalExamens += FRAIS_EXAMEN_PARC;
                        break;
                }
            }
        }

        revenus.put("total_examens", totalExamens);

        // Revenus pour le service des mines (frais d'inscription + examens)
        double revenuServiceDesMines = totalInscriptions + totalExamens;
        revenus.put("revenu_service_mines", revenuServiceDesMines);

        // Revenu total de l'auto-école (uniquement les séances)
        double revenuAutoEcole = totalCode + totalConduite + totalParc;
        revenus.put("revenu_auto_ecole", revenuAutoEcole);

        // Total général (tout confondu)
        double total = totalInscriptions + totalCode + totalConduite + totalParc + totalExamens;
        revenus.put("total_general", total);

        return revenus;
    }

    /**
     * Calcule les heures effectuées par un apprenant
     */
    public Map<String, Double> calculerHeuresEffectuees(int apprenantId) throws SQLException {
        Map<String, Double> heures = new HashMap<>();

        List<Seance> seances = seanceDAO.findByApprenant(apprenantId);
        double heuresCode = 0.0;
        double heuresConduite = 0.0;
        double heuresParc = 0.0;

        for (Seance seance : seances) {
            if (seance.isPassed()) {
                double duree = seance.getDureeEnHeures();
                switch (seance.getType()) {
                    case CODE:
                        heuresCode += duree;
                        break;
                    case CONDUITE:
                        heuresConduite += duree;
                        break;
                    case PARC:
                        heuresParc += duree;
                        break;
                }
            }
        }

        heures.put("code", heuresCode);
        heures.put("conduite", heuresConduite);
        heures.put("parc", heuresParc);
        heures.put("total", heuresCode + heuresConduite + heuresParc);

        return heures;
    }

    /**
     * Calcule le pourcentage de progression pour chaque phase
     */
    public Map<String, Double> calculerProgressionHeures(User apprenant, int apprenantId) throws SQLException {
        Map<String, Double> progression = new HashMap<>();
        Map<String, Double> heuresEffectuees = calculerHeuresEffectuees(apprenantId);

        double progCode = (apprenant.getHeuresPreveuesCode() > 0) ?
                (heuresEffectuees.get("code") / apprenant.getHeuresPreveuesCode() * 100) : 0;
        double progConduite = (apprenant.getHeuresPreveuesConduite() > 0) ?
                (heuresEffectuees.get("conduite") / apprenant.getHeuresPreveuesConduite() * 100) : 0;
        double progParc = (apprenant.getHeuresPreveuesParc() > 0) ?
                (heuresEffectuees.get("parc") / apprenant.getHeuresPreveuesParc() * 100) : 0;

        progression.put("code", Math.min(progCode, 100.0));
        progression.put("conduite", Math.min(progConduite, 100.0));
        progression.put("parc", Math.min(progParc, 100.0));

        return progression;
    }
}