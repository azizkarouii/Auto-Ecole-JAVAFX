package tn.spring.autoecole.services;

import tn.spring.autoecole.dao.VehiculeDAO;
import tn.spring.autoecole.models.Vehicule;
import tn.spring.autoecole.models.enums.TypePermis;
import tn.spring.autoecole.utils.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VehiculeService {

    private final VehiculeDAO vehiculeDAO;

    public VehiculeService() {
        this.vehiculeDAO = new VehiculeDAO();
    }

    public List<Vehicule> getAllVehicules() throws SQLException {
        return vehiculeDAO.findAll();
    }

    public Optional<Vehicule> getVehiculeById(int id) throws SQLException {
        return vehiculeDAO.findById(id);
    }

    public List<Vehicule> getAvailableVehicules() throws SQLException {
        return vehiculeDAO.findAvailable();
    }

    public List<Vehicule> getVehiculesByType(TypePermis type) throws SQLException {
        return vehiculeDAO.findByType(type);
    }

    public List<Vehicule> getAvailableVehiculesByType(TypePermis type) throws SQLException {
        return vehiculeDAO.findAvailableByType(type);
    }

    public List<Vehicule> getAvailableVehiculesByTypeAndDate(TypePermis type, LocalDate date)
            throws SQLException {
        return vehiculeDAO.findAvailableByTypeAndDate(type, date);
    }

    public Vehicule createVehicule(Vehicule vehicule) throws SQLException, IllegalArgumentException {
        String validationError = ValidationUtils.validateVehicule(
                vehicule.getMatricule(),
                vehicule.getMarque(),
                vehicule.getModele()
        );

        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (vehicule.getType() == null) {
            throw new IllegalArgumentException("Le type de véhicule est obligatoire");
        }

        if (vehiculeDAO.existsByMatricule(vehicule.getMatricule())) {
            throw new IllegalArgumentException("Un véhicule avec cette matricule existe déjà");
        }

        return vehiculeDAO.save(vehicule);
    }

    public void updateVehicule(Vehicule vehicule) throws SQLException, IllegalArgumentException {
        if (vehiculeDAO.findById(vehicule.getId()).isEmpty()) {
            throw new IllegalArgumentException("Véhicule non trouvé");
        }

        String validationError = ValidationUtils.validateVehicule(
                vehicule.getMatricule(),
                vehicule.getMarque(),
                vehicule.getModele()
        );

        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (vehicule.getType() == null) {
            throw new IllegalArgumentException("Le type de véhicule est obligatoire");
        }

        if (vehiculeDAO.existsByMatriculeExcludingId(vehicule.getMatricule(), vehicule.getId())) {
            throw new IllegalArgumentException("Un autre véhicule avec cette matricule existe déjà");
        }

        vehiculeDAO.update(vehicule);
    }

    public void deleteVehicule(int id) throws SQLException, IllegalArgumentException {
        if (vehiculeDAO.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Véhicule non trouvé");
        }
        vehiculeDAO.delete(id);
    }

    public void toggleDisponibilite(int id) throws SQLException {
        Optional<Vehicule> vehiculeOpt = vehiculeDAO.findById(id);
        if (vehiculeOpt.isEmpty()) {
            throw new IllegalArgumentException("Véhicule non trouvé");
        }

        Vehicule vehicule = vehiculeOpt.get();
        vehiculeDAO.updateDisponibilite(id, !vehicule.isDisponible());
    }

    public void markAsAvailable(int id) throws SQLException {
        vehiculeDAO.updateDisponibilite(id, true);
    }

    public void markAsUnavailable(int id) throws SQLException {
        vehiculeDAO.updateDisponibilite(id, false);
    }

    public Map<TypePermis, Long> countByType() throws SQLException {
        return vehiculeDAO.countByType();
    }

    public long countAvailable() throws SQLException {
        return vehiculeDAO.countAvailable();
    }

    public List<Vehicule> searchVehicules(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return getAllVehicules();
        }

        String lowerQuery = query.toLowerCase().trim();
        return getAllVehicules().stream()
                .filter(vehicule ->
                        vehicule.getMatricule().toLowerCase().contains(lowerQuery) ||
                                vehicule.getMarque().toLowerCase().contains(lowerQuery) ||
                                vehicule.getModele().toLowerCase().contains(lowerQuery)
                )
                .collect(Collectors.toList());
    }

    public boolean isAvailableForDate(int vehiculeId, LocalDate date) throws SQLException {
        List<Vehicule> available = vehiculeDAO.findAvailableByTypeAndDate(
                getVehiculeById(vehiculeId).get().getType(),
                date
        );
        return available.stream().anyMatch(v -> v.getId() == vehiculeId);
    }
}