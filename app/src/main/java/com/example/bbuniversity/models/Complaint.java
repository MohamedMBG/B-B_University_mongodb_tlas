package com.example.bbuniversity.models;

public class Complaint {

    private String _id;           // id MongoDB (ObjectId en string)
    private String studentId;     // uid Firebase ou id Mongo
    private String teacherId;     // uid prof
    private String subjectId;     // ex: "MATH_101"
    private String noteId;        // id de la note concernée

    private String title;         // "Réclamation sur la note"
    private String description;   // message de l'étudiant

    private double initialGrade;  // note avant réclamation
    private Double modifiedGrade; // note après traitement (nullable)

    private String status;        // "pending", "accepted", "rejected"
    private String response;      // réponse du prof

    private String dateFiled;     // String ISO venant du backend
    private String dateProcessed; // idem

    // Optionnel : pratique pour Firestore ou adapter
    private String documentPath;

    public Complaint() {
        // requis pour Gson / Firestore
    }

    // ---------- GETTERS / SETTERS ----------

    public String getId() {
        return _id;
    }
    public void setId(String id) {
        this._id = id;
    }

    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTeacherId() {
        return teacherId;
    }
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getNoteId() {
        return noteId;
    }
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public double getInitialGrade() {
        return initialGrade;
    }
    public void setInitialGrade(double initialGrade) {
        this.initialGrade = initialGrade;
    }

    public Double getModifiedGrade() {
        return modifiedGrade;
    }
    public void setModifiedGrade(Double modifiedGrade) {
        this.modifiedGrade = modifiedGrade;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }

    public String getDateFiled() {
        return dateFiled;
    }
    public void setDateFiled(String dateFiled) {
        this.dateFiled = dateFiled;
    }

    public String getDateProcessed() {
        return dateProcessed;
    }
    public void setDateProcessed(String dateProcessed) {
        this.dateProcessed = dateProcessed;
    }

    public String getDocumentPath() {
        return documentPath;
    }
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }
}
