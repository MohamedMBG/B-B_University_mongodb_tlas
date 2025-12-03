package com.example.bbuniversity.admin_panel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.MainActivity;
import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.NoteAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.etudiant.StudentActivity;
import com.example.bbuniversity.models.DashboardResponse;
import com.example.bbuniversity.models.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboard extends AppCompatActivity {

    private NoteAdapter noteAdapter;
    private TextView textTotalStudents, textTotalTeachers, textTotalAbsences;
    private CardView manageTeachers, manageStudents;
    private ApiService apiService;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent i = new Intent(AdminDashboard.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });


        setupUI();
        setupButtons();
    }
    private void setupUI() {
        RecyclerView recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(new ArrayList<>());
        recyclerViewNotes.setAdapter(noteAdapter);

        textTotalStudents = findViewById(R.id.textTotalStudents);
        textTotalTeachers = findViewById(R.id.textTotalTeachers);

        manageTeachers = findViewById(R.id.ManageTeahcers);
        manageStudents = findViewById(R.id.ManageStudents);
        textTotalAbsences = findViewById(R.id.textTotalAbsences);

    }

    private void setupButtons() {
        findViewById(R.id.fabAddStudent).setOnClickListener(v -> launch(CreateStudentActivity.class));
        findViewById(R.id.fabAddTeacher).setOnClickListener(v -> launch(CreateProfessorActivity.class));

        manageStudents.setOnClickListener(view -> launch(ManageStudentsActivity.class));
        manageTeachers.setOnClickListener(view -> launch(ManageProfessorsActivity.class));
        findViewById(R.id.fabAddAbsence).setOnClickListener(v -> launch(AddAbsenceActivity.class));
        findViewById(R.id.btnAdminTimetable).setOnClickListener(v -> launch(TimetableAdminActivity.class));
        findViewById(R.id.btnAddClass).setOnClickListener(v -> launch(CreateClassActivity.class));
        findViewById(R.id.btnAddSubject).setOnClickListener(v -> launch(CreateSubjectActivity.class));

    }

    private void launch(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }





    private void loadDashboardDataFromMongo() {
        apiService.getAdminDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AdminDashboard.this,
                            "Erreur dashboard code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                DashboardResponse data = response.body();

                textTotalStudents.setText(String.valueOf(data.getTotalStudents()));
                textTotalTeachers.setText(String.valueOf(data.getTotalTeachers()));
                textTotalAbsences.setText(String.valueOf(data.getTotalAbsences()));

                List<Note> notes = data.getRecentNotes();
                if (notes == null) notes = new ArrayList<>();
                noteAdapter.mettreAJourListe(notes);
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                Toast.makeText(AdminDashboard.this,
                        "Erreur réseau Mongo: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardDataFromMongo();
    }

}
