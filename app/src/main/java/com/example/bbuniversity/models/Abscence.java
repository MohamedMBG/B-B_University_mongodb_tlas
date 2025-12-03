package com.example.bbuniversity.models;

public class Abscence {

    // correspond à _id dans Mongo
    private String _id;

    // UID Firebase de l'étudiant (ce qu’on met dans studentId dans l’URL)
    private String studentUserId;

    // Email de l’étudiant (facultatif mais utile)
    private String studentEmail;

    // Nom de la matière (ce que l’admin choisit dans etMatiere)
    private String matiere;

    // Date au format texte (on renverra un string ISO côté API)
    private String date;

    // Absence justifiée ou non
    private boolean justifiee;

    public Abscence() {}

    // -------- GETTERS / SETTERS --------

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(String studentUserId) {
        this.studentUserId = studentUserId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isJustifiee() {
        return justifiee;
    }

    public void setJustifiee(boolean justifiee) {
        this.justifiee = justifiee;
    }
}
