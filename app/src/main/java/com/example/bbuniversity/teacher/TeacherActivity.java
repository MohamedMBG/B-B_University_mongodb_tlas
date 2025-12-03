package com.example.bbuniversity.teacher;

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

public class TeacherActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_teacher);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

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
            String email = etUsername.getText() != null
                    ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null
                    ? etPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            loginTeacher(email, password);
        });
    }

    private void loginTeacher(String email, String password) {
        // 1) Authentification via FirebaseAuth (email / mot de passe)
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = auth.getCurrentUser();
                    if (fbUser == null) {
                        Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = fbUser.getUid();

                    // 2) Chargement des infos utilisateur depuis MongoDB
                    apiService.getUser(uid).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(TeacherActivity.this,
                                        "Error loading user from Mongo (code=" + response.code() + ")",
                                        Toast.LENGTH_LONG).show();
                                auth.signOut();
                                return;
                            }

                            User user = response.body();
                            String role = user.getRole();

                            if (role != null && role.equalsIgnoreCase("professor")) {
                                Toast.makeText(TeacherActivity.this,
                                        "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(TeacherActivity.this, TeacherDashboard.class));
                                finish();
                            } else {
                                Toast.makeText(TeacherActivity.this,
                                        "Access denied: not a professor", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(TeacherActivity.this,
                                    "Network error (Mongo): " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
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
