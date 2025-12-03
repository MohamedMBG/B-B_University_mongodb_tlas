package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> listeNotes;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

    public NoteAdapter(List<Note> listeNotes) {
        // pour éviter un NPE si on passe null
        this.listeNotes = (listeNotes != null) ? listeNotes : new ArrayList<>();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = listeNotes.get(position);

        // Matière
        String matiere = note.getMatiere() != null
                ? formatMatiereId(note.getMatiere())
                : "Matière inconnue";
        holder.textMatiere.setText("Matière: " + matiere);

        // Notes
        holder.textControle.setText(
                String.format(Locale.FRANCE, "Contrôle continu: %.1f", note.getControle()));
        holder.textExamen.setText(
                String.format(Locale.FRANCE, "Examen: %.1f", note.getExamenFinal()));
        holder.textParticipation.setText(
                String.format(Locale.FRANCE, "Participation: %.1f", note.getParticipation()));
        holder.textMoyenne.setText(
                String.format(Locale.FRANCE, "Moyenne: %.1f", note.getNoteGenerale()));

        // Date (si tu as un champ String ISO côté Note)
        if (note.getDerniereMiseAJour() != null &&
                !note.getDerniereMiseAJour().isEmpty()) {
            holder.textDate.setText("Mise à jour: " + note.getDerniereMiseAJour());
        } else {
            holder.textDate.setText("");
        }

        // Nom de l'étudiant (pour l'instant on affiche l'id)
        // → si tu ajoutes un champ studentName côté backend/Note, tu l'utilises ici.
        if (note.getStudentId() != null) {
            holder.textEtudiant.setText("Étudiant: " + note.getStudentId());
        } else {
            holder.textEtudiant.setText("Étudiant: inconnu");
        }
    }

    @Override
    public int getItemCount() {
        return (listeNotes != null) ? listeNotes.size() : 0;
    }

    public void mettreAJourListe(List<Note> nouvellesNotes) {
        this.listeNotes = (nouvellesNotes != null) ? nouvellesNotes : new ArrayList<>();
        notifyDataSetChanged();
    }

    private String formatMatiereId(String matiereId) {
        if (matiereId == null) return "Matière inconnue";

        switch (matiereId) {
            case "math_101":
            case "MATH_101":
                return "Mathématiques";
            case "phys_101":
            case "PHYS_101":
                return "Physique";
            case "info_101":
            case "INFO_101":
                return "Informatique";
            default:
                return matiereId;
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textMatiere, textControle, textExamen, textParticipation,
                textMoyenne, textDate, textEtudiant;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textMatiere = itemView.findViewById(R.id.textMatiere);
            textControle = itemView.findViewById(R.id.textControle);
            textExamen = itemView.findViewById(R.id.textExamen);
            textParticipation = itemView.findViewById(R.id.textParticipation);
            textMoyenne = itemView.findViewById(R.id.textMoyenne);
            textDate = itemView.findViewById(R.id.textDate);
            textEtudiant = itemView.findViewById(R.id.textEtudiant);
        }
    }
}
