package com.example.bbuniversity.etudiant;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.TimetableEntry;
import com.example.bbuniversity.adapters.TimetableAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentTimetableActivity extends AppCompatActivity {

    private TextView tvClassTitle, tvEmpty;
    private RecyclerView rvTimetable;
    private ProgressBar progressBar;

    private TimetableAdapter adapter;
    private final List<TimetableEntry> entries = new ArrayList<>();

    private ApiService apiService;
    private String className;   // ex: "3IIR A"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_timetable);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        className = getIntent().getStringExtra("class");
        if (className == null || className.isEmpty()) {
            Toast.makeText(this, "Classe non fournie", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Bind UI
        tvClassTitle = findViewById(R.id.tvClassTitle);
        tvEmpty      = findViewById(R.id.tvEmpty);
        rvTimetable  = findViewById(R.id.rvTimetable);
        progressBar  = findViewById(R.id.progressBar);

        tvClassTitle.setText("Emploi du temps - " + className);

        rvTimetable.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TimetableAdapter(entries);
        rvTimetable.setAdapter(adapter);

        loadTimetableFromMongo();
    }

    private void loadTimetableFromMongo() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        apiService.getTimetable(className).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Erreur chargement emploi du temps (code="
                            + response.code() + ")");
                    return;
                }

                Map<String, Object> body = response.body();
                Object entriesObj = body.get("entries");

                entries.clear();

                if (entriesObj instanceof List) {
                    List<?> rawList = (List<?>) entriesObj;
                    for (Object obj : rawList) {
                        if (obj instanceof Map) {
                            Map<?, ?> m = (Map<?, ?>) obj;
                            String day     = m.get("day")     != null ? m.get("day").toString()     : "";
                            String subject = m.get("subject") != null ? m.get("subject").toString() : "";
                            String start   = m.get("start")   != null ? m.get("start").toString()   : "";
                            String end     = m.get("end")     != null ? m.get("end").toString()     : "";

                            entries.add(new TimetableEntry(day, subject, start, end));
                        }
                    }
                }

                if (entries.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Aucune séance définie pour cette classe.");
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Erreur réseau : " + t.getMessage());
            }
        });
    }
}
