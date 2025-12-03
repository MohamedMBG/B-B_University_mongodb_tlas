package com.example.bbuniversity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.TimetableViewActivity;
import com.example.bbuniversity.adapters.ClassAdapter;
import com.example.bbuniversity.admin_panel.AdminActivity;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.ClassInfo;
import com.example.bbuniversity.models.Professeur;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherDashboard extends AppCompatActivity implements ClassAdapter.OnClassClickListener {

    private TextView tvWelcome;
    private RecyclerView rvNextClasses;
    private ClassAdapter adapter;
    private final List<ClassInfo> classInfos = new ArrayList<>();

    private ApiService apiService;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_dashboard);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        tvWelcome     = findViewById(R.id.tvWelcome);
        rvNextClasses = findViewById(R.id.rvNextClasses);

        rvNextClasses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(classInfos, this);
        rvNextClasses.setAdapter(adapter);

        auth       = FirebaseAuth.getInstance();
        apiService = ApiClient.getClient().create(ApiService.class);

        // Boutons
        findViewById(R.id.btnComplaints).setOnClickListener(v ->
                startActivity(new Intent(this, TeacherComplaintsActivity.class)));



        findViewById(R.id.btnAllClasses).setOnClickListener(v ->
                startActivity(new Intent(this, TeacherClassesActivity.class)));

        findViewById(R.id.btnTeacherTimetable).setOnClickListener(v -> {
            FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            if (current == null) {
                Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, TeacherTimetableActivity.class);
            i.putExtra("professorId", current.getUid()); // ✅ même clé que dans TeacherTimetableActivity
            startActivity(i);
        });


        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, AdminActivity.class));
            finish();
        });

        // Charger les infos du prof depuis Mongo
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadTeacherInfoFromMongo(user.getUid());
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }
    }

    /** Récupère le professeur depuis MongoDB via l'API */
    private void loadTeacherInfoFromMongo(String uid) {
        apiService.getProfessorById(uid).enqueue(new Callback<Professeur>() {
            @Override
            public void onResponse(Call<Professeur> call, Response<Professeur> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TeacherDashboard.this,
                            "Erreur chargement professeur (code=" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                populateInfo(response.body());
            }

            @Override
            public void onFailure(Call<Professeur> call, Throwable t) {
                Toast.makeText(TeacherDashboard.this,
                        "Erreur réseau (professeur): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Remplit le header + la liste des classes à partir du modèle Professeur (Mongo) */
    private void populateInfo(Professeur prof) {
        if (prof == null) return;

        String prenom = prof.getPrenom() != null ? prof.getPrenom() : "";
        String nom    = prof.getNom() != null ? prof.getNom() : "";
        String name   = (prenom + " " + nom).trim();

        tvWelcome.setText(getString(R.string.welcome_teacher, name));

        // enseignement = { "POO" : ["2IIR-A", "2IIR-B"], "Reseaux" : ["3IIR-A"] }
        Map<String, List<String>> ens = prof.getEnseignement();
        classInfos.clear();

        if (ens != null) {
            for (Map.Entry<String, List<String>> e : ens.entrySet()) {
                String matiere = e.getKey();
                List<String> classes = e.getValue();
                if (classes == null) continue;
                for (String c : classes) {
                    classInfos.add(new ClassInfo(c, matiere));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClassClick(ClassInfo info) {
        Intent i = new Intent(this, ClassStudentsActivity.class);
        i.putExtra("class", info.getClassName());
        i.putExtra("subject", info.getSubject());
        startActivity(i);
    }
}
