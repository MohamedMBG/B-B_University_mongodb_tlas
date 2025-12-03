package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.ManageAbsenceAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Abscence;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageAbsencesActivity extends AppCompatActivity implements ManageAbsenceAdapter.OnJustifyListener {

    private String studentId;
    private final List<Abscence> absences = new ArrayList<>();
    private ManageAbsenceAdapter adapter;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_absences);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Student ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);

        RecyclerView recycler = findViewById(R.id.recyclerAbsences);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageAbsenceAdapter(absences, this);
        recycler.setAdapter(adapter);

        loadAbsencesFromMongo();
    }

    private void loadAbsencesFromMongo() {
        apiService.getStudentAbsences(studentId).enqueue(new Callback<List<Abscence>>() {
            @Override
            public void onResponse(Call<List<Abscence>> call,
                                   Response<List<Abscence>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ManageAbsencesActivity.this,
                            "Erreur chargement absences (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                absences.clear();
                absences.addAll(response.body());
                adapter.updateData(absences);
            }

            @Override
            public void onFailure(Call<List<Abscence>> call, Throwable t) {
                Toast.makeText(ManageAbsencesActivity.this,
                        "Erreur réseau : " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onJustify(Abscence absence) {
        if (absence == null || absence.getId() == null) {
            Toast.makeText(this, "Absence invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.justifyAbsence(studentId, absence.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(ManageAbsencesActivity.this,
                                    "Erreur justification (code " + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(ManageAbsencesActivity.this,
                                "Absence justifiée et note mise à jour",
                                Toast.LENGTH_SHORT).show();

                        // recharger la liste depuis Mongo
                        loadAbsencesFromMongo();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ManageAbsencesActivity.this,
                                "Erreur réseau : " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}