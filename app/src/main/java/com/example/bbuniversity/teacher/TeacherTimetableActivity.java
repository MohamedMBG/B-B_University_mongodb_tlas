package com.example.bbuniversity.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.TimetableAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Professeur;
import com.example.bbuniversity.models.TimetableEntry;
import com.example.bbuniversity.models.TimetableResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherTimetableActivity extends AppCompatActivity {

    private Spinner spinnerClass;
    private RecyclerView rvTimetable;

    private ApiService apiService;

    private String professorId;

    // Liste des paires (classe, matière) que ce prof enseigne
    private final List<TeacherSlot> teacherSlots = new ArrayList<>();

    // Séances filtrées pour la classe + matière sélectionnées
    private final List<TimetableEntry> timetableEntries = new ArrayList<>();
    private TimetableAdapter timetableAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_timetable);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        spinnerClass = findViewById(R.id.spinnerClass);
        rvTimetable  = findViewById(R.id.recyclerTimetable);

        rvTimetable.setLayoutManager(new LinearLayoutManager(this));
        timetableAdapter = new TimetableAdapter(timetableEntries);
        rvTimetable.setAdapter(timetableAdapter);

        // récupéré depuis TeacherDashboard
        professorId = getIntent().getStringExtra("professorId");

        if (professorId == null || professorId.isEmpty()) {
            Toast.makeText(this, "Professeur inconnu", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupSpinnerListener();
        loadProfessorClasses();
    }

    private void setupSpinnerListener() {
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= teacherSlots.size()) return;

                TeacherSlot slot = teacherSlots.get(position);
                String selectedClass   = slot.className;
                String selectedSubject = slot.subject;

                loadTimetableForClassAndSubject(selectedClass, selectedSubject);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // rien
            }
        });
    }

    /** 1) Récupère le prof depuis Mongo et construit la liste (classe, matière) */
    private void loadProfessorClasses() {
        apiService.getProfessorById(professorId).enqueue(new Callback<Professeur>() {
            @Override
            public void onResponse(Call<Professeur> call, Response<Professeur> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TeacherTimetableActivity.this,
                            "Erreur chargement prof (Mongo) code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Professeur prof = response.body();
                Map<String, List<String>> enseignement = prof.getEnseignement();

                teacherSlots.clear();

                if (enseignement != null) {
                    // enseignement : clé = matière, valeur = liste de classes
                    for (Map.Entry<String, List<String>> e : enseignement.entrySet()) {
                        String subject = e.getKey();          // ex: "JAVA"
                        List<String> classes = e.getValue();  // ex: ["4IIR2", "4IIR3"]

                        if (classes != null) {
                            for (String c : classes) {
                                teacherSlots.add(new TeacherSlot(c, subject));
                            }
                        }
                    }
                }

                if (teacherSlots.isEmpty()) {
                    Toast.makeText(TeacherTimetableActivity.this,
                            "Aucune classe associée à ce professeur.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // L’adapter utilise toString() de TeacherSlot → "4IIR2 - JAVA"
                ArrayAdapter<TeacherSlot> adapter = new ArrayAdapter<>(
                        TeacherTimetableActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        teacherSlots
                );
                spinnerClass.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<Professeur> call, Throwable t) {
                Toast.makeText(TeacherTimetableActivity.this,
                        "Erreur réseau prof : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** 2) Charge l’EDT de la classe et garde uniquement la matière de ce prof */
    private void loadTimetableForClassAndSubject(String className, String subject) {
        apiService.getTimetablee(className).enqueue(new Callback<TimetableResponse>() {
            @Override
            public void onResponse(Call<TimetableResponse> call,
                                   Response<TimetableResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TeacherTimetableActivity.this,
                            "Erreur chargement EDT code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                timetableEntries.clear();

                if (response.body().getEntries() != null) {
                    for (TimetableEntry e : response.body().getEntries()) {
                        // ne garder que les séances de la matière du prof
                        if (subject.equalsIgnoreCase(e.getSubject())) {
                            timetableEntries.add(e);
                        }
                    }
                }

                timetableAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<TimetableResponse> call, Throwable t) {
                Toast.makeText(TeacherTimetableActivity.this,
                        "Erreur réseau EDT : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Petite classe pour représenter (classe, matière) dans le Spinner */
    private static class TeacherSlot {
        String className;
        String subject;

        TeacherSlot(String className, String subject) {
            this.className = className;
            this.subject = subject;
        }

        @Override
        public String toString() {
            // Ce qui sera affiché dans le Spinner
            return className + " - " + subject;
        }
    }
}
