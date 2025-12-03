package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Etudiant;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(Etudiant student);
        void onStudentLongClick(Etudiant student, View view);
    }

    private List<Etudiant> students;
    private final OnStudentClickListener listener;

    public StudentAdapter(List<Etudiant> students, OnStudentClickListener listener) {
        this.students = (students != null) ? students : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        if (position >= 0 && position < students.size()) {
            Etudiant student = students.get(position);
            if (student != null) {
                holder.bind(student, listener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (students != null) ? students.size() : 0;
    }

    public void updateList(List<Etudiant> newList) {
        this.students = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView studentName;
        private final TextView studentEmail;
        private final TextView studentClass;
        private final TextView studentMatricule;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            studentEmail = itemView.findViewById(R.id.studentEmail);
            studentClass = itemView.findViewById(R.id.studentClass);
            studentMatricule = itemView.findViewById(R.id.studentMatricule);
        }

        public void bind(@NonNull Etudiant student, @NonNull OnStudentClickListener listener) {

            // Nom complet
            String prenom = student.getPrenom() != null ? student.getPrenom() : "";
            String nom    = student.getNom() != null ? student.getNom() : "";
            String fullName = (prenom + " " + nom).trim();
            studentName.setText(fullName.isEmpty() ? "No Name" : fullName);

            // Email
            String email = student.getEmail();
            studentEmail.setText(email != null ? email : "No Email");

            // Code classe (ex: 2AP2AP1) ou classe simple
            String codeClasse = student.getCodeClasse();
            if (codeClasse == null || codeClasse.trim().isEmpty()) {
                // fallback sur le champ classe (ex: "2AP1")
                String classe = student.getClasse();
                studentClass.setText(classe != null && !classe.isEmpty() ? classe : "No Class");
            } else {
                studentClass.setText(codeClasse);
            }

            // Matricule (int → String)
            int matricule = student.getMatricule();
            if (matricule > 0) {
                studentMatricule.setText(String.valueOf(matricule));
            } else {
                studentMatricule.setText("No ID");
            }

            // Clicks
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onStudentClick(student);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onStudentLongClick(student, itemView);
                    return true;
                }
                return false;
            });
        }
    }
}
