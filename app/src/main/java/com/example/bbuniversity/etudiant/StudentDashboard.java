package com.example.bbuniversity.etudiant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.EmailSender;
import com.example.bbuniversity.R;
import com.example.bbuniversity.TimetableViewActivity;
import com.example.bbuniversity.adapters.AbsenceAdapter;
import com.example.bbuniversity.adapters.StudentNoteAdapter;
import com.example.bbuniversity.api.ApiClient;
import com.example.bbuniversity.api.ApiService;
import com.example.bbuniversity.models.Abscence;
import com.example.bbuniversity.models.Complaint;
import com.example.bbuniversity.models.Note;
import com.example.bbuniversity.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboard extends AppCompatActivity {

    private TextView tvName, tvFiliere, tvMatricule, tvClasse,
            absencesCountText, tvOverallGrade, tvViewAllGrades;
    private RecyclerView absencesRecyclerView, notesRecyclerView;
    private String studentClassName = "";

    private final List<Abscence> absenceList = new ArrayList<>();
    private final List<Note> noteList       = new ArrayList<>();
    private AbsenceAdapter adapter;
    private StudentNoteAdapter noteAdapter;

    private ApiService apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        apiService = ApiClient.getClient().create(ApiService.class);

        // UI bindings
        tvName            = findViewById(R.id.FullStdName);
        tvFiliere         = findViewById(R.id.stdFiliere);
        tvMatricule       = findViewById(R.id.stdMatricule);
        tvClasse          = findViewById(R.id.stdClasse);
        absencesCountText = findViewById(R.id.absences_count_text);
        absencesRecyclerView = findViewById(R.id.absences_recycler_view);
        notesRecyclerView    = findViewById(R.id.notes_recycler_view);
        tvOverallGrade       = findViewById(R.id.tvOverallGrade);
        tvViewAllGrades      = findViewById(R.id.tvViewAllGrades);
        Button btnViewGrades   = findViewById(R.id.btnViewGrades);
        Button btnLogout       = findViewById(R.id.btnLogout);
        Button btnViewAbsences = findViewById(R.id.btnViewAbsences);
        Button btnTimetable    = findViewById(R.id.btnTimetable);

        btnTimetable.setOnClickListener(v -> {
            Intent i = new Intent(this, StudentTimetableActivity.class);
            i.putExtra("class", studentClassName);   // on passe la classe vers l’activity
            startActivity(i);
        });

        // Voir toutes les absences
        btnViewAbsences.setOnClickListener(
                v -> startActivity(new Intent(this, StudentAbsencesActivity.class)));

        // Logout
        btnLogout.setOnClickListener(v -> logout());

        // Absences list
        adapter = new AbsenceAdapter(absenceList);
        absencesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        absencesRecyclerView.setAdapter(adapter);

        // Notes list + réclamation
        noteAdapter = new StudentNoteAdapter(noteList, this::showComplaintDialog);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);

        // Aller à toutes les notes
        View.OnClickListener openAll = v ->
                startActivity(new Intent(this, StudentGradesActivity.class));
        tvViewAllGrades.setOnClickListener(openAll);
        btnViewGrades.setOnClickListener(openAll);

        // 🔑 Récupération UID Firebase puis données dans Mongo
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();        // ✅ UID = _id dans Mongo
            loadUserDataFromMongo(uid);
            loadAbsencesFromMongo(uid);
            loadNotesFromMongo(uid);
        } else {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
        }
    }

    /* -------------------------------------------------
     *  RÉCLAMATION
     * ------------------------------------------------- */

    private void showComplaintDialog(Note note) {
        final android.widget.EditText input = new android.widget.EditText(this);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Réclamation")
                .setMessage("Expliquez votre réclamation")
                .setView(input)
                .setPositiveButton("Envoyer", (d, w) -> {
                    String message = input.getText().toString().trim();
                    sendComplaint(note, message);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void sendComplaint(Note note, String description) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Complaint complaint = new Complaint();
        complaint.setStudentId(user.getUid());          // ✅ UID
        complaint.setTeacherId(note.getProfesseurId()); // doit être _id Mongo du prof
        complaint.setSubjectId(note.getMatiere());
        complaint.setNoteId(note.getId());
        complaint.setInitialGrade(note.getNoteGenerale());
        complaint.setModifiedGrade(note.getNoteGenerale());
        complaint.setTitle("Réclamation sur la note");
        complaint.setDescription(description);
        complaint.setResponse("");
        complaint.setStatus("pending");

        apiService.createComplaint(complaint)
                .enqueue(new Callback<Complaint>() {
                    @Override
                    public void onResponse(Call<Complaint> call, Response<Complaint> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(StudentDashboard.this,
                                    "Réclamation envoyée", Toast.LENGTH_SHORT).show();
                            notifyTeacher(note.getProfesseurId(), description);
                        } else {
                            Toast.makeText(StudentDashboard.this,
                                    "Erreur API réclamation code=" + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Complaint> call, Throwable t) {
                        Toast.makeText(StudentDashboard.this,
                                "Erreur réseau réclamation: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Email-notify the teacher using Mongo API */
    private void notifyTeacher(String profId, String message) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String studentName = currentUser != null ? currentUser.getDisplayName() : "Étudiant";

        apiService.getUser(profId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                String email = response.body().getEmail();
                if (email == null || email.isEmpty()) return;

                new Thread(() -> {
                    try {
                        EmailSender.sendEmail(
                                email,
                                "Nouvelle réclamation reçue",
                                "Bonjour,\nL'étudiant " + studentName +
                                        " a envoyé une réclamation :\n\n" + message
                        );
                    } catch (MessagingException e) {
                        Log.e("Email", "failed to send", e);
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("notifyTeacher", "Erreur récupération prof Mongo", t);
            }
        });
    }

    /* -------------------------------------------------
     *  NOTES (Mongo)
     * ------------------------------------------------- */

    private void loadNotesFromMongo(String uid) {
        apiService.getNotesByStudent(uid).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(StudentDashboard.this,
                            "Erreur chargement notes code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                noteList.clear();
                noteList.addAll(response.body());
                noteAdapter.updateNotes(noteList);

                double total = 0;
                int count = 0;
                for (Note n : noteList) {
                    total += n.getNoteGenerale();
                    count++;
                }
                if (count > 0) {
                    double avg = total / count;
                    tvOverallGrade.setText(
                            String.format(Locale.FRANCE, "%.2f/20", avg)
                    );
                } else {
                    tvOverallGrade.setText("--/20");
                }
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                Toast.makeText(StudentDashboard.this,
                        "Erreur réseau notes: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /* -------------------------------------------------
     *  ABSENCES (Mongo)
     * ------------------------------------------------- */

    private void loadAbsencesFromMongo(String uid) {
        apiService.getAbsences(uid).enqueue(new Callback<List<Abscence>>() {
            @Override
            public void onResponse(Call<List<Abscence>> call, Response<List<Abscence>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(StudentDashboard.this,
                            "Erreur chargement absences code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                absenceList.clear();
                absenceList.addAll(response.body());
                absencesCountText.setText("Nombre d'absences : " + absenceList.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Abscence>> call, Throwable t) {
                Toast.makeText(StudentDashboard.this,
                        "Erreur réseau absences: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /* -------------------------------------------------
     *  UTILISATEUR (Mongo)
     * ------------------------------------------------- */

    private void loadUserDataFromMongo(String uid) {
        apiService.getUser(uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(StudentDashboard.this,
                            "Erreur chargement utilisateur code=" + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                updateUI(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(StudentDashboard.this,
                        "Erreur réseau utilisateur: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(User user) {
        if (user == null) {
            Toast.makeText(this,
                    "Données utilisateur non trouvées.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String nom       = safe(user.getNom());
        String prenom    = safe(user.getPrenom());
        String filiere   = safe(user.getFiliere());
        String matricule = String.valueOf(user.getMatricule());
        String niveau    = String.valueOf(user.getNiveau());
        String classe    = safe(user.getClasse());

        studentClassName = classe; // pour l'emploi du temps

        tvName.setText(nom + " " + prenom);
        tvFiliere.setText(filiere);
        tvMatricule.setText(matricule);
        tvClasse.setText(niveau + " " + filiere + " " + classe);
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    /* -------------------------------------------------
     *  LOGOUT
     * ------------------------------------------------- */

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, StudentActivity.class));
        finish();
    }
}
