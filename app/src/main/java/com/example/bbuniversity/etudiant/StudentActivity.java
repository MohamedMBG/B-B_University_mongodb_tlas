package com.example.bbuniversity.etudiant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button connectBtn, back;
    private TextView forgotPassword;

    private FirebaseAuth auth;
    private ApiService apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student);

        etUsername      = findViewById(R.id.etUsername);
        etPassword      = findViewById(R.id.etPassword);
        connectBtn      = findViewById(R.id.connect_btn);
        back            = findViewById(R.id.back);
        forgotPassword  = findViewById(R.id.tvForgotPassword);

        auth       = FirebaseAuth.getInstance();
        apiService = ApiClient.getClient().create(ApiService.class);

        back.setOnClickListener(v -> finish());

        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.bbuniversity.ForgotPasswordActivity.class)));

        connectBtn.setOnClickListener(v -> {
            String email    = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            loginStudent(email, password);
        });
    }

    private void loginStudent(String email, String password) {
        // 1) Auth Firebase (pour rester cohérent avec la création de compte)
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = auth.getCurrentUser();
                    if (fbUser == null) {
                        Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2) Récupération des infos dans Mongo :
                    //    _id = email   (très important)
                    apiService.getUser(email).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(StudentActivity.this,
                                        "Error fetching user data code = " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                return;
                            }

                            User u = response.body();
                            if (!"student".equalsIgnoreCase(u.getRole())) {
                                Toast.makeText(StudentActivity.this,
                                        "Access denied: not a student",
                                        Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                return;
                            }

                            // OK → on ouvre le dashboard étudiant
                            Toast.makeText(StudentActivity.this,
                                    "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(StudentActivity.this, StudentDashboard.class));
                            finish();
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(StudentActivity.this,
                                    "Network error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            auth.signOut();
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Login failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
