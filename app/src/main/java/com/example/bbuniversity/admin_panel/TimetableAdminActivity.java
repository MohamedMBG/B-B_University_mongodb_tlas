package com.example.bbuniversity.admin_panel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Classe;
import com.example.bbuniversity.models.Matiere;
import com.example.bbuniversity.models.TimetableEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Écran d'administration permettant de créer l'emploi du temps d'une classe
 * → stockage dans MongoDB via /api/classes/{classId}/timetables
 */
public class TimetableAdminActivity extends AppCompatActivity {

    // Sélecteur de classe
    private Spinner spinnerClass;
    // Sélecteur de matière
    private Spinner spinnerSubject;
    // Sélecteur de jour
    private Spinner spinnerDay;
    // Champs pour les heures de début et de fin
    private EditText etStart, etEnd;
    // Liste affichant les séances ajoutées
    private ListView listView;
    // Liste interne des entrées
    private final List<TimetableEntry> entries = new ArrayList<>();
    // Adaptateur pour la ListView
    private ArrayAdapter<String> listAdapter;

    // Listes pour les spinners
    private final List<String> classNames   = new ArrayList<>();
    private final List<String> subjectNames = new ArrayList<>();

    private ApiService apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timetable_admin);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        // Liaison des vues
        spinnerClass   = findViewById(R.id.spinnerClass);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerDay     = findViewById(R.id.spinnerDay);
        etStart        = findViewById(R.id.etStart);
        etEnd          = findViewById(R.id.etEnd);
        listView       = findViewById(R.id.listEntries);
        Button btnAdd  = findViewById(R.id.btnAddEntry);
        Button btnSave = findViewById(R.id.btnSaveTimetable);

        // Adapter pour la liste des séances
        listAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );
        listView.setAdapter(listAdapter);

        // Chargement des classes et matières depuis Mongo
        loadClassesFromMongo();
        loadSubjectsFromMongo();

        // Ajout d'une entrée au clic
        btnAdd.setOnClickListener(v -> addEntry());
        // Sauvegarde dans Mongo
        btnSave.setOnClickListener(v -> saveTimetable());
    }

    /** 🔹 Charge la liste des classes depuis Mongo (/api/classes) */
    private void loadClassesFromMongo() {
        apiService.getClassesMongo().enqueue(new Callback<List<Classe>>() {
            @Override
            public void onResponse(Call<List<Classe>> call, Response<List<Classe>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TimetableAdminActivity.this,
                            "Erreur chargement classes (Mongo) code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                classNames.clear();
                for (Classe c : response.body()) {
                    if (c.getName() != null) {
                        classNames.add(c.getName());   // ex: "3IIR A"
                    }
                }

                if (classNames.isEmpty()) {
                    Toast.makeText(TimetableAdminActivity.this,
                            "Aucune classe trouvée dans Mongo",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        TimetableAdminActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        classNames
                );
                spinnerClass.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Classe>> call, Throwable t) {
                Toast.makeText(TimetableAdminActivity.this,
                        "Erreur réseau (classes Mongo): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** 🔹 Charge la liste des matières depuis Mongo (/api/matieres) */
    private void loadSubjectsFromMongo() {
        apiService.getMatieres().enqueue(new Callback<List<Matiere>>() {
            @Override
            public void onResponse(Call<List<Matiere>> call, Response<List<Matiere>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TimetableAdminActivity.this,
                            "Erreur chargement matières (Mongo) code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                subjectNames.clear();
                for (Matiere m : response.body()) {
                    if (m.getNom() != null) {
                        subjectNames.add(m.getNom());
                    }
                }

                if (subjectNames.isEmpty()) {
                    Toast.makeText(TimetableAdminActivity.this,
                            "Aucune matière trouvée dans Mongo",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        TimetableAdminActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        subjectNames
                );
                spinnerSubject.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Matiere>> call, Throwable t) {
                Toast.makeText(TimetableAdminActivity.this,
                        "Erreur réseau (matieres Mongo): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** 🔹 Ajoute une séance à la liste locale et à l'affichage */
    private void addEntry() {
        if (spinnerClass.getSelectedItem() == null) {
            Toast.makeText(this, "Sélectionnez une classe", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerSubject.getSelectedItem() == null) {
            Toast.makeText(this, "Sélectionnez une matière", Toast.LENGTH_SHORT).show();
            return;
        }

        String day     = spinnerDay.getSelectedItem().toString();
        String subject = spinnerSubject.getSelectedItem().toString();
        String start   = etStart.getText().toString().trim();
        String end     = etEnd.getText().toString().trim();

        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Heures manquantes", Toast.LENGTH_SHORT).show();
            return;
        }

        TimetableEntry entry = new TimetableEntry(day, subject, start, end);
        entries.add(entry);

        listAdapter.add(day + " - " + subject + " " + start + "-" + end);
        etStart.setText("");
        etEnd.setText("");
    }

    /** 🔹 Enregistre l'emploi du temps de la classe sélectionnée dans Mongo */
    private void saveTimetable() {
        if (spinnerClass.getSelectedItem() == null) {
            Toast.makeText(this, "Sélectionnez une classe", Toast.LENGTH_SHORT).show();
            return;
        }

        String className = spinnerClass.getSelectedItem().toString();

        if (entries.isEmpty()) {
            Toast.makeText(this, "Ajoutez au moins une séance", Toast.LENGTH_SHORT).show();
            return;
        }

        // construction payload
        List<Map<String, String>> data = new ArrayList<>();
        for (TimetableEntry e : entries) {
            Map<String, String> m = new HashMap<>();
            m.put("day", e.getDay());
            m.put("subject", e.getSubject());
            m.put("start", e.getStart());
            m.put("end", e.getEnd());
            data.add(m);
        }

        Map<String, Object> doc = new HashMap<>();
        doc.put("class", className);
        doc.put("entries", data);

        // POST /api/classes/{classId}/timetables
        apiService.saveTimetable(className, doc).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TimetableAdminActivity.this,
                            "Emploi du temps enregistré dans Mongo",
                            Toast.LENGTH_SHORT).show();
                    entries.clear();
                    listAdapter.clear();
                } else {
                    Toast.makeText(TimetableAdminActivity.this,
                            "Erreur API (timetables) code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TimetableAdminActivity.this,
                        "Erreur réseau (timetables): " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
