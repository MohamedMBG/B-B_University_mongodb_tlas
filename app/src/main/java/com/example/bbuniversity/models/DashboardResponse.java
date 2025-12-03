package com.example.bbuniversity.models;

import java.util.List;

public class DashboardResponse {
    private int totalStudents;
    private int totalTeachers;
    private int totalAbsences;
    private List<Note> recentNotes;

    public int getTotalStudents() {
        return totalStudents;
    }

    public int getTotalTeachers() {
        return totalTeachers;
    }

    public int getTotalAbsences() {
        return totalAbsences;
    }

    public List<Note> getRecentNotes() {
        return recentNotes;
    }
}
