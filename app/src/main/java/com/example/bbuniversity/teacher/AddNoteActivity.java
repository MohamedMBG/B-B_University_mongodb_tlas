package com.example.bbuniversity.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Note;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etParticipation, etControle, etExamen;
    private Button btnSave;

    private ApiService apiService;

    // ⚠️ ICI maintenant : UID de l'étudiant (pas l'email)
    private String studentId;
    private String professeurId;
    private String matiereId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_note);

        etParticipation = findViewById(R.id.etParticipation);
        etControle      = findViewById(R.id.etControle);
        etExamen        = findViewById(R.id.etExamen);
        btnSave         = findViewById(R.id.btnAddNote);

        apiService = ApiClient.getClient().create(ApiService.class);

        // 👉 récupère le UID de l'étudiant, envoyé par ClassStudentsActivity
        studentId    = getIntent().getStringExtra("studentId");   // UID
        professeurId = getIntent().getStringExtra("professorId");
        matiereId    = getIntent().getStringExtra("matiereId");

        android.util.Log.d("AddNoteActivity", "EXTRAS => studentId(uid)=" + studentId
                + ", profId=" + professeurId
                + ", matiereId=" + matiereId);

        if (studentId == null || professeurId == null || matiereId == null) {
            Toast.makeText(this,
                    "BUG interne : infos manquantes\nstudentId=" + studentId +
                            "\nprofId=" + professeurId +
                            "\nmatiereId=" + matiereId,
                    Toast.LENGTH_LONG).show();
        }

        // 1. Récupérer le bouton par son ID
        Button btnCancel = findViewById(R.id.btnCancel);

        // 2. Ajouter l'écouteur de clic (Listener)
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ferme l'activité actuelle et retourne à la précédente
                finish();
            }
        });


        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String partStr = etParticipation.getText().toString().trim();
        String ctrlStr = etControle.getText().toString().trim();
        String examStr = etExamen.getText().toString().trim();

        if (partStr.isEmpty() || ctrlStr.isEmpty() || examStr.isEmpty()) {
            Toast.makeText(this, "Remplir toutes les notes", Toast.LENGTH_SHORT).show();
            return;
        }

        double participation, controle, examenFinal;
        try {
            participation = Double.parseDouble(partStr.replace(",", "."));
            controle      = Double.parseDouble(ctrlStr.replace(",", "."));
            examenFinal   = Double.parseDouble(examStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Notes invalides", Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentId == null || professeurId == null || matiereId == null) {
            Toast.makeText(this,
                    "Impossible d’envoyer la note (infos manquantes)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        double noteGenerale = (participation + controle + examenFinal) / 3.0;

        Note note = new Note();
        note.setStudentId(studentId);          // ✅ UID
        note.setProfesseurId(professeurId);
        note.setMatiere(matiereId);
        note.setParticipation(participation);
        note.setControle(controle);
        note.setExamenFinal(examenFinal);
        note.setNoteGenerale(noteGenerale);

        android.util.Log.d("AddNoteActivity",
                "Sending note => studentId(uid)=" + studentId +
                        ", profId=" + professeurId +
                        ", matiere=" + matiereId);

        apiService.createOrUpdateNote(note).enqueue(new Callback<Note>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddNoteActivity.this,
                            String.format(Locale.FRANCE,
                                    "Note enregistrée (%.1f)", noteGenerale),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddNoteActivity.this,
                            "Erreur API notes code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onFailure(Call<Note> call, Throwable t) {
                Toast.makeText(AddNoteActivity.this,
                        "Erreur réseau: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
