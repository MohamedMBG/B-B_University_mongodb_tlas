package com.example.bbuniversity.models;

import java.util.List;

public class TimetableResponse {
    private String classe;                // ou "class" selon ton backend
    private List<TimetableEntry> entries;

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public List<TimetableEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TimetableEntry> entries) {
        this.entries = entries;
    }
}
