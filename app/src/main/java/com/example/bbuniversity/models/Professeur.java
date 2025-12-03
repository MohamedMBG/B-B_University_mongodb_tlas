package com.example.bbuniversity.models;

import java.util.List;
import java.util.Map;

public class Professeur extends User {

    private String departement;
    private String adresse;

    // clé = nom de la matière, valeur = liste des classes concernées
    private Map<String, List<String>> enseignement;

    public Professeur() {
        // Obligatoire pour Firestore / Gson
    }

    /**
     * idOrUid : tu peux passer le uid Firebase ou l'_id Mongo
     * On l'utilise pour set_id() ET setUid() pour que tout reste synchro.
     */
    public Professeur(String idOrUid,
                      String nom,
                      String prenom,
                      String email,
                      String departement,
                      String adresse,
                      Map<String, List<String>> enseignement) {

        // On remplit les champs hérités de User
        set_id(idOrUid);      // pour Mongo (_id)
        setUid(idOrUid);      // pour compat Firebase uid
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
        setRole("professor"); // fixé ici, plus besoin de le passer en param

        // Champs spécifiques professeur
        this.departement = departement;
        this.adresse = adresse;
        this.enseignement = enseignement;
    }

    // --- Getters / Setters spécifiques ---

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Map<String, List<String>> getEnseignement() {
        return enseignement;
    }

    public void setEnseignement(Map<String, List<String>> enseignement) {
        this.enseignement = enseignement;
    }
}
