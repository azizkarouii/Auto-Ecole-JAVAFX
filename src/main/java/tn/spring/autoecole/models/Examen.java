package tn.spring.autoecole.models;

import tn.spring.autoecole.models.enums.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Examen {
    private int id;
    private TypeSeance type;
    private LocalDate dateExamen;
    private LocalTime heureExamen;
    private int apprenantId;
    private ResultatExamen resultat;

    // Propriété pour l'affichage
    private String apprenantNom;

    // Constructeurs
    public Examen() {}

    public Examen(int id, TypeSeance type, LocalDate dateExamen, LocalTime heureExamen,
                  int apprenantId, ResultatExamen resultat) {
        this.id = id;
        this.type = type;
        this.dateExamen = dateExamen;
        this.heureExamen = heureExamen;
        this.apprenantId = apprenantId;
        this.resultat = resultat;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public TypeSeance getType() { return type; }
    public void setType(TypeSeance type) { this.type = type; }

    public LocalDate getDateExamen() { return dateExamen; }
    public void setDateExamen(LocalDate dateExamen) { this.dateExamen = dateExamen; }

    public LocalTime getHeureExamen() { return heureExamen; }
    public void setHeureExamen(LocalTime heureExamen) { this.heureExamen = heureExamen; }

    public int getApprenantId() { return apprenantId; }
    public void setApprenantId(int apprenantId) { this.apprenantId = apprenantId; }

    public ResultatExamen getResultat() { return resultat; }
    public void setResultat(ResultatExamen resultat) { this.resultat = resultat; }

    public String getApprenantNom() { return apprenantNom; }
    public void setApprenantNom(String apprenantNom) { this.apprenantNom = apprenantNom; }

    // Méthodes utilitaires
    public String getDateFormatee() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateExamen.format(formatter);
    }

    public String getHeureFormatee() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return heureExamen.format(formatter);
    }

    public String getDateHeureFormatee() {
        return getDateFormatee() + " à " + getHeureFormatee();
    }

    public boolean isPassed() {
        LocalDate now = LocalDate.now();
        if (dateExamen.isBefore(now)) {
            return true;
        }
        if (dateExamen.isEqual(now)) {
            return heureExamen.isBefore(LocalTime.now());
        }
        return false;
    }

    public boolean isToday() {
        return dateExamen.isEqual(LocalDate.now());
    }

    public boolean isFuture() {
        return !isPassed();
    }

    public boolean isEnAttente() {
        return resultat == ResultatExamen.EN_ATTENTE;
    }

    public boolean isReussi() {
        return resultat == ResultatExamen.REUSSI;
    }

    public boolean isEchec() {
        return resultat == ResultatExamen.ECHEC;
    }

    public String getResultatIcon() {
        return switch (resultat) {
            case REUSSI -> "✓";
            case ECHEC -> "✗";
            case EN_ATTENTE -> "⏳";
        };
    }

    public String getResultatComplet() {
        return getResultatIcon() + " " + resultat.getDisplayName();
    }

    @Override
    public String toString() {
        return "Examen " + type.getDisplayName() + " - " + getDateHeureFormatee() +
                " - " + resultat.getDisplayName() +
                " - " + (apprenantNom != null ? apprenantNom : "Apprenant #" + apprenantId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Examen examen = (Examen) o;
        return id == examen.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
