package com.example.bbuniversity.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherComplaintDetailActivity extends AppCompatActivity {

    // 🔹 maintenant on utilise l’ID Mongo, pas un path Firestore
    private String complaintId;
    private String noteId;          // si tu veux l’afficher ou l’utiliser plus tard

    private EditText etNewGrade;
    private TextView tvMessage;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_complaint_detail);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // 1) Récupération des extras
        complaintId = getIntent().getStringExtra("complaintId");
        noteId      = getIntent().getStringExtra("noteId");      // optionnel
        String message = getIntent().getStringExtra("description");

        // 2) Bind views
        etNewGrade = findViewById(R.id.etNewGrade);
        tvMessage  = findViewById(R.id.tvComplaintMessage);
        tvMessage.setText(message != null ? message : "");

        Button accept = findViewById(R.id.btnAcceptComplaint);
        Button reject = findViewById(R.id.btnRejectComplaint);

        apiService = ApiClient.getClient().create(ApiService.class);

        accept.setOnClickListener(v -> acceptComplaint());
        reject.setOnClickListener(v -> rejectComplaint());
    }

    // ---------- ACCEPTATION (note modifiée) ----------
    private void acceptComplaint() {
        String gradeStr = etNewGrade.getText().toString().trim();
        if (gradeStr.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer la nouvelle note", Toast.LENGTH_SHORT).show();
            return;
        }

        double grade;
        try {
            grade = Double.parseDouble(gradeStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Note invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (complaintId == null || complaintId.isEmpty()) {
            Toast.makeText(this, "ID de plainte manquant", Toast.LENGTH_SHORT).show();
            return;
        }

        // body envoyé au backend : { "modifiedGrade": 15.5 }
        Map<String, Object> body = new HashMap<>();
        body.put("modifiedGrade", grade);

        apiService.acceptComplaint(complaintId, body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(TeacherComplaintDetailActivity.this,
                                    "Plainte acceptée, note mise à jour",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(TeacherComplaintDetailActivity.this,
                                    "Erreur API accept: code=" + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(TeacherComplaintDetailActivity.this,
                                "Erreur réseau accept: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ---------- REJET (raison obligatoire) ----------
    private void rejectComplaint() {
        if (complaintId == null || complaintId.isEmpty()) {
            Toast.makeText(this, "ID de plainte manquant", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Raison du refus")
                .setView(input)
                .setPositiveButton("Envoyer", (d, w) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Veuillez entrer une raison", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> body = new HashMap<>();
                    body.put("response", reason);

                    apiService.rejectComplaint(complaintId, body)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(TeacherComplaintDetailActivity.this,
                                                "Plainte refusée",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(TeacherComplaintDetailActivity.this,
                                                "Erreur API reject: code=" + response.code(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(TeacherComplaintDetailActivity.this,
                                            "Erreur réseau reject: " + t.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
