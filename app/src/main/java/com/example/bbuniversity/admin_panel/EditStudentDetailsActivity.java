package com.example.bbuniversity.admin_panel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditStudentDetailsActivity extends AppCompatActivity {

    private TextInputEditText etNom, etPrenom, etEmail,
            etMatricule, etNiveau, etFiliere, etClasseCode;
    private MaterialButton btnSave;
    private String studentId;

    // ✅ API Mongo
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_student_details);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class); // ✅ init API Mongo
        studentId = getIntent().getStringExtra("studentId");

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadStudentDataFromMongo();  // ✅ maintenant on lit depuis Mongo

        btnSave.setOnClickListener(v -> saveStudentData());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(view -> finish());
    }

    private void initializeViews() {
        etNom        = findViewById(R.id.etNom);
        etPrenom     = findViewById(R.id.etPrenom);
        etEmail      = findViewById(R.id.etEmail);
        etMatricule  = findViewById(R.id.etMatricule);
        etNiveau     = findViewById(R.id.etNiveau);
        etFiliere    = findViewById(R.id.etFiliere);
        etClasseCode = findViewById(R.id.etClasseCode);
        btnSave      = findViewById(R.id.btnSave);

        // Email non modifiable (lié à Firebase Auth)
        etEmail.setEnabled(false);
    }

    // ⚡ 1) CHARGEMENT DEPUIS MONGODB
    private void loadStudentDataFromMongo() {
        apiService.getUser(studentId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(EditStudentDetailsActivity.this,
                            "Error loading student data (Mongo): code=" + response.code(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                populateFieldsFromUser(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(EditStudentDetailsActivity.this,
                        "Network error (Mongo): " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFieldsFromUser(User user) {
        if (user == null) return;

        etNom.setText(safe(user.getNom()));
        etPrenom.setText(safe(user.getPrenom()));
        etEmail.setText(safe(user.getEmail()));

        if (user.getMatricule() != null) {
            etMatricule.setText(String.valueOf(user.getMatricule()));
        } else {
            etMatricule.setText("");
        }

        if (user.getNiveau() != null) {
            etNiveau.setText(String.valueOf(user.getNiveau()));
        } else {
            etNiveau.setText("0");
        }

        etFiliere.setText(safe(user.getFiliere()));
        etClasseCode.setText(safe(user.getClasse()));  // tu peux mettre codeClasse si tu préfères
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    // ⚡ 2) SAUVEGARDE UNIQUEMENT SUR MONGODB
    private void saveStudentData() {
        String nom          = textOf(etNom);
        String prenom       = textOf(etPrenom);
        String email        = textOf(etEmail);
        String matriculeStr = textOf(etMatricule);
        String niveauStr    = textOf(etNiveau);
        String filiere      = textOf(etFiliere);
        String classeCode   = textOf(etClasseCode);

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || matriculeStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long matricule;
        int niveau;
        try {
            matricule = Long.parseLong(matriculeStr);
            niveau    = Integer.parseInt(niveauStr.isEmpty() ? "0" : niveauStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Matricule and Niveau must be valid numbers",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("nom", nom);
        updates.put("prenom", prenom);
        updates.put("matricule", matricule);
        updates.put("niveau", niveau);
        updates.put("filiere", filiere);
        updates.put("classe", classeCode);
        updates.put("role", "student");

        // 🔥 MAJ MongoDB via API
        apiService.updateUser(studentId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditStudentDetailsActivity.this,
                            "Student updated successfully (Mongo)",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditStudentDetailsActivity.this,
                            "Mongo error: code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditStudentDetailsActivity.this,
                        "Mongo network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
