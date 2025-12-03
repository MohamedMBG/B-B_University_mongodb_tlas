package com.example.bbuniversity.models;

public class Etudiant extends User {

    public Etudiant() {
        // Obligatoire pour Firestore / Gson
    }

    public Etudiant(String idOrUid,
                    String nom,
                    String prenom,
                    String email,
                    int matricule,
                    int niveau,
                    String filiere,
                    String classe,
                    String codeClasse) {

        // On remplit le User (tu peux considérer idOrUid comme _id/uid)
        set_id(idOrUid);      // si tu utilises _id côté Mongo
        setUid(idOrUid);      // pour rester compatible avec FirebaseAuth uid
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
        setRole("student");

        setMatricule(matricule);
        setNiveau(niveau);
        setFiliere(filiere);
        setClasse(classe);
        setCodeClasse(codeClasse);
    }

    // Pas de nouveaux champs ici.
    // On utilise uniquement les getters/setters hérités de User :
    //
    // get_id(), getUid(), getNom(), getPrenom(), getEmail(), getRole()
    // getMatricule(), getNiveau(), getFiliere(), getClasse(), getCodeClasse()


}
