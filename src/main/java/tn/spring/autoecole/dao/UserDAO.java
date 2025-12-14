package tn.spring.autoecole.dao;

import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.enums.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private Connection cnx = SingletonConnection.getInstance();

    public Optional<User> findByEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE mail = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<User> findByRole(Role role) throws SQLException {
        String query = "SELECT * FROM users WHERE role = ? ORDER BY nom, prenom";
        List<User> users = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    public List<User> findAll() throws SQLException {
        String query = "SELECT * FROM users ORDER BY role, nom, prenom";
        List<User> users = new ArrayList<>();

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    public List<User> findApprenants() throws SQLException {
        return findByRole(Role.APPRENANT);
    }

    public List<User> findMoniteurs() throws SQLException {
        return findByRole(Role.MONITEUR);
    }

    public List<User> findAdmins() throws SQLException {
        return findByRole(Role.ADMIN);
    }

    public List<User> findApprenantsWithFilters(TypePermis typePermis, Niveau niveau) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM users WHERE role = 'APPRENANT'");

        if (typePermis != null) {
            query.append(" AND type_permis = ?");
        }
        if (niveau != null) {
            query.append(" AND niveau = ?");
        }
        query.append(" ORDER BY nom, prenom");

        List<User> users = new ArrayList<>();

        try (PreparedStatement stmt = cnx.prepareStatement(query.toString())) {
            int paramIndex = 1;
            if (typePermis != null) {
                stmt.setString(paramIndex++, typePermis.name());
            }
            if (niveau != null) {
                stmt.setString(paramIndex, niveau.name());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    public User save(User user) throws SQLException {
        String query = "INSERT INTO users (nom, prenom, cin, date_naissance, mail, " +
                "mot_de_passe, num_tel, role, type_permis, niveau, " +
                "heures_prevues_code, heures_prevues_conduite, heures_prevues_parc, frais_inscription_paye) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            setUserParameters(stmt, user);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La création de l'utilisateur a échoué");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }
        return user;
    }

    public void update(User user) throws SQLException {
        String query = "UPDATE users SET nom = ?, prenom = ?, cin = ?, " +
                "date_naissance = ?, mail = ?, mot_de_passe = ?, num_tel = ?, " +
                "role = ?, type_permis = ?, niveau = ?, " +
                "heures_prevues_code = ?, heures_prevues_conduite = ?, " +
                "heures_prevues_parc = ?, frais_inscription_paye = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            setUserParameters(stmt, user);
            stmt.setInt(15, user.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour a échoué, utilisateur non trouvé");
            }
        }
    }

    public void updateNiveau(int userId, Niveau niveau) throws SQLException {
        String query = "UPDATE users SET niveau = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, niveau.name());
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour du niveau a échoué");
            }
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La suppression a échoué, utilisateur non trouvé");
            }
        }
    }

    public boolean existsByCIN(String cin) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE cin = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, cin);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean existsByEmail(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE mail = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean existsByCINExcludingId(String cin, int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE cin = ? AND id != ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, cin);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean existsByEmailExcludingId(String email, int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE mail = ? AND id != ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public long countByRole(Role role) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE role = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    private void setUserParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getNom());
        stmt.setString(2, user.getPrenom());
        stmt.setString(3, user.getCin());
        stmt.setDate(4, Date.valueOf(user.getDateNaissance()));
        stmt.setString(5, user.getMail());
        stmt.setString(6, user.getMotDePasse());
        stmt.setString(7, user.getNumTel());
        stmt.setString(8, user.getRole().name());

        if (user.getTypePermis() != null) {
            stmt.setString(9, user.getTypePermis().name());
        } else {
            stmt.setNull(9, Types.VARCHAR);
        }

        if (user.getNiveau() != null) {
            stmt.setString(10, user.getNiveau().name());
        } else {
            stmt.setNull(10, Types.VARCHAR);
        }

        // NOUVEAUX CHAMPS
        stmt.setInt(11, user.getHeuresPreveuesCode());
        stmt.setInt(12, user.getHeuresPreveuesConduite());
        stmt.setInt(13, user.getHeuresPreveuesParc());
        stmt.setBoolean(14, user.isFraisInscriptionPaye());
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setCin(rs.getString("cin"));
        user.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
        user.setMail(rs.getString("mail"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        user.setNumTel(rs.getString("num_tel"));
        user.setRole(Role.valueOf(rs.getString("role")));

        String typePermis = rs.getString("type_permis");
        if (typePermis != null) {
            user.setTypePermis(TypePermis.valueOf(typePermis));
        }

        String niveau = rs.getString("niveau");
        if (niveau != null) {
            user.setNiveau(Niveau.valueOf(niveau));
        }

        // NOUVEAUX CHAMPS - avec valeurs par défaut si NULL
        int heuresCode = rs.getInt("heures_prevues_code");
        user.setHeuresPreveuesCode(rs.wasNull() ? 8 : heuresCode);

        int heuresConduite = rs.getInt("heures_prevues_conduite");
        user.setHeuresPreveuesConduite(rs.wasNull() ? 25 : heuresConduite);

        int heuresParc = rs.getInt("heures_prevues_parc");
        user.setHeuresPreveuesParc(rs.wasNull() ? 4 : heuresParc);

        user.setFraisInscriptionPaye(rs.getBoolean("frais_inscription_paye"));

        return user;
    }
}