package tn.spring.autoecole.models;

import tn.spring.autoecole.models.enums.TypePermis;
import java.util.Objects;

public class Vehicule {
    private int id;
    private String matricule;
    private String marque;
    private String modele;
    private TypePermis type;
    private boolean disponible;

    // Constructeurs
    public Vehicule() {}

    public Vehicule(int id, String matricule, String marque, String modele,
                    TypePermis type, boolean disponible) {
        this.id = id;
        this.matricule = matricule;
        this.marque = marque;
        this.modele = modele;
        this.type = type;
        this.disponible = disponible;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public TypePermis getType() { return type; }
    public void setType(TypePermis type) { this.type = type; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    // Méthodes utilitaires
    public String getInfoComplete() {
        return marque + " " + modele + " (" + matricule + ")";
    }

    public String getStatutDisponibilite() {
        return disponible ? "✓ Disponible" : "✗ Non disponible";
    }

    @Override
    public String toString() {
        return matricule + " - " + marque + " " + modele +
                " [" + type.getDisplayName() + "] " +
                (disponible ? "✓" : "✗");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return id == vehicule.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

