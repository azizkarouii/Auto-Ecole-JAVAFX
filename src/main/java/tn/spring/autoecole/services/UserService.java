package tn.spring.autoecole.services;

import tn.spring.autoecole.dao.UserDAO;
import tn.spring.autoecole.models.User;
import tn.spring.autoecole.models.enums.*;
import tn.spring.autoecole.utils.PasswordUtils;
import tn.spring.autoecole.utils.ValidationUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public Optional<User> getUserById(int id) throws SQLException {
        return userDAO.findById(id);
    }

    public List<User> getAllApprenants() throws SQLException {
        return userDAO.findApprenants();
    }

    public List<User> getAllMoniteurs() throws SQLException {
        return userDAO.findMoniteurs();
    }

    public List<User> getAllAdmins() throws SQLException {
        return userDAO.findAdmins();
    }

    public List<User> getApprenantsFiltered(TypePermis typePermis, Niveau niveau) throws SQLException {
        return userDAO.findApprenantsWithFilters(typePermis, niveau);
    }

    public User createUser(User user) throws SQLException, IllegalArgumentException {
        String validationError = ValidationUtils.validateUser(
                user.getNom(),
                user.getPrenom(),
                user.getCin(),
                user.getDateNaissance(),
                user.getMail(),
                user.getNumTel(),
                user.getMotDePasse()
        );

        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (userDAO.existsByCIN(user.getCin())) {
            throw new IllegalArgumentException("Un utilisateur avec ce CIN existe déjà");
        }

        if (userDAO.existsByEmail(user.getMail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        if (user.getRole() == Role.APPRENANT) {
            if (user.getTypePermis() == null) {
                throw new IllegalArgumentException("Le type de permis est obligatoire pour un apprenant");
            }
            if (user.getNiveau() == null) {
                user.setNiveau(Niveau.CODE);
            }
        }

        user.setMotDePasse(PasswordUtils.hashPassword(user.getMotDePasse()));

        return userDAO.save(user);
    }

    public void updateUser(User user) throws SQLException, IllegalArgumentException {
        if (userDAO.findById(user.getId()).isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        String validationError = ValidationUtils.validateUser(
                user.getNom(),
                user.getPrenom(),
                user.getCin(),
                user.getDateNaissance(),
                user.getMail(),
                user.getNumTel(),
                user.getMotDePasse()
        );

        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (userDAO.existsByCINExcludingId(user.getCin(), user.getId())) {
            throw new IllegalArgumentException("Un autre utilisateur avec ce CIN existe déjà");
        }

        if (userDAO.existsByEmailExcludingId(user.getMail(), user.getId())) {
            throw new IllegalArgumentException("Un autre utilisateur avec cet email existe déjà");
        }

        userDAO.update(user);
    }

    public void deleteUser(int id) throws SQLException, IllegalArgumentException {
        if (userDAO.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }
        userDAO.delete(id);
    }

    public void updateApprenantNiveau(int apprenantId, Niveau niveau) throws SQLException {
        Optional<User> userOpt = userDAO.findById(apprenantId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Apprenant non trouvé");
        }

        User user = userOpt.get();
        if (user.getRole() != Role.APPRENANT) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un apprenant");
        }

        userDAO.updateNiveau(apprenantId, niveau);
    }

    public void progressApprenant(int apprenantId) throws SQLException {
        Optional<User> userOpt = userDAO.findById(apprenantId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Apprenant non trouvé");
        }

        User user = userOpt.get();
        if (user.getRole() != Role.APPRENANT) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un apprenant");
        }

        Niveau currentNiveau = user.getNiveau();
        if (currentNiveau == null) {
            currentNiveau = Niveau.CODE;
        }

        Niveau nextNiveau = currentNiveau.getNext();
        if (nextNiveau == null) {
            throw new IllegalArgumentException("L'apprenant a déjà obtenu son permis");
        }

        userDAO.updateNiveau(apprenantId, nextNiveau);
    }

    public long countByRole(Role role) throws SQLException {
        return userDAO.countByRole(role);
    }

    public List<User> searchUsers(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }

        String lowerQuery = query.toLowerCase().trim();
        return getAllUsers().stream()
                .filter(user ->
                        user.getNom().toLowerCase().contains(lowerQuery) ||
                                user.getPrenom().toLowerCase().contains(lowerQuery) ||
                                user.getCin().contains(lowerQuery) ||
                                user.getMail().toLowerCase().contains(lowerQuery)
                )
                .collect(Collectors.toList());
    }
}