package tn.spring.autoecole.models;

import tn.spring.autoecole.models.enums.TypeSeance;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Seance {
    private int id;
    private TypeSeance type;
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private int apprenantId;
    private Integer moniteurId;
    private Integer vehiculeId;

    // Propriétés pour l'affichage
    private String apprenantNom;
    private String moniteurNom;
    private String vehiculeInfo;

    // Constructeurs
    public Seance() {
    }

    public Seance(int id, TypeSeance type, LocalDate dateSeance, LocalTime heureDebut, LocalTime heureFin,
                  int apprenantId, Integer moniteurId, Integer vehiculeId) {
        this.id = id;
        this.type = type;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.apprenantId = apprenantId;
        this.moniteurId = moniteurId;
        this.vehiculeId = vehiculeId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TypeSeance getType() {
        return type;
    }

    public void setType(TypeSeance type) {
        this.type = type;
    }

    public LocalDate getDateSeance() {
        return dateSeance;
    }

    public void setDateSeance(LocalDate dateSeance) {
        this.dateSeance = dateSeance;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    // Compatibilité avec l'ancien code (heureSeance devient heureDebut)
    public LocalTime getHeureSeance() {
        return heureDebut;
    }

    public void setHeureSeance(LocalTime heureSeance) {
        this.heureDebut = heureSeance;
    }

    public int getApprenantId() {
        return apprenantId;
    }

    public void setApprenantId(int apprenantId) {
        this.apprenantId = apprenantId;
    }

    public Integer getMoniteurId() {
        return moniteurId;
    }

    public void setMoniteurId(Integer moniteurId) {
        this.moniteurId = moniteurId;
    }

    public Integer getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Integer vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public String getApprenantNom() {
        return apprenantNom;
    }

    public void setApprenantNom(String apprenantNom) {
        this.apprenantNom = apprenantNom;
    }

    public String getMoniteurNom() {
        return moniteurNom;
    }

    public void setMoniteurNom(String moniteurNom) {
        this.moniteurNom = moniteurNom;
    }

    public String getVehiculeInfo() {
        return vehiculeInfo;
    }

    public void setVehiculeInfo(String vehiculeInfo) {
        this.vehiculeInfo = vehiculeInfo;
    }

    // Méthodes utilitaires
    public String getDateFormatee() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateSeance.format(formatter);
    }

    public String getHeureFormatee() {
        if (heureDebut == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String debut = heureDebut.format(formatter);
        if (heureFin != null) {
            String fin = heureFin.format(formatter);
            return debut + " - " + fin;
        }
        return debut;
    }

    public String getDateHeureFormatee() {
        return getDateFormatee() + " à " + getHeureFormatee();
    }

    /**
     * Calcule la durée de la séance en heures décimales
     * @return durée en heures (ex: 1.5 pour 1h30)
     */
    public double getDureeEnHeures() {
        if (heureDebut == null || heureFin == null) {
            return 0.0;
        }
        Duration duration = Duration.between(heureDebut, heureFin);
        return duration.toMinutes() / 60.0;
    }

    /**
     * Calcule la durée de la séance en minutes
     * @return durée en minutes
     */
    public long getDureeEnMinutes() {
        if (heureDebut == null || heureFin == null) {
            return 0;
        }
        Duration duration = Duration.between(heureDebut, heureFin);
        return duration.toMinutes();
    }

    /**
     * Retourne la durée formatée (ex: "1h30", "2h00")
     */
    public String getDureeFormatee() {
        if (heureDebut == null || heureFin == null) {
            return "N/A";
        }
        long minutes = getDureeEnMinutes();
        long heures = minutes / 60;
        long mins = minutes % 60;
        return String.format("%dh%02d", heures, mins);
    }

    /**
     * Calcule le coût de la séance selon le type
     * Code: 10 DT/heure
     * Conduite et Parc: 25 DT/heure
     */
    public double getCout() {
        double duree = getDureeEnHeures();
        return switch (type) {
            case CODE -> duree * 10.0;
            case CONDUITE, PARC -> duree * 25.0;
        };
    }

    public boolean requiresMoniteur() {
        return type == TypeSeance.CONDUITE || type == TypeSeance.PARC;
    }

    public boolean requiresVehicule() {
        return type == TypeSeance.CONDUITE || type == TypeSeance.PARC;
    }

    public boolean isPassed() {
        LocalDate now = LocalDate.now();
        if (dateSeance.isBefore(now)) {
            return true;
        }
        if (dateSeance.isEqual(now)) {
            return heureDebut != null && heureDebut.isBefore(LocalTime.now());
        }
        return false;
    }

    public boolean isToday() {
        return dateSeance.isEqual(LocalDate.now());
    }

    public boolean isFuture() {
        return !isPassed();
    }

    @Override
    public String toString() {
        return type.getDisplayName() + " - " + getDateHeureFormatee() + " - " + getDureeFormatee() +
                " - " + (apprenantNom != null ? apprenantNom : "Apprenant #" + apprenantId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seance seance = (Seance) o;
        return id == seance.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}