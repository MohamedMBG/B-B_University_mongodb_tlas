package com.example.bbuniversity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassStudentsActivity extends AppCompatActivity
        implements StudentAdapter.OnStudentClickListener {

    private final List<Etudiant> students = new ArrayList<>();
    private StudentAdapter adapter;
    private String className;
    private String subject;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_students);
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        className = getIntent().getStringExtra("class");
        subject   = getIntent().getStringExtra("subject");

        TextView title = findViewById(R.id.tvClassTitle);
        title.setText(className + " - " + subject);

        RecyclerView recycler = findViewById(R.id.recyclerStudents);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(students, this);
        recycler.setAdapter(adapter);

        // 1. Find the back button by its ID
        ImageButton btnBack = findViewById(R.id.btnBack);

        // 2. Set the click listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This triggers the default Android back navigation behavior
                // It is equivalent to pressing the physical back button on the device.
                getOnBackPressedDispatcher().onBackPressed();

                // Alternatively, if you just want to close this specific activity:
                // finish();
            }
        });

        loadStudentsFromMongo();
    }

    // --------- CHARGER LES ÉTUDIANTS DEPUIS MONGO ----------
    private void loadStudentsFromMongo() {
        apiService.getStudentsByClass(className).enqueue(new Callback<List<Etudiant>>() {
            @Override
            public void onResponse(Call<List<Etudiant>> call, Response<List<Etudiant>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ClassStudentsActivity.this,
                            "Erreur chargement étudiants (classe) code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                students.clear();
                students.addAll(response.body());
                adapter.updateList(students);
            }

            @Override
            public void onFailure(Call<List<Etudiant>> call, Throwable t) {
                Toast.makeText(ClassStudentsActivity.this,
                        "Erreur réseau étudiants : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // --------- CLIC SUR UN ÉTUDIANT ----------

    @Override
    public void onStudentClick(Etudiant student) {
        Intent intent = new Intent(this, AddNoteActivity.class);

        // ✅ maintenant : on envoie le UID (qui correspond à _id / uid dans Mongo)
        // adapte selon ton modèle Etudiant: getUid() ou getId()
        String studentUid = student.getUid();   // ou student.getId() si c’est comme ça que tu l’as nommé
        intent.putExtra("studentId", studentUid);

        // matière (nom ou id, selon ta logique serveur)
        intent.putExtra("matiereId", subject);

        // id interne du prof (Firebase UID)
        String profUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (profUid != null) {
            intent.putExtra("professorId", profUid);
        }

        startActivity(intent);
    }

    @Override
    public void onStudentLongClick(Etudiant student, View view) {
        // Rien pour l’instant
    }
}
