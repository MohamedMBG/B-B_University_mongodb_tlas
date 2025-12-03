package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateSubjectActivity extends AppCompatActivity {

    private TextInputEditText etSubjectName;
    private Button btnCreate, btnCancel;
    private FirebaseFirestore db;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_subject);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        etSubjectName = findViewById(R.id.etSubjectName);
        btnCreate = findViewById(R.id.btnCreateSubject);
        btnCancel = findViewById(R.id.btnCancel);
        db = FirebaseFirestore.getInstance();
        apiService = ApiClient.getClient().create(ApiService.class);

        btnCreate.setOnClickListener(v -> addSubject());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void addSubject() {
        String nom = etSubjectName.getText().toString().trim();
        if (nom.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer le nom de la matière", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("nom", nom);

        // 1) Firestore
        db.collection("matieres").document(nom).set(data)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "Matière créée (Firestore)", Toast.LENGTH_SHORT).show();

                    // 2) Mongo via API
                    Map<String, Object> mongoData = new HashMap<>(data);
                    mongoData.put("_id", nom);

                    apiService.createMatiere(mongoData).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(CreateSubjectActivity.this,
                                        "Matière synchronisée dans MongoDB",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                String err = "code=" + response.code();
                                try {
                                    if (response.errorBody() != null) {
                                        err += " body=" + response.errorBody().string();
                                    }
                                } catch (Exception ignored) {}
                                Toast.makeText(CreateSubjectActivity.this,
                                        "Erreur API Mongo: " + err,
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(CreateSubjectActivity.this,
                                    "Erreur réseau Mongo: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
