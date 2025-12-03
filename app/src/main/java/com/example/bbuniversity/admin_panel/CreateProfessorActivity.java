package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Matiere;
import com.example.bbuniversity.models.Professeur;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateProfessorActivity extends AppCompatActivity {

    private TextInputEditText etNom, etPrenom, etEmail, etPassword, etAdresse, etDepartement;
    private AutoCompleteTextView autoMatiere;
    private Button btnCreate, btnCancel, btnAssocierClasses;
    private String teacherId;

    private final List<String> allClasses = new ArrayList<>();
    private final List<String> selectedClasses = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_professor);
        EdgeToEdge.enable(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Views
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAdresse = findViewById(R.id.etAdresse);
        etDepartement = findViewById(R.id.etDepartement);
        autoMatiere = findViewById(R.id.autoMatiere);
        btnCreate = findViewById(R.id.btnCreateStudent);
        btnCancel = findViewById(R.id.btnCancel);
        btnAssocierClasses = findViewById(R.id.btnAssocierClasses);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // encore utilisé pour loadTeacherData
        apiService = ApiClient.getClient().create(ApiService.class);

        teacherId = getIntent().getStringExtra("teacherId");

        // 🔹 Charger matières depuis Mongo
        loadMatieresFromMongo();

        // 🔹 Charger classes depuis Mongo
        loadClassesFromMongo();

        autoMatiere.setOnClickListener(v -> autoMatiere.showDropDown());

        btnCancel.setOnClickListener(v -> finish());
        btnAssocierClasses.setOnClickListener(v -> showClassDialog());

        if (teacherId != null) {
            // mode édition
            etEmail.setEnabled(false);
            etPassword.setVisibility(View.GONE);
            btnCreate.setText("Mettre à jour");
            loadTeacherData();
            btnCreate.setOnClickListener(v -> updateProfessor());
        } else {
            // création
            btnCreate.setOnClickListener(v -> createProfessor());
        }
    }

    /** Charge les matières depuis MongoDB via /api/matieres */
    private void loadMatieresFromMongo() {
        apiService.getMatieres().enqueue(new Callback<List<Matiere>>() {
            @Override
            public void onResponse(Call<List<Matiere>> call, Response<List<Matiere>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CreateProfessorActivity.this,
                            "Erreur chargement matières (Mongo): code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                List<String> matieres = new ArrayList<>();
                for (Matiere m : response.body()) {
                    if (m.getNom() != null) {
                        matieres.add(m.getNom());
                    }
                }

                autoMatiere.setAdapter(new ArrayAdapter<>(
                        CreateProfessorActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        matieres
                ));
            }

            @Override
            public void onFailure(Call<List<Matiere>> call, Throwable t) {
                Toast.makeText(CreateProfessorActivity.this,
                        "Erreur réseau (matieres Mongo): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Charge les classes depuis MongoDB via /api/classes */
    private void loadClassesFromMongo() {
        apiService.getClasses().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CreateProfessorActivity.this,
                            "Erreur chargement classes (Mongo): code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                allClasses.clear();
                for (Map<String, Object> doc : response.body()) {
                    // On suppose que chaque doc a un champ "name"
                    Object nameObj = doc.get("name");
                    if (nameObj != null) {
                        allClasses.add(nameObj.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(CreateProfessorActivity.this,
                        "Erreur réseau (classes Mongo): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    // Dialog choix classes (lecture Firestore)
    // Dialog choix classes (données déjà chargées depuis Mongo)
    private void showClassDialog() {
        if (allClasses.isEmpty()) {
            Toast.makeText(this, "Aucune classe trouvée (Mongo)", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean[] checked = new boolean[allClasses.size()];
        String[] classesArray = allClasses.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Sélectionner les classes")
                .setMultiChoiceItems(classesArray, checked, (dialog, i, isChecked) -> {
                    String c = classesArray[i];
                    if (isChecked) selectedClasses.add(c);
                    else selectedClasses.remove(c);
                })
                .setPositiveButton("OK", null)
                .setNegativeButton("Annuler", null)
                .show();
    }


    // 🔹 Création professeur : FirebaseAuth + MongoDB (API)
    private void createProfessor() {
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();
        String dep = etDepartement.getText().toString().trim();
        String matiere = autoMatiere.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email et mot de passe requis", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    Map<String, List<String>> enseignement = new HashMap<>();
                    if (!matiere.isEmpty()) {
                        enseignement.put(matiere, new ArrayList<>(selectedClasses));
                    }

                    Map<String, Object> profData = new HashMap<>();
                    profData.put("_id", uid);
                    profData.put("uid", uid);
                    profData.put("nom", nom);
                    profData.put("prenom", prenom);
                    profData.put("email", email);
                    profData.put("adresse", adresse);
                    profData.put("departement", dep);
                    profData.put("role", "professor");
                    profData.put("enseignement", enseignement);

                    apiService.createUser(profData).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(CreateProfessorActivity.this,
                                        "Professeur créé avec succès", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                String err = "code=" + response.code();
                                try {
                                    if (response.errorBody() != null) {
                                        err += " body=" + response.errorBody().string();
                                    }
                                } catch (Exception ignored) {}
                                Toast.makeText(CreateProfessorActivity.this,
                                        "Erreur API (Mongo): " + err,
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(CreateProfessorActivity.this,
                                    "Erreur réseau: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur Auth: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadTeacherData() {
        // Pour l’instant on laisse Firestore pour le chargement
        FirebaseFirestore.getInstance().collection("users").document(teacherId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Professeur p = doc.toObject(Professeur.class);
                        if (p != null) {
                            etNom.setText(p.getNom());
                            etPrenom.setText(p.getPrenom());
                            etEmail.setText(p.getEmail());
                            etAdresse.setText(p.getAdresse());
                            etDepartement.setText(p.getDepartement());
                        }
                    }
                });
    }

    private void updateProfessor() {
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();
        String dep = etDepartement.getText().toString().trim();
        String matiere = autoMatiere.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("nom", nom);
        updates.put("prenom", prenom);
        updates.put("adresse", adresse);
        updates.put("departement", dep);

        if (!matiere.isEmpty()) {
            Map<String, List<String>> enseignement = new HashMap<>();
            enseignement.put(matiere, new ArrayList<>(selectedClasses));
            updates.put("enseignement", enseignement);
        }

        apiService.updateUser(teacherId, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateProfessorActivity.this,
                            "Professeur mis à jour", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String err = "code=" + response.code();
                    try {
                        if (response.errorBody() != null) {
                            err += " body=" + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(CreateProfessorActivity.this,
                            "Erreur API (Mongo): " + err,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateProfessorActivity.this,
                        "Erreur réseau: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
