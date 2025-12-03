package com.example.bbuniversity.models;

public class Matiere {

    private String _id;  // ex: "NOSQL"
    private String nom;  // ex: "NOSQL"

    public Matiere() {
        // constructeur vide obligatoire pour Retrofit / Gson
    }

    public Matiere(String _id, String nom) {
        this._id = _id;
        this.nom = nom;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return nom; // utile pour le spinner / autocomplete
    }
}
