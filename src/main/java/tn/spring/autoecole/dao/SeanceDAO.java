package tn.spring.autoecole.dao;

import tn.spring.autoecole.models.Seance;
import tn.spring.autoecole.models.enums.TypeSeance;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeanceDAO {

    private Connection cnx = SingletonConnection.getInstance();

    public Optional<Seance> findById(int id) throws SQLException {
        String query = """
            SELECT s.*, 
                   u_appr.nom as apprenant_nom, u_appr.prenom as apprenant_prenom,
                   u_mon.nom as moniteur_nom, u_mon.prenom as moniteur_prenom,
                   v.matricule, v.marque, v.modele
            FROM seances s
            LEFT JOIN users u_appr ON s.apprenant_id = u_appr.id
            LEFT JOIN users u_mon ON s.moniteur_id = u_mon.id
            LEFT JOIN vehicules v ON s.vehicule_id = v.id
            WHERE s.id = ?
            """;

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSeance(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Seance> findAll() throws SQLException {
        String query = """
            SELECT s.*, 
                   u_appr.nom as apprenant_nom, u_appr.prenom as apprenant_prenom,
                   u_mon.nom as moniteur_nom, u_mon.prenom as moniteur_prenom,
                   v.matricule, v.marque, v.modele
            FROM seances s
            LEFT JOIN users u_appr ON s.apprenant_id = u_appr.id
            LEFT JOIN users u_mon ON s.moniteur_id = u_mon.id
            LEFT JOIN vehicules v ON s.vehicule_id = v.id
            ORDER BY s.date_seance DESC, s.heure_debut DESC
            """;

        List<Seance> seances = new ArrayList<>();

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                seances.add(mapResultSetToSeance(rs));
            }
        }
        return seances;
    }

    public List<Seance> findByApprenant(int apprenantId) throws SQLException {
        String query = """
            SELECT s.*, 
                   u_appr.nom as apprenant_nom, u_appr.prenom as apprenant_prenom,
                   u_mon.nom as moniteur_nom, u_mon.prenom as moniteur_prenom,
                   v.matricule, v.marque, v.modele
            FROM seances s
            LEFT JOIN users u_appr ON s.apprenant_id = u_appr.id
            LEFT JOIN users u_mon ON s.moniteur_id = u_mon.id
            LEFT JOIN vehicules v ON s.vehicule_id = v.id
            WHERE s.apprenant_id = ?
            ORDER BY s.date_seance DESC, s.heure_debut DESC
            """;

        List<Seance> seances = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, apprenantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapResultSetToSeance(rs));
                }
            }
        }
        return seances;
    }

    public List<Seance> findByMoniteur(int moniteurId) throws SQLException {
        String query = """
            SELECT s.*, 
                   u_appr.nom as apprenant_nom, u_appr.prenom as apprenant_prenom,
                   u_mon.nom as moniteur_nom, u_mon.prenom as moniteur_prenom,
                   v.matricule, v.marque, v.modele
            FROM seances s
            LEFT JOIN users u_appr ON s.apprenant_id = u_appr.id
            LEFT JOIN users u_mon ON s.moniteur_id = u_mon.id
            LEFT JOIN vehicules v ON s.vehicule_id = v.id
            WHERE s.moniteur_id = ?
            ORDER BY s.date_seance DESC, s.heure_debut DESC
            """;

        List<Seance> seances = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, moniteurId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapResultSetToSeance(rs));
                }
            }
        }
        return seances;
    }

    public List<Seance> findByDate(LocalDate date) throws SQLException {
        String query = """
            SELECT s.*, 
                   u_appr.nom as apprenant_nom, u_appr.prenom as apprenant_prenom,
                   u_mon.nom as moniteur_nom, u_mon.prenom as moniteur_prenom,
                   v.matricule, v.marque, v.modele
            FROM seances s
            LEFT JOIN users u_appr ON s.apprenant_id = u_appr.id
            LEFT JOIN users u_mon ON s.moniteur_id = u_mon.id
            LEFT JOIN vehicules v ON s.vehicule_id = v.id
            WHERE s.date_seance = ?
            ORDER BY s.heure_debut
            """;

        List<Seance> seances = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapResultSetToSeance(rs));
                }
            }
        }
        return seances;
    }

    public List<Seance> findUpcomingByApprenant(int apprenantId) throws SQLException {
        String query = """
            SELECT s.*, 
                   u_appr.nom as apprenant_nom, u_appr.prenom as apprenant_prenom,
                   u_mon.nom as moniteur_nom, u_mon.prenom as moniteur_prenom,
                   v.matricule, v.marque, v.modele
            FROM seances s
            LEFT JOIN users u_appr ON s.apprenant_id = u_appr.id
            LEFT JOIN users u_mon ON s.moniteur_id = u_mon.id
            LEFT JOIN vehicules v ON s.vehicule_id = v.id
            WHERE s.apprenant_id = ? AND s.date_seance >= CURDATE()
            ORDER BY s.date_seance, s.heure_debut
            """;

        List<Seance> seances = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, apprenantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapResultSetToSeance(rs));
                }
            }
        }
        return seances;
    }

    public Seance save(Seance seance) throws SQLException {
        String query = "INSERT INTO seances (type, date_seance, heure_debut, heure_fin, " +
                "apprenant_id, moniteur_id, vehicule_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, seance.getType().name());
            stmt.setDate(2, Date.valueOf(seance.getDateSeance()));
            stmt.setTime(3, Time.valueOf(seance.getHeureDebut()));
            stmt.setTime(4, Time.valueOf(seance.getHeureFin()));
            stmt.setInt(5, seance.getApprenantId());

            if (seance.getMoniteurId() != null) {
                stmt.setInt(6, seance.getMoniteurId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            if (seance.getVehiculeId() != null) {
                stmt.setInt(7, seance.getVehiculeId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La création de la séance a échoué");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    seance.setId(generatedKeys.getInt(1));
                }
            }
        }
        return seance;
    }

    public void update(Seance seance) throws SQLException {
        String query = "UPDATE seances SET type = ?, date_seance = ?, heure_debut = ?, heure_fin = ?, " +
                "apprenant_id = ?, moniteur_id = ?, vehicule_id = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, seance.getType().name());
            stmt.setDate(2, Date.valueOf(seance.getDateSeance()));
            stmt.setTime(3, Time.valueOf(seance.getHeureDebut()));
            stmt.setTime(4, Time.valueOf(seance.getHeureFin()));
            stmt.setInt(5, seance.getApprenantId());

            if (seance.getMoniteurId() != null) {
                stmt.setInt(6, seance.getMoniteurId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            if (seance.getVehiculeId() != null) {
                stmt.setInt(7, seance.getVehiculeId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setInt(8, seance.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour a échoué, séance non trouvée");
            }
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM seances WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La suppression a échoué, séance non trouvée");
            }
        }
    }

    public long countByApprenant(int apprenantId) throws SQLException {
        String query = "SELECT COUNT(*) FROM seances WHERE apprenant_id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, apprenantId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    public long countByMoniteur(int moniteurId) throws SQLException {
        String query = "SELECT COUNT(*) FROM seances WHERE moniteur_id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, moniteurId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    private Seance mapResultSetToSeance(ResultSet rs) throws SQLException {
        Seance seance = new Seance();
        seance.setId(rs.getInt("id"));
        seance.setType(TypeSeance.valueOf(rs.getString("type")));
        seance.setDateSeance(rs.getDate("date_seance").toLocalDate());
        seance.setHeureDebut(rs.getTime("heure_debut").toLocalTime());

        // Gérer heure_fin qui peut être NULL pour anciennes données
        Time heureFin = rs.getTime("heure_fin");
        if (heureFin != null) {
            seance.setHeureFin(heureFin.toLocalTime());
        } else {
            // Si pas d'heure fin, mettre heure_debut + 1h par défaut
            seance.setHeureFin(seance.getHeureDebut().plusHours(1));
        }

        seance.setApprenantId(rs.getInt("apprenant_id"));

        int moniteurId = rs.getInt("moniteur_id");
        if (!rs.wasNull()) {
            seance.setMoniteurId(moniteurId);
        }

        int vehiculeId = rs.getInt("vehicule_id");
        if (!rs.wasNull()) {
            seance.setVehiculeId(vehiculeId);
        }

        // Set display names
        String apprenantPrenom = rs.getString("apprenant_prenom");
        String apprenantNom = rs.getString("apprenant_nom");
        if (apprenantPrenom != null && apprenantNom != null) {
            seance.setApprenantNom(apprenantPrenom + " " + apprenantNom);
        }

        String moniteurPrenom = rs.getString("moniteur_prenom");
        String moniteurNom = rs.getString("moniteur_nom");
        if (moniteurPrenom != null && moniteurNom != null) {
            seance.setMoniteurNom(moniteurPrenom + " " + moniteurNom);
        }

        String matricule = rs.getString("matricule");
        String marque = rs.getString("marque");
        String modele = rs.getString("modele");
        if (matricule != null) {
            seance.setVehiculeInfo(matricule + " - " + marque + " " + modele);
        }

        return seance;
    }
}