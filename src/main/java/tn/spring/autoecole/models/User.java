package tn.spring.autoecole.models;

import tn.spring.autoecole.models.enums.*;
import java.time.LocalDate;
import java.util.Objects;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String cin;
    private LocalDate dateNaissance;
    private String mail;
    private String motDePasse;
    private String numTel;
    private Role role;
    private TypePermis typePermis;
    private Niveau niveau;

    // Heures prévues par phase
    private int heuresPreveuesCode;
    private int heuresPreveuesConduite;
    private int heuresPreveuesParc;

    // Frais d'inscription payé
    private boolean fraisInscriptionPaye;

    // Constructeurs
    public User() {
        // Valeurs par défaut
        this.heuresPreveuesCode = 8;
        this.heuresPreveuesConduite = 25;
        this.heuresPreveuesParc = 4;
        this.fraisInscriptionPaye = false;
    }

    public User(int id, String nom, String prenom, String cin, LocalDate dateNaissance,
                String mail, String motDePasse, String numTel, Role role,
                TypePermis typePermis, Niveau niveau) {
        this();
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.dateNaissance = dateNaissance;
        this.mail = mail;
        this.motDePasse = motDePasse;
        this.numTel = numTel;
        this.role = role;
        this.typePermis = typePermis;
        this.niveau = niveau;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public TypePermis getTypePermis() { return typePermis; }
    public void setTypePermis(TypePermis typePermis) { this.typePermis = typePermis; }

    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }

    public int getHeuresPreveuesCode() { return heuresPreveuesCode; }
    public void setHeuresPreveuesCode(int heuresPreveuesCode) { this.heuresPreveuesCode = heuresPreveuesCode; }

    public int getHeuresPreveuesConduite() { return heuresPreveuesConduite; }
    public void setHeuresPreveuesConduite(int heuresPreveuesConduite) { this.heuresPreveuesConduite = heuresPreveuesConduite; }

    public int getHeuresPreveuesParc() { return heuresPreveuesParc; }
    public void setHeuresPreveuesParc(int heuresPreveuesParc) { this.heuresPreveuesParc = heuresPreveuesParc; }

    public boolean isFraisInscriptionPaye() { return fraisInscriptionPaye; }
    public void setFraisInscriptionPaye(boolean fraisInscriptionPaye) { this.fraisInscriptionPaye = fraisInscriptionPaye; }

    // Méthodes utilitaires
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateNaissance.getYear();
    }

    public boolean isApprenant() {
        return role == Role.APPRENANT;
    }

    public boolean isMoniteur() {
        return role == Role.MONITEUR;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isSuperAdmin() {
        return role == Role.SUPERADMIN;
    }

    @Override
    public String toString() {
        return getNomComplet() + " (" + role.getDisplayName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}