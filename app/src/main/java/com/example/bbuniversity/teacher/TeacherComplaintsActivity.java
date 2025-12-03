package com.example.bbuniversity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.ComplaintAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Complaint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherComplaintsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ComplaintAdapter adapter;
    private final List<Complaint> complaints = new ArrayList<>();

    private ApiService apiService;

    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadComplaintsFromMongo();   // 🔄 on rafraîchit depuis l’API
            handler.postDelayed(this, 3000);  // relance dans 3 secondes
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_complaints);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        recycler = findViewById(R.id.recyclerComplaints);
        if (recycler == null) {
            Log.e("TeacherComplaints", "RecyclerView is null! Check your XML id.");
            finish();
            return;
        }

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(complaints, this::openDetail);
        recycler.setAdapter(adapter);

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            Log.e("TeacherComplaints", "No logged-in user!");
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 1er chargement
        loadComplaintsFromMongo();
    }

    // ---------- CHARGER DEPUIS MONGO ----------
    private void loadComplaintsFromMongo() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = current.getUid();   // on utilise l’UID Firebase comme teacherId API

        apiService.getComplaintsForTeacher(teacherId)
                .enqueue(new Callback<List<Complaint>>() {
                    @Override
                    public void onResponse(Call<List<Complaint>> call,
                                           Response<List<Complaint>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(TeacherComplaintsActivity.this,
                                    "Erreur chargement plaintes : code=" + response.code(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        complaints.clear();
                        complaints.addAll(response.body());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<List<Complaint>> call, Throwable t) {
                        Log.e("TeacherComplaints", "API error", t);
                        Toast.makeText(TeacherComplaintsActivity.this,
                                "Erreur réseau : " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(refreshRunnable);  // démarrage de la boucle auto-refresh
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(refreshRunnable);  // arrêt de la boucle
    }

    // ---------- OUVERTURE DU DÉTAIL ----------
    private void openDetail(@NonNull Complaint complaint) {
        Intent intent = new Intent(this, TeacherComplaintDetailActivity.class);

        // 👉 ici on envoie l’ID Mongo, pas un documentPath Firestore
        intent.putExtra("complaintId", complaint.getId());     // champ _id dans Mongo → getId()
        intent.putExtra("noteId",      complaint.getNoteId());
        intent.putExtra("studentId",   complaint.getStudentId());
        intent.putExtra("description", complaint.getDescription());
        intent.putExtra("initialGrade", complaint.getInitialGrade());
        intent.putExtra("status",      complaint.getStatus());

        startActivity(intent);
    }
}
