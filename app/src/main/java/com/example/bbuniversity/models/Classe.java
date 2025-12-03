package com.example.bbuniversity.models;

public class Classe {

    private String _id;        // ex: "IIR3A"
    private String name;       // ex: "3IIR A"

    public Classe() {
    }

    public Classe(String _id, String name, String codeClasse) {
        this._id = _id;
        this.name = name;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name != null ? name : _id; // pour afficher dans le dropdown
    }
}
