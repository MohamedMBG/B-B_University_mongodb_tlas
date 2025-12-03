package com.example.bbuniversity.models;

public class Note {

    private String _id;           // optionnel
    private String studentId;
    private String professeurId;
    private String matiere;       // ex: "MATH_101"

    private double participation;
    private double controle;
    private double examenFinal;
    private double noteGenerale;

    private String derniereMiseAJour; // ou Date si tu veux

    public Note() {
        // requis pour Gson
    }

    public String getId() {
        return _id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getProfesseurId() {
        return professeurId;
    }

    public String getMatiere() {
        return matiere;
    }

    public double getParticipation() {
        return participation;
    }

    public double getControle() {
        return controle;
    }

    public double getExamenFinal() {
        return examenFinal;
    }

    public double getNoteGenerale() {
        return noteGenerale;
    }

    public String getDerniereMiseAJour() {
        return derniereMiseAJour;
    }

    // setters utiles si tu construis l’objet côté Java
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setProfesseurId(String professeurId) {
        this.professeurId = professeurId;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public void setParticipation(double participation) {
        this.participation = participation;
    }

    public void setControle(double controle) {
        this.controle = controle;
    }

    public void setExamenFinal(double examenFinal) {
        this.examenFinal = examenFinal;
    }

    public void setNoteGenerale(double noteGenerale) {
        this.noteGenerale = noteGenerale;
    }

    public void setDerniereMiseAJour(String derniereMiseAJour) {
        this.derniereMiseAJour = derniereMiseAJour;
    }
}
