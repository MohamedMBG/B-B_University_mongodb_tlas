package com.example.bbuniversity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.ClassAdapter;
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

public class TeacherClassesActivity extends AppCompatActivity
        implements ClassAdapter.OnClassClickListener {

    private ClassAdapter adapter;
    private final List<ClassInfo> classes = new ArrayList<>();

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_classes);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

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

        RecyclerView recycler = findViewById(R.id.recyclerClasses);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(classes, this);
        recycler.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadClassesFromMongo(user.getUid());
        } else {
            Toast.makeText(this, "Prof non connecté", Toast.LENGTH_SHORT).show();
        }
    }

    // --------- CHARGER LES CLASSES DU PROF DEPUIS MONGO ----------
    private void loadClassesFromMongo(String professorUid) {
        apiService.getProfessorById(professorUid).enqueue(new Callback<Professeur>() {
            @Override
            public void onResponse(Call<Professeur> call, Response<Professeur> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TeacherClassesActivity.this,
                            "Erreur chargement classes prof : code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Professeur p = response.body();
                classes.clear();

                if (p.getEnseignement() != null) {
                    // enseignement: { "MATH_101": ["3IIRB", "3IIRC"], ... }
                    for (Map.Entry<String, List<String>> e : p.getEnseignement().entrySet()) {
                        String subjectId = e.getKey();       // ex: "MATH_101"
                        List<String> classList = e.getValue(); // ex: ["3IIRB","3IIRC"]

                        if (classList != null) {
                            for (String c : classList) {
                                classes.add(new ClassInfo(c, subjectId));
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<Professeur> call, Throwable t) {
                Toast.makeText(TeacherClassesActivity.this,
                        "Erreur réseau classes prof : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // --------- CLIC SUR UNE CLASSE ----------
    @Override
    public void onClassClick(ClassInfo info) {
        Intent i = new Intent(this, ClassStudentsActivity.class);
        i.putExtra("class", info.getClassName());  // ex: "3IIRB"
        i.putExtra("subject", info.getSubject());  // ex: "MATH_101"
        startActivity(i);
    }
}
