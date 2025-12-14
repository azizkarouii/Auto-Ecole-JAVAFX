package tn.spring.autoecole.utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{8}$");

    private static final Pattern CIN_PATTERN =
            Pattern.compile("^[0-9]{8}$");

    private static final Pattern MATRICULE_PATTERN =
            Pattern.compile("^[0-9]{1,4}\\s?TUN\\s?[0-9]{1,4}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidCIN(String cin) {
        if (cin == null || cin.trim().isEmpty()) {
            return false;
        }
        return CIN_PATTERN.matcher(cin.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }

    public static boolean isAdult(LocalDate dateNaissance) {
        if (dateNaissance == null) {
            return false;
        }
        return LocalDate.now().minusYears(18).isAfter(dateNaissance) ||
                LocalDate.now().minusYears(18).isEqual(dateNaissance);
    }

    public static boolean isValidMatricule(String matricule) {
        if (matricule == null || matricule.trim().isEmpty()) {
            return false;
        }
        return MATRICULE_PATTERN.matcher(matricule.trim()).matches();
    }

    public static String validateUser(String nom, String prenom, String cin,
                                      LocalDate dateNaissance, String email,
                                      String phone, String password) {
        if (!isValidName(nom)) {
            return "Le nom doit contenir au moins 2 caractères";
        }
        if (!isValidName(prenom)) {
            return "Le prénom doit contenir au moins 2 caractères";
        }
        if (!isValidCIN(cin)) {
            return "Le CIN doit contenir exactement 8 chiffres";
        }
        if (dateNaissance == null) {
            return "La date de naissance est obligatoire";
        }
        if (!isAdult(dateNaissance)) {
            return "L'utilisateur doit avoir au moins 18 ans";
        }
        if (!isValidEmail(email)) {
            return "L'adresse email est invalide";
        }
        if (!isValidPhone(phone)) {
            return "Le numéro de téléphone doit contenir 8 chiffres";
        }
        if (!isValidPassword(password)) {
            return "Le mot de passe doit contenir au moins 6 caractères";
        }
        return null;
    }

    public static String validateVehicule(String matricule, String marque, String modele) {
        if (!isValidMatricule(matricule)) {
            return "La matricule est invalide (format: 1234 TU 5678)";
        }
        if (marque == null || marque.trim().isEmpty()) {
            return "La marque est obligatoire";
        }
        if (modele == null || modele.trim().isEmpty()) {
            return "Le modèle est obligatoire";
        }
        return null;
    }

    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}