package com.example.bbuniversity.admin_panel;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.TeacherAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Professeur;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageProfessorsActivity extends AppCompatActivity
        implements TeacherAdapter.OnTeacherClickListener {

    private RecyclerView teachersRv;
    private TeacherAdapter adapter;
    private final List<Professeur> teacherList = new ArrayList<>();
    private final List<Professeur> filteredTeachers = new ArrayList<>();
    private TextInputEditText searchInput;
    private ImageView goBack;

    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_professors);
        EdgeToEdge.enable(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Init API client
        apiService = ApiClient.getClient().create(ApiService.class);

        // Views
        goBack = findViewById(R.id.btn_back);
        searchInput = findViewById(R.id.searchInput);
        teachersRv = findViewById(R.id.recyclerViewTeachers);

        goBack.setOnClickListener(v -> finish());

        // Recycler
        adapter = new TeacherAdapter(filteredTeachers, this);
        teachersRv.setLayoutManager(new LinearLayoutManager(this));
        teachersRv.setAdapter(adapter);

        fetchProfessors();
        setupSearch();
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
        filteredTeachers.clear();

        if (query == null || query.isEmpty()) {
            filteredTeachers.addAll(teacherList);
        } else {
            String lower = query.toLowerCase();
            for (Professeur p : teacherList) {
                if (p != null && matchesQuery(p, lower)) {
                    filteredTeachers.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean matchesQuery(Professeur p, String q) {
        return (p.getNom() != null && p.getNom().toLowerCase().contains(q)) ||
                (p.getPrenom() != null && p.getPrenom().toLowerCase().contains(q)) ||
                (p.getEmail() != null && p.getEmail().toLowerCase().contains(q)) ||
                (p.getDepartement() != null && p.getDepartement().toLowerCase().contains(q));
    }

    @Override
    public void onTeacherClick(Professeur teacher) {
        if (teacher != null && teacher.getUid() != null) {
            Intent intent = new Intent(this, CreateProfessorActivity.class);
            intent.putExtra("teacherId", teacher.getUid());
            startActivity(intent);
        }
    }

    private void fetchProfessors() {
        // Call GET /api/users?role=professor
        apiService.getProfessors("professor").enqueue(new Callback<List<Professeur>>() {
            @Override
            public void onResponse(Call<List<Professeur>> call,
                                   Response<List<Professeur>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    // you can log/Toast if you want
                    return;
                }

                teacherList.clear();
                teacherList.addAll(response.body());

                filteredTeachers.clear();
                filteredTeachers.addAll(teacherList);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Professeur>> call, Throwable t) {
                // log / toast if needed
            }
        });
    }
}
