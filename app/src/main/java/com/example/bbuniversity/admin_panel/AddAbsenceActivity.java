package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Abscence;
import com.example.bbuniversity.models.Etudiant;
import com.example.bbuniversity.models.Matiere;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAbsenceActivity extends AppCompatActivity {

    private TextInputEditText etStudentEmail, etDate;
    private TextInputEditText etMatiere;
    private MaterialCheckBox cbJustified;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_absence);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        etStudentEmail = findViewById(R.id.etStudentEmail);
        etMatiere      = findViewById(R.id.etMatiere);
        etDate         = findViewById(R.id.etDate);
        cbJustified    = findViewById(R.id.cbJustified);
        MaterialButton btnAdd    = findViewById(R.id.btnAddAbsence);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        etMatiere.setFocusable(false);
        etMatiere.setOnClickListener(v -> showSubjectDialog());

        btnAdd.setOnClickListener(v -> addAbsence());
        btnCancel.setOnClickListener(v -> finish());
    }

    /** 📌 Matières uniquement depuis Mongo (/api/matieres) */
    private void showSubjectDialog() {
        apiService.getMatieres().enqueue(new Callback<java.util.List<Matiere>>() {
            @Override
            public void onResponse(Call<java.util.List<Matiere>> call,
                                   Response<java.util.List<Matiere>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AddAbsenceActivity.this,
                            "Erreur chargement matières (Mongo) code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                java.util.List<String> subjects = new java.util.ArrayList<>();
                for (Matiere m : response.body()) {
                    if (m.getNom() != null) {
                        subjects.add(m.getNom());
                    }
                }

                if (subjects.isEmpty()) {
                    Toast.makeText(AddAbsenceActivity.this,
                            "Aucune matière trouvée dans Mongo",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] array = subjects.toArray(new String[0]);
                new AlertDialog.Builder(AddAbsenceActivity.this)
                        .setTitle("Sélectionner la matière")
                        .setItems(array, (d, which) -> etMatiere.setText(array[which]))
                        .show();
            }

            @Override
            public void onFailure(Call<java.util.List<Matiere>> call, Throwable t) {
                Toast.makeText(AddAbsenceActivity.this,
                        "Erreur réseau (matieres Mongo): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Quand l’admin clique sur "Ajouter" */
    private void addAbsence() {
        String email    = etStudentEmail.getText().toString().trim();
        String matiere  = etMatiere.getText().toString().trim();
        String dateStr  = etDate.getText().toString().trim();
        boolean justified = cbJustified.isChecked();

        if (email.isEmpty() || matiere.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Juste vérifier le format, mais on envoie la string au backend
        try {
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Format de date invalide (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            return;
        }

        // 👉 Étape 1 : chercher l’étudiant dans Mongo par EMAIL
        apiService.getStudents().enqueue(new Callback<java.util.List<Etudiant>>() {
            @Override
            public void onResponse(Call<java.util.List<Etudiant>> call,
                                   Response<java.util.List<Etudiant>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AddAbsenceActivity.this,
                            "Erreur chargement étudiants (Mongo) code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Etudiant target = null;
                for (Etudiant e : response.body()) {
                    if (e.getEmail() != null &&
                            e.getEmail().equalsIgnoreCase(email)) {
                        target = e;
                        break;
                    }
                }

                if (target == null) {
                    Toast.makeText(AddAbsenceActivity.this,
                            "Étudiant introuvable (email inexistant en Mongo)",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // On a trouvé l'étudiant → _id = studentUserId
                String studentId = target.getUid();
                sendAbsenceToMongo(studentId, target.getEmail(), matiere, dateStr, justified);
            }

            @Override
            public void onFailure(Call<java.util.List<Etudiant>> call, Throwable t) {
                Toast.makeText(AddAbsenceActivity.this,
                        "Erreur réseau (getStudents): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Étape 2 : envoyer l’absence dans Mongo pour cet étudiant */
    private void sendAbsenceToMongo(String uid, String email,
                                    String matiere, String dateStr, boolean justified) {

        Abscence absence = new Abscence();
        absence.setStudentUserId(uid);      // ⚠ utilisé par /api/students/{uid}/absences GET
        absence.setStudentEmail(email);
        absence.setMatiere(matiere);
        absence.setDate(dateStr);           // "dd/MM/yyyy" → parsé côté Node
        absence.setJustifiee(justified);

        apiService.addAbsence(uid, absence)
                .enqueue(new Callback<Abscence>() {
                    @Override
                    public void onResponse(Call<Abscence> call, Response<Abscence> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddAbsenceActivity.this,
                                    "Absence enregistrée (Mongo)",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = "Erreur API Mongo: code=" + response.code();
                            Toast.makeText(AddAbsenceActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Abscence> call, Throwable t) {
                        Toast.makeText(AddAbsenceActivity.this,
                                "Erreur réseau Mongo: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
