package com.example.bbuniversity.admin_panel;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button connectBtn, back;
    private TextView forgotPassword;

    private ApiService apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Views
        etUsername     = findViewById(R.id.etUsername);
        etPassword     = findViewById(R.id.etPassword);
        connectBtn     = findViewById(R.id.connect_btn);
        back           = findViewById(R.id.back);
        forgotPassword = findViewById(R.id.tvForgotPassword);

        back.setOnClickListener(v -> finish());

        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.bbuniversity.ForgotPasswordActivity.class)));

        // ✅ Mongo API
        apiService = ApiClient.getClient().create(ApiService.class);

        connectBtn.setOnClickListener(view -> {
            String username = String.valueOf(etUsername.getText()).trim();
            String password = String.valueOf(etPassword.getText()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Please enter both username and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            authenticateAdmin(username, password);
        });
    }

    /**
     * Auth admin uniquement via MongoDB.
     * On récupère le user d'id "admin" et on compare email + password saisis.
     */
    private void authenticateAdmin(String email, String password) {
        // On va chercher le user "admin" dans Mongo
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getUser("admin").enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AdminActivity.this,
                            "Erreur API ou admin introuvable", Toast.LENGTH_SHORT).show();
                    return;
                }

                User admin = response.body();

                // 🔒 Vérification email
                if (!email.equals(admin.getEmail())) {
                    Toast.makeText(AdminActivity.this,
                            "Email incorrect", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🔒 Vérification password (champ venant de Mongo)
                if (admin.getPassword() == null || !password.equals(admin.getPassword())) {
                    Toast.makeText(AdminActivity.this,
                            "Mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Succès
                Toast.makeText(AdminActivity.this,
                        "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminActivity.this, AdminDashboard.class));
                finish();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(AdminActivity.this,
                        "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
