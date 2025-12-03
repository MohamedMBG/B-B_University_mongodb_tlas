package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Abscence;

import java.util.List;

public class AbsenceAdapter extends RecyclerView.Adapter<AbsenceAdapter.AbsenceViewHolder> {

    private List<Abscence> absences;

    public AbsenceAdapter(List<Abscence> absences) {
        this.absences = absences;
    }

    @NonNull
    @Override
    public AbsenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.absence_item, parent, false);
        return new AbsenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsenceViewHolder holder, int position) {
        Abscence absence = absences.get(position);

        // Matière
        holder.subject.setText("Matière : " + safe(absence.getMatiere()));

        // Justifiée ou non
        holder.justified.setText("Justifiée : " + (absence.isJustifiee() ? "Oui" : "Non"));

        // Date : tu l’enregistres déjà au format "dd/MM/yyyy" dans AddAbsenceActivity
        String dateStr = absence.getDate();
        holder.date.setText(dateStr != null && !dateStr.isEmpty()
                ? dateStr
                : "Date inconnue");
    }

    @Override
    public int getItemCount() {
        return absences != null ? absences.size() : 0;
    }

    public void updateData(List<Abscence> newList) {
        this.absences = newList;
        notifyDataSetChanged();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    static class AbsenceViewHolder extends RecyclerView.ViewHolder {
        TextView subject, justified, date;

        AbsenceViewHolder(@NonNull View itemView) {
            super(itemView);
            subject   = itemView.findViewById(R.id.abs_subject);
            justified = itemView.findViewById(R.id.abs_justified);
            date      = itemView.findViewById(R.id.abs_date);
        }
    }
}
