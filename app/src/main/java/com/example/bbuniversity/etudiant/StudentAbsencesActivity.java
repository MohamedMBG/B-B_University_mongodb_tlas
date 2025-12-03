package com.example.bbuniversity.etudiant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.AbsenceAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Abscence;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAbsencesActivity extends AppCompatActivity {

    // Toutes les absences venant de Mongo
    private final List<Abscence> allAbsences = new ArrayList<>();
    // Absences filtrées affichées dans le RecyclerView
    private final List<Abscence> filteredAbsences = new ArrayList<>();
    private AbsenceAdapter adapter;

    // API Mongo
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_absences);

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        apiService = ApiClient.getClient().create(ApiService.class);

        TextInputEditText etSubject = findViewById(R.id.etFilterSubject);
        TextInputEditText etDate    = findViewById(R.id.etFilterDate);
        RecyclerView recycler       = findViewById(R.id.recyclerAbsences);

        adapter = new AbsenceAdapter(filteredAbsences);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // 👤 Récupérer l'utilisateur courant (UID Firebase)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadAbsencesFromMongo(user.getUid());
        } else {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
        }

        // 🔍 Filtres sujet + date
        TextWatcher watcher = new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String subj = etSubject.getText() == null ? "" : etSubject.getText().toString();
                String date = etDate.getText() == null ? "" : etDate.getText().toString();
                filterAbsences(subj, date);
            }
        };
        etSubject.addTextChangedListener(watcher);
        etDate.addTextChangedListener(watcher);
    }

    /** Charge les absences depuis MongoDB via l'API */
    private void loadAbsencesFromMongo(String studentId) {
        apiService.getAbsences(studentId).enqueue(new Callback<List<Abscence>>() {
            @Override
            public void onResponse(Call<List<Abscence>> call, Response<List<Abscence>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(StudentAbsencesActivity.this,
                            "Erreur de chargement des absences : code = " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                allAbsences.clear();
                allAbsences.addAll(response.body());

                filteredAbsences.clear();
                filteredAbsences.addAll(allAbsences);
                adapter.updateData(filteredAbsences);
            }

            @Override
            public void onFailure(Call<List<Abscence>> call, Throwable t) {
                Toast.makeText(StudentAbsencesActivity.this,
                        "Erreur réseau (absences) : " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Filtre les absences par matière et date (dd/MM/yyyy tapé par l’étudiant) */
    private void filterAbsences(String subject, String dateStr) {
        filteredAbsences.clear();

        String qSubject = subject == null ? "" : subject.toLowerCase(Locale.ROOT);
        String qDate    = dateStr == null ? "" : dateStr.trim();

        for (Abscence a : allAbsences) {
            boolean matchSubj =
                    qSubject.isEmpty()
                            || (a.getMatiere() != null &&
                            a.getMatiere().toLowerCase(Locale.ROOT).contains(qSubject));

            boolean matchDate = qDate.isEmpty() || dateMatches(a.getDate(), qDate);

            if (matchSubj && matchDate) {
                filteredAbsences.add(a);
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Compare la date renvoyée par l’API
     * (format "yyyy-MM-dd" ou parfois ISO complet) au texte saisi (dd/MM/yyyy ou fragment).
     */
    private boolean dateMatches(String rawDate, String query) {
        if (rawDate == null || rawDate.isEmpty()) return false;
        if (query == null || query.isEmpty()) return true;

        // On tente plusieurs formats possibles venant de l’API
        Date parsed = null;
        String[] formats = {
                "yyyy-MM-dd",                  // celui qu’on renvoie dans l’API
                "yyyy-MM-dd'T'HH:mm:ss.SSSX"   // au cas où ce soit un ISO complet
        };

        for (String f : formats) {
            try {
                SimpleDateFormat iso = new SimpleDateFormat(f, Locale.getDefault());
                parsed = iso.parse(rawDate);
                if (parsed != null) break;
            } catch (ParseException ignored) {}
        }

        if (parsed == null) {
            // fallback : compare brut
            return rawDate.contains(query);
        }

        SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        String formatted = out.format(parsed);

        // L’utilisateur peut taper "01/07" ou "01/07/2025"
        return formatted.contains(query);
    }

    /** TextWatcher simplifié */
    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
