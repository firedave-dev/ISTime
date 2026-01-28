package com.technolab.istime.model;

public class Cours {
    private int id;
    private String filiere;
    private String professeur;
    private String matiere;
    private String salle;
    private String jour;
    private String creneau;

    public Cours(int id, String filiere, String professeur, String matiere, String salle, String jour, String creneau) {
        this.id = id;
        this.filiere = filiere;
        this.professeur = professeur;
        this.matiere = matiere;
        this.salle = salle;
        this.jour = jour;
        this.creneau = creneau;
    }
    public int getId() { 
        return id;
     }
    public String getFiliere() {
        return filiere; 
    }
    public String getProfesseur() { 
        return professeur; 
    }
    public String getMatiere() { 
        return matiere; 
    }
    public String getSalle() {
        return salle;
     }
    public String getJour() {
        return jour; 
    }
    public String getCreneau() {
        return creneau;
    }
}
