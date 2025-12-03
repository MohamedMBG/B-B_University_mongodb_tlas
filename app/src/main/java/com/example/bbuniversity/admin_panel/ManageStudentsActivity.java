package com.example.bbuniversity.admin_panel;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.StudentAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Etudiant;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageStudentsActivity extends AppCompatActivity
        implements StudentAdapter.OnStudentClickListener {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<Etudiant> studentList = new ArrayList<>();
    private List<Etudiant> filteredList = new ArrayList<>();
    private TextInputEditText searchInput;
    private ImageView fabBack;

    // --- API Mongo ---
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_students);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        initializeViews();
        setupRecyclerView();
        setupSearch();
        loadStudentsFromMongo();
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        recyclerView = findViewById(R.id.recyclerViewStudents);
        fabBack = findViewById(R.id.btn_back);
        fabBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(filteredList, this);
        recyclerView.setAdapter(adapter);
    }

    // ----------------------------
    //  CHARGEMENT DEPUIS MONGO
    // ----------------------------
    private void loadStudentsFromMongo() {
        apiService.getStudents().enqueue(new Callback<List<Etudiant>>() {
            @Override
            public void onResponse(Call<List<Etudiant>> call,
                                   Response<List<Etudiant>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ManageStudentsActivity.this,
                            "Erreur chargement étudiants : code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                studentList.clear();
                studentList.addAll(response.body());
                updateFilteredList("");
            }

            @Override
            public void onFailure(Call<List<Etudiant>> call, Throwable t) {
                Toast.makeText(ManageStudentsActivity.this,
                        "Erreur réseau étudiants : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFilteredList(s != null ? s.toString() : "");
            }
        });
    }

    private void updateFilteredList(String query) {
        filteredList.clear();

        if (query == null || query.isEmpty()) {
            filteredList.addAll(studentList);
        } else {
            String lower = query.toLowerCase();
            for (Etudiant student : studentList) {
                if (student != null && matchesQuery(student, lower)) {
                    filteredList.add(student);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean matchesQuery(Etudiant student, String q) {
        // Nom + prénom
        if (student.getNom() != null &&
                student.getNom().toLowerCase().contains(q)) return true;

        if (student.getPrenom() != null &&
                student.getPrenom().toLowerCase().contains(q)) return true;

        // Email
        if (student.getEmail() != null &&
                student.getEmail().toLowerCase().contains(q)) return true;

        // Matricule (int → String)
        int matricule = student.getMatricule();
        if (matricule > 0 &&
                String.valueOf(matricule).contains(q)) return true;

        // Code classe ou classe
        String codeClasse = student.getCodeClasse();
        if (codeClasse != null &&
                codeClasse.toLowerCase().contains(q)) return true;

        String classe = student.getClasse();
        if (classe != null &&
                classe.toLowerCase().contains(q)) return true;

        // Filière
        if (student.getFiliere() != null &&
                student.getFiliere().toLowerCase().contains(q)) return true;

        return false;
    }

    // ----------------------------
    //  CLICS SUR UN ÉTUDIANT
    // ----------------------------
    @Override
    public void onStudentClick(Etudiant student) {
        try {
            if (student != null && student.getUid() != null) {
                Intent intent = new Intent(this, EditStudentDetailsActivity.class);
                intent.putExtra("studentId", student.getUid());  // toujours uid comme clé
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    "Error opening student details: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStudentLongClick(Etudiant student, View view) {
        if (student != null && student.getPrenom() != null) {
            Toast.makeText(this,
                    "Long pressed: " + student.getPrenom(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
