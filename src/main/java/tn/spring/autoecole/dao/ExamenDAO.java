package tn.spring.autoecole.dao;

import tn.spring.autoecole.models.Examen;
import tn.spring.autoecole.models.enums.ResultatExamen;
import tn.spring.autoecole.models.enums.TypeSeance;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExamenDAO {

    private Connection cnx = SingletonConnection.getInstance();

    public Optional<Examen> findById(int id) throws SQLException {
        String query = """
            SELECT e.*, 
                   u.nom as apprenant_nom, u.prenom as apprenant_prenom
            FROM examens e
            LEFT JOIN users u ON e.apprenant_id = u.id
            WHERE e.id = ?
            """;

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExamen(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Examen> findAll() throws SQLException {
        String query = """
            SELECT e.*, 
                   u.nom as apprenant_nom, u.prenom as apprenant_prenom
            FROM examens e
            LEFT JOIN users u ON e.apprenant_id = u.id
            ORDER BY e.date_examen DESC, e.heure_examen DESC
            """;

        List<Examen> examens = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                examens.add(mapResultSetToExamen(rs));
            }
        }
        return examens;
    }

    public List<Examen> findByApprenant(int apprenantId) throws SQLException {
        String query = """
            SELECT e.*, 
                   u.nom as apprenant_nom, u.prenom as apprenant_prenom
            FROM examens e
            LEFT JOIN users u ON e.apprenant_id = u.id
            WHERE e.apprenant_id = ?
            ORDER BY e.date_examen DESC, e.heure_examen DESC
            """;

        List<Examen> examens = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, apprenantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(mapResultSetToExamen(rs));
                }
            }
        }
        return examens;
    }

    public List<Examen> findUpcomingByApprenant(int apprenantId) throws SQLException {
        String query = """
            SELECT e.*, 
                   u.nom as apprenant_nom, u.prenom as apprenant_prenom
            FROM examens e
            LEFT JOIN users u ON e.apprenant_id = u.id
            WHERE e.apprenant_id = ? AND e.date_examen >= CURDATE()
            ORDER BY e.date_examen, e.heure_examen
            """;

        List<Examen> examens = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, apprenantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(mapResultSetToExamen(rs));
                }
            }
        }
        return examens;
    }

    public List<Examen> findByDate(LocalDate date) throws SQLException {
        String query = """
            SELECT e.*, 
                   u.nom as apprenant_nom, u.prenom as apprenant_prenom
            FROM examens e
            LEFT JOIN users u ON e.apprenant_id = u.id
            WHERE e.date_examen = ?
            ORDER BY e.heure_examen
            """;

        List<Examen> examens = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(mapResultSetToExamen(rs));
                }
            }
        }
        return examens;
    }

    public List<Examen> findByResultat(ResultatExamen resultat) throws SQLException {
        String query = """
            SELECT e.*, 
                   u.nom as apprenant_nom, u.prenom as apprenant_prenom
            FROM examens e
            LEFT JOIN users u ON e.apprenant_id = u.id
            WHERE e.resultat = ?
            ORDER BY e.date_examen DESC
            """;

        List<Examen> examens = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, resultat.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(mapResultSetToExamen(rs));
                }
            }
        }
        return examens;
    }

    public Examen save(Examen examen) throws SQLException {
        String query = "INSERT INTO examens (type, date_examen, heure_examen, apprenant_id, resultat) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, examen.getType().name());
            stmt.setDate(2, Date.valueOf(examen.getDateExamen()));
            stmt.setTime(3, Time.valueOf(examen.getHeureExamen()));
            stmt.setInt(4, examen.getApprenantId());
            stmt.setString(5, examen.getResultat().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La création de l'examen a échoué");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    examen.setId(generatedKeys.getInt(1));
                }
            }
        }
        return examen;
    }

    public void update(Examen examen) throws SQLException {
        String query = "UPDATE examens SET type = ?, date_examen = ?, heure_examen = ?, " +
                "apprenant_id = ?, resultat = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, examen.getType().name());
            stmt.setDate(2, Date.valueOf(examen.getDateExamen()));
            stmt.setTime(3, Time.valueOf(examen.getHeureExamen()));
            stmt.setInt(4, examen.getApprenantId());
            stmt.setString(5, examen.getResultat().name());
            stmt.setInt(6, examen.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour a échoué, examen non trouvé");
            }
        }
    }

    public void updateResultat(int id, ResultatExamen resultat) throws SQLException {
        String query = "UPDATE examens SET resultat = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, resultat.name());
            stmt.setInt(2, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour a échoué, examen non trouvé");
            }
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM examens WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La suppression a échoué, examen non trouvé");
            }
        }
    }

    public long countByApprenant(int apprenantId) throws SQLException {
        String query = "SELECT COUNT(*) FROM examens WHERE apprenant_id = ?";

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

    public Map<ResultatExamen, Long> countByResultat() throws SQLException {
        String query = "SELECT resultat, COUNT(*) as count FROM examens GROUP BY resultat";
        Map<ResultatExamen, Long> counts = new HashMap<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ResultatExamen resultat = ResultatExamen.valueOf(rs.getString("resultat"));
                long count = rs.getLong("count");
                counts.put(resultat, count);
            }
        }
        return counts;
    }

    public double getSuccessRate() throws SQLException {
        String query = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN resultat = 'REUSSI' THEN 1 ELSE 0 END) as reussis
            FROM examens
            WHERE resultat IN ('REUSSI', 'ECHEC')
            """;

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                long total = rs.getLong("total");
                long reussis = rs.getLong("reussis");
                if (total > 0) {
                    return (double) reussis / total * 100;
                }
            }
        }
        return 0.0;
    }

    private Examen mapResultSetToExamen(ResultSet rs) throws SQLException {
        Examen examen = new Examen();
        examen.setId(rs.getInt("id"));
        examen.setType(TypeSeance.valueOf(rs.getString("type")));
        examen.setDateExamen(rs.getDate("date_examen").toLocalDate());
        examen.setHeureExamen(rs.getTime("heure_examen").toLocalTime());
        examen.setApprenantId(rs.getInt("apprenant_id"));
        examen.setResultat(ResultatExamen.valueOf(rs.getString("resultat")));

        // Set display name
        String apprenantPrenom = rs.getString("apprenant_prenom");
        String apprenantNom = rs.getString("apprenant_nom");
        if (apprenantPrenom != null && apprenantNom != null) {
            examen.setApprenantNom(apprenantPrenom + " " + apprenantNom);
        }

        return examen;
    }
}