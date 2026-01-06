package tn.spring.autoecole.services;

import tn.spring.autoecole.dao.SeanceDAO;
import tn.spring.autoecole.dao.UserDAO;
import tn.spring.autoecole.dao.VehiculeDAO;
import tn.spring.autoecole.models.Seance;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.Vehicule;
import tn.spring.autoecole.models.enums.Role;
import tn.spring.autoecole.models.enums.TypeSeance;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class SeanceService {

    private final SeanceDAO seanceDAO;
    private final UserDAO userDAO;
    private final VehiculeDAO vehiculeDAO;

    public SeanceService() {
        this.seanceDAO = new SeanceDAO();
        this.userDAO = new UserDAO();
        this.vehiculeDAO = new VehiculeDAO();
    }

    public List<Seance> getAllSeances() throws SQLException {
        return seanceDAO.findAll();
    }

    public Optional<Seance> getSeanceById(int id) throws SQLException {
        return seanceDAO.findById(id);
    }

    public List<Seance> getSeancesByApprenant(int apprenantId) throws SQLException {
        return seanceDAO.findByApprenant(apprenantId);
    }

    public List<Seance> getSeancesByMoniteur(int moniteurId) throws SQLException {
        return seanceDAO.findByMoniteur(moniteurId);
    }

    public List<Seance> getSeancesByDate(LocalDate date) throws SQLException {
        return seanceDAO.findByDate(date);
    }

    public List<Seance> getUpcomingSeancesByApprenant(int apprenantId) throws SQLException {
        return seanceDAO.findUpcomingByApprenant(apprenantId);
    }

    public Seance createSeance(Seance seance) throws SQLException, IllegalArgumentException {
        if (seance.getType() == null) {
            throw new IllegalArgumentException("Le type de séance est obligatoire");
        }
        if (seance.getDateSeance() == null) {
            throw new IllegalArgumentException("La date de la séance est obligatoire");
        }
        if (seance.getHeureSeance() == null) {
            throw new IllegalArgumentException("L'heure de la séance est obligatoire");
        }

        if (seance.getDateSeance().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de la séance ne peut pas être dans le passé");
        }

        Optional<User> apprenantOpt = userDAO.findById(seance.getApprenantId());
        if (apprenantOpt.isEmpty()) {
            throw new IllegalArgumentException("Apprenant non trouvé");
        }
        User apprenant = apprenantOpt.get();
        if (apprenant.getRole() != Role.APPRENANT) {
            throw new IllegalArgumentException("L'utilisateur spécifié n'est pas un apprenant");
        }

        if (seance.getType() == TypeSeance.CONDUITE || seance.getType() == TypeSeance.PARC) {
            if (seance.getMoniteurId() == null) {
                throw new IllegalArgumentException("Un moniteur est obligatoire pour une séance de " +
                        seance.getType().getDisplayName());
            }

            Optional<User> moniteurOpt = userDAO.findById(seance.getMoniteurId());
            if (moniteurOpt.isEmpty()) {
                throw new IllegalArgumentException("Moniteur non trouvé");
            }
            User moniteur = moniteurOpt.get();
            if (moniteur.getRole() != Role.MONITEUR) {
                throw new IllegalArgumentException("L'utilisateur spécifié n'est pas un moniteur");
            }

            if (seance.getVehiculeId() == null) {
                throw new IllegalArgumentException("Un véhicule est obligatoire pour une séance de " +
                        seance.getType().getDisplayName());
            }

            Optional<Vehicule> vehiculeOpt = vehiculeDAO.findById(seance.getVehiculeId());
            if (vehiculeOpt.isEmpty()) {
                throw new IllegalArgumentException("Véhicule non trouvé");
            }
            Vehicule vehicule = vehiculeOpt.get();
            if (!vehicule.isDisponible()) {
                throw new IllegalArgumentException("Le véhicule sélectionné n'est pas disponible");
            }

            if (apprenant.getTypePermis() != vehicule.getType()) {
                throw new IllegalArgumentException("Le type de véhicule ne correspond pas au type de permis de l'apprenant");
            }
        }

        if (!apprenant.isFraisInscriptionPaye()) {
            apprenant.setFraisInscriptionPaye(true);
            userDAO.update(apprenant);
        }

        return seanceDAO.save(seance);
    }

    public void updateSeance(Seance seance) throws SQLException, IllegalArgumentException {
        if (seanceDAO.findById(seance.getId()).isEmpty()) {
            throw new IllegalArgumentException("Séance non trouvée");
        }

        if (seance.getType() == null) {
            throw new IllegalArgumentException("Le type de séance est obligatoire");
        }
        if (seance.getDateSeance() == null) {
            throw new IllegalArgumentException("La date de la séance est obligatoire");
        }
        if (seance.getHeureSeance() == null) {
            throw new IllegalArgumentException("L'heure de la séance est obligatoire");
        }

        if (seance.getType() == TypeSeance.CONDUITE || seance.getType() == TypeSeance.PARC) {
            if (seance.getMoniteurId() == null) {
                throw new IllegalArgumentException("Un moniteur est obligatoire");
            }
            if (seance.getVehiculeId() == null) {
                throw new IllegalArgumentException("Un véhicule est obligatoire");
            }
        }

        seanceDAO.update(seance);
    }

    public void deleteSeance(int id) throws SQLException, IllegalArgumentException {
        if (seanceDAO.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Séance non trouvée");
        }
        seanceDAO.delete(id);
    }

    public long countByApprenant(int apprenantId) throws SQLException {
        return seanceDAO.countByApprenant(apprenantId);
    }

    public long countByMoniteur(int moniteurId) throws SQLException {
        return seanceDAO.countByMoniteur(moniteurId);
    }

    public boolean isMoniteurAvailable(int moniteurId, LocalDate date, LocalTime time)
            throws SQLException {
        List<Seance> seances = seanceDAO.findByMoniteur(moniteurId);
        return seances.stream()
                .noneMatch(s -> s.getDateSeance().equals(date) && s.getHeureSeance().equals(time));
    }

    public boolean isVehiculeAvailable(int vehiculeId, LocalDate date, LocalTime time)
            throws SQLException {
        List<Seance> seances = seanceDAO.findAll();
        return seances.stream()
                .filter(s -> s.getVehiculeId() != null && s.getVehiculeId() == vehiculeId)
                .noneMatch(s -> s.getDateSeance().equals(date) && s.getHeureSeance().equals(time));
    }
}