package tn.spring.autoecole.dao;

import tn.spring.autoecole.models.Vehicule;
import tn.spring.autoecole.models.enums.TypePermis;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VehiculeDAO {

    private Connection cnx = SingletonConnection.getInstance();

    public Optional<Vehicule> findById(int id) throws SQLException {
        String query = "SELECT * FROM vehicules WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehicule(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Vehicule> findAll() throws SQLException {
        String query = "SELECT * FROM vehicules ORDER BY type, marque, modele";
        List<Vehicule> vehicules = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
        }
        return vehicules;
    }

    public List<Vehicule> findAvailable() throws SQLException {
        String query = "SELECT * FROM vehicules WHERE disponible = TRUE ORDER BY type, marque";
        List<Vehicule> vehicules = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
        }
        return vehicules;
    }

    public List<Vehicule> findByType(TypePermis type) throws SQLException {
        String query = "SELECT * FROM vehicules WHERE type = ? ORDER BY marque, modele";
        List<Vehicule> vehicules = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        }
        return vehicules;
    }

    public List<Vehicule> findAvailableByType(TypePermis type) throws SQLException {
        String query = "SELECT * FROM vehicules WHERE type = ? AND disponible = TRUE ORDER BY marque, modele";
        List<Vehicule> vehicules = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        }
        return vehicules;
    }

    public List<Vehicule> findAvailableByTypeAndDate(TypePermis type, LocalDate date) throws SQLException {
        String query = """
            SELECT v.* FROM vehicules v
            WHERE v.type = ? AND v.disponible = TRUE
            AND v.id NOT IN (
                SELECT vehicule_id FROM seances 
                WHERE date_seance = ? AND vehicule_id IS NOT NULL
            )
            ORDER BY v.marque, v.modele
            """;

        List<Vehicule> vehicules = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, type.name());
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        }
        return vehicules;
    }

    public Vehicule save(Vehicule vehicule) throws SQLException {
        String query = "INSERT INTO vehicules (matricule, marque, modele, type, disponible) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehicule.getMatricule());
            stmt.setString(2, vehicule.getMarque());
            stmt.setString(3, vehicule.getModele());
            stmt.setString(4, vehicule.getType().name());
            stmt.setBoolean(5, vehicule.isDisponible());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La création du véhicule a échoué");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vehicule.setId(generatedKeys.getInt(1));
                }
            }
        }
        return vehicule;
    }

    public void update(Vehicule vehicule) throws SQLException {
        String query = "UPDATE vehicules SET matricule = ?, marque = ?, modele = ?, " +
                "type = ?, disponible = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, vehicule.getMatricule());
            stmt.setString(2, vehicule.getMarque());
            stmt.setString(3, vehicule.getModele());
            stmt.setString(4, vehicule.getType().name());
            stmt.setBoolean(5, vehicule.isDisponible());
            stmt.setInt(6, vehicule.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour a échoué, véhicule non trouvé");
            }
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM vehicules WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La suppression a échoué, véhicule non trouvé");
            }
        }
    }

    public boolean existsByMatricule(String matricule) throws SQLException {
        String query = "SELECT COUNT(*) FROM vehicules WHERE matricule = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, matricule);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean existsByMatriculeExcludingId(String matricule, int vehiculeId) throws SQLException {
        String query = "SELECT COUNT(*) FROM vehicules WHERE matricule = ? AND id != ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, matricule);
            stmt.setInt(2, vehiculeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void updateDisponibilite(int id, boolean disponible) throws SQLException {
        String query = "UPDATE vehicules SET disponible = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setBoolean(1, disponible);
            stmt.setInt(2, id);

            stmt.executeUpdate();
        }
    }

    public Map<TypePermis, Long> countByType() throws SQLException {
        String query = "SELECT type, COUNT(*) as count FROM vehicules GROUP BY type";
        Map<TypePermis, Long> counts = new HashMap<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TypePermis type = TypePermis.valueOf(rs.getString("type"));
                long count = rs.getLong("count");
                counts.put(type, count);
            }
        }
        return counts;
    }

    public long countAvailable() throws SQLException {
        String query = "SELECT COUNT(*) FROM vehicules WHERE disponible = TRUE";

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    private Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(rs.getInt("id"));
        vehicule.setMatricule(rs.getString("matricule"));
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));
        vehicule.setType(TypePermis.valueOf(rs.getString("type")));
        vehicule.setDisponible(rs.getBoolean("disponible"));
        return vehicule;
    }
}