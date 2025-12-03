package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Classe;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore; // encore utilisé si tu veux ailleurs

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateStudentActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etNom, etPassword, etPrenom;
    private TextInputEditText etMatricule, etFiliere, etNiveau;
    private Button btnCreateStudent, cancel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;   // plus utilisé pour les classes, mais tu peux le garder

    private AutoCompleteTextView classDropdown;
    private final List<String> classList = new ArrayList<>();

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_student);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        mAuth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance(); // plus nécessaire pour les classes
        apiService = ApiClient.getClient().create(ApiService.class);

        // Views
        etEmail     = findViewById(R.id.etEmail);
        etNom       = findViewById(R.id.etNom);
        etPassword  = findViewById(R.id.etPassword);
        etPrenom    = findViewById(R.id.etPrenom);
        etMatricule = findViewById(R.id.etMatricule);
        etFiliere   = findViewById(R.id.etFiliere);
        etNiveau    = findViewById(R.id.etNiveau);
        classDropdown     = findViewById(R.id.classDropdown);
        btnCreateStudent  = findViewById(R.id.btnCreateStudent);
        cancel            = findViewById(R.id.btnCancel);

        cancel.setOnClickListener(v -> finish());
        btnCreateStudent.setOnClickListener(v -> createStudent());

        // 👉 maintenant on charge depuis Mongo
        fetchClassesFromMongo();
    }

    /** Charge les classes depuis MongoDB via /api/classes */
    private void fetchClassesFromMongo() {
        apiService.getClassesMongo().enqueue(new Callback<List<Classe>>() {
            @Override
            public void onResponse(Call<List<Classe>> call,
                                   Response<List<Classe>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CreateStudentActivity.this,
                            "Erreur chargement classes (Mongo), code=" + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                classList.clear();
                for (Classe c : response.body()) {
                    if (c.getName() != null) {
                        classList.add(c.getName());   // ex : "4IIR2"
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        CreateStudentActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        classList
                );
                classDropdown.setAdapter(adapter);
                classDropdown.setOnClickListener(v -> classDropdown.showDropDown());
                classDropdown.setThreshold(1);
            }

            @Override
            public void onFailure(Call<List<Classe>> call, Throwable t) {
                Toast.makeText(CreateStudentActivity.this,
                        "Erreur réseau (Mongo classes): " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createStudent() {
        String email       = etEmail.getText().toString().trim();
        String nom         = etNom.getText().toString().trim();
        String password    = etPassword.getText().toString().trim();
        String prenom      = etPrenom.getText().toString().trim();
        String matriculeStr= etMatricule.getText().toString().trim();
        String filiere     = etFiliere.getText().toString().trim();
        String classe      = classDropdown.getText().toString().trim();
        String niveauStr   = etNiveau.getText().toString().trim();

        if (email.isEmpty() || nom.isEmpty() || password.isEmpty() || prenom.isEmpty()
                || matriculeStr.isEmpty() || filiere.isEmpty() || classe.isEmpty()
                || niveauStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int matricule = Integer.parseInt(matriculeStr);
            int niveau    = Integer.parseInt(niveauStr);

            String codeClasse = niveauStr + filiere + classe;

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful() && mAuth.getCurrentUser() != null) {
                            String uid = mAuth.getCurrentUser().getUid();

                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(prenom + " " + nom)
                                            .build();

                            mAuth.getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            createMongoStudent(uid, email, nom, prenom,
                                                    matricule, filiere, classe, niveau, codeClasse);
                                        } else {
                                            Toast.makeText(this,
                                                    "Erreur profil: " + profileTask.getException(),
                                                    Toast.LENGTH_SHORT).show();
                                            mAuth.getCurrentUser().delete();
                                        }
                                    });
                        } else {
                            Toast.makeText(this,
                                    "Erreur création compte: " + authTask.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Matricule et niveau doivent être des nombres",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void createMongoStudent(String uid, String email, String nom, String prenom,
                                    int matricule, String filiere, String classe,
                                    int niveau, String codeClasse) {

        Map<String, Object> studentData = new HashMap<>();
        studentData.put("_id", uid);        // UID Firebase = _id Mongo
        studentData.put("uid", uid);
        studentData.put("email", email);
        studentData.put("nom", nom);
        studentData.put("prenom", prenom);
        studentData.put("matricule", matricule);
        studentData.put("filiere", filiere);
        studentData.put("classe", classe);
        studentData.put("codeClasse", codeClasse);
        studentData.put("niveau", niveau);
        studentData.put("role", "student");

        apiService.createUser(studentData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateStudentActivity.this,
                            "Étudiant créé avec succès",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateStudentActivity.this,
                            "Erreur API (Mongo)",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateStudentActivity.this,
                        "Erreur réseau: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
