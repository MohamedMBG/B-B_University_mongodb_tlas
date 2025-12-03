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

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateClassActivity extends AppCompatActivity {

    private TextInputEditText etClassName;
    private Button btnCreate, btnCancel;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_class);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        etClassName = findViewById(R.id.etClassName);
        btnCreate = findViewById(R.id.btnCreateClass);
        btnCancel = findViewById(R.id.btnCancel);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnCreate.setOnClickListener(v -> addClass());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void addClass() {
        String name = etClassName.getText() != null
                ? etClassName.getText().toString().trim()
                : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer le nom de la classe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Corps à envoyer à l’API
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("_id", name); //

        apiService.createClass(data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateClassActivity.this,
                            "Classe créée dans MongoDB",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String err = "code=" + response.code();
                    try {
                        if (response.errorBody() != null) {
                            err += " body=" + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(CreateClassActivity.this,
                            "Erreur API Mongo: " + err,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateClassActivity.this,
                        "Erreur réseau Mongo: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
