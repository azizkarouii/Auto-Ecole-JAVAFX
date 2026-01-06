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

    public List<Examen> getAllExamens() throws SQLException {
        return examenDAO.findAll();
    }


    public Optional<Examen> getExamenById(int id) throws SQLException {
        return examenDAO.findById(id);
    }


    public List<Examen> getExamensByApprenant(int apprenantId) throws SQLException {
        return examenDAO.findByApprenant(apprenantId);
    }


    public List<Examen> getUpcomingExamensByApprenant(int apprenantId) throws SQLException {
        return examenDAO.findUpcomingByApprenant(apprenantId);
    }


    public List<Examen> getExamensByDate(LocalDate date) throws SQLException {
        return examenDAO.findByDate(date);
    }


    public List<Examen> getExamensByResultat(ResultatExamen resultat) throws SQLException {
        return examenDAO.findByResultat(resultat);
    }

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

        if (examen.getDateExamen().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de l'examen ne peut pas être dans le passé");
        }

        Optional<User> apprenantOpt = userDAO.findById(examen.getApprenantId());
        if (apprenantOpt.isEmpty()) {
            throw new IllegalArgumentException("Apprenant non trouvé");
        }
        User apprenant = apprenantOpt.get();
        if (apprenant.getRole() != Role.APPRENANT) {
            throw new IllegalArgumentException("L'utilisateur spécifié n'est pas un apprenant");
        }

        Niveau niveauRequis = examen.getType().getCorrespondingNiveau();
        if (apprenant.getNiveau() != niveauRequis) {
            throw new IllegalArgumentException(
                    "L'apprenant n'est pas au niveau requis pour cet examen. " +
                            "Niveau actuel: " + (apprenant.getNiveau() != null ? apprenant.getNiveau().getDisplayName() : "Aucun") +
                            ", Niveau requis: " + niveauRequis.getDisplayName()
            );
        }

        if (examen.getResultat() == null) {
            examen.setResultat(ResultatExamen.EN_ATTENTE);
        }

        if (!apprenant.isFraisInscriptionPaye()) {
            apprenant.setFraisInscriptionPaye(true);
            userDAO.update(apprenant);
        }

        return examenDAO.save(examen);
    }


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


    public void updateResultatExamen(int examenId, ResultatExamen resultat)
            throws SQLException, IllegalArgumentException {

        Optional<Examen> examenOpt = examenDAO.findById(examenId);
        if (examenOpt.isEmpty()) {
            throw new IllegalArgumentException("Examen non trouvé");
        }

        Examen examen = examenOpt.get();

        examenDAO.updateResultat(examenId, resultat);

        if (resultat == ResultatExamen.REUSSI) {
            progressApprenantAfterExam(examen.getApprenantId(), examen.getType());
        }
    }


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


    public void deleteExamen(int id) throws SQLException, IllegalArgumentException {
        if (examenDAO.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Examen non trouvé");
        }
        examenDAO.delete(id);
    }

    public long countByApprenant(int apprenantId) throws SQLException {
        return examenDAO.countByApprenant(apprenantId);
    }


    public Map<ResultatExamen, Long> countByResultat() throws SQLException {
        return examenDAO.countByResultat();
    }


    public double getSuccessRate() throws SQLException {
        return examenDAO.getSuccessRate();
    }


    public boolean hasPassedExam(int apprenantId, TypeSeance typeExamen) throws SQLException {
        List<Examen> examens = examenDAO.findByApprenant(apprenantId);
        return examens.stream()
                .anyMatch(e -> e.getType() == typeExamen && e.getResultat() == ResultatExamen.REUSSI);
    }

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