package com.example.bbuniversity.etudiant;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.StudentNoteAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Complaint;
import com.example.bbuniversity.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentGradesActivity extends AppCompatActivity {

    // recycler showing all grade items
    private RecyclerView recyclerGrades;
    // adapter used by the recycler
    private StudentNoteAdapter adapter;
    // list containing the student's notes
    private final List<Note> notes = new ArrayList<>();

    // Mongo API
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_grades);
        EdgeToEdge.enable(this);

        // hide navigation bar
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        apiService = ApiClient.getClient().create(ApiService.class);

        // locate recycler view in layout
        recyclerGrades = findViewById(R.id.recyclerGrades);
        // create adapter handling complaints
        adapter = new StudentNoteAdapter(notes, this::showComplaintDialog);
        // vertical list configuration
        recyclerGrades.setLayoutManager(new LinearLayoutManager(this));
        // attach adapter
        recyclerGrades.setAdapter(adapter);

        // retrieve current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadNotesFromMongo(user.getUid());
        } else {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
        }
    }

    /* -------------------------------------------------
     *  CHARGEMENT DES NOTES (Mongo)
     * ------------------------------------------------- */

    /** Charge toutes les notes de l'étudiant depuis Mongo */
    private void loadNotesFromMongo(String uid) {
        apiService.getStudentSubjects(uid).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(StudentGradesActivity.this,
                            "Erreur de chargement des notes : code=" + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                notes.clear();
                notes.addAll(response.body());
                adapter.updateNotes(notes);
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                Toast.makeText(StudentGradesActivity.this,
                        "Erreur réseau notes : " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* -------------------------------------------------
     *  RÉCLAMATION
     * ------------------------------------------------- */

    /** Affiche une boîte de dialogue pour envoyer une plainte */
    private void showComplaintDialog(Note note) {
        final android.widget.EditText input = new android.widget.EditText(this);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Réclamation")
                .setMessage("Expliquez votre réclamation")
                .setView(input)
                .setPositiveButton("Envoyer", (dialog, which) -> {
                    String message = input.getText().toString().trim();
                    sendComplaint(note, message);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    /** Envoie la plainte à Mongo via l'API */
    private void sendComplaint(Note note, String description) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // version POJO pour l’API Mongo
        Complaint complaint = new Complaint();
        complaint.setStudentId(user.getUid());
        complaint.setTeacherId(note.getProfesseurId());
        complaint.setSubjectId(note.getMatiere());
        // Assure-toi que Note a un champ id (ou adapte ici)
        complaint.setNoteId(note.getId());
        complaint.setInitialGrade(note.getNoteGenerale());
        complaint.setModifiedGrade(note.getNoteGenerale());
        complaint.setTitle("Réclamation sur la note");
        complaint.setDescription(description);
        complaint.setResponse("");
        complaint.setStatus("pending");
        // createdAt / dateFiled seront gérés côté backend (new Date())

        apiService.createComplaint(complaint)
                .enqueue(new Callback<Complaint>() {
                    @Override
                    public void onResponse(Call<Complaint> call, Response<Complaint> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(StudentGradesActivity.this,
                                    "Réclamation envoyée", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(StudentGradesActivity.this,
                                    "Erreur API réclamation : code=" + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Complaint> call, Throwable t) {
                        Toast.makeText(StudentGradesActivity.this,
                                "Erreur réseau réclamation : " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
