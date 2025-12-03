package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Complaint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of Complaint objects (Mongo version).
 */
public class ComplaintAdapter
        extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
    }

    private final List<Complaint> complaints;
    private final OnComplaintClickListener listener;

    // pour parser les dates ISO venant du backend (ex: 2025-12-01T22:14:03.123Z)
    private final SimpleDateFormat isoParser =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private final SimpleDateFormat displayFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ComplaintAdapter(List<Complaint> complaints,
                            OnComplaintClickListener listener) {
        this.complaints = complaints;
        this.listener   = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Complaint c = complaints.get(pos);

        // Titre
        holder.titleView.setText(
                c.getTitle() != null ? c.getTitle() : "Réclamation"
        );

        // Statut
        holder.statusView.setText(
                c.getStatus() != null ? c.getStatus() : "pending"
        );

        // Message
        holder.messageView.setText(
                c.getDescription() != null ? c.getDescription() : ""
        );

        // Date (String ISO → jolie date)
        String rawDate = c.getDateFiled();  // doit être String dans ton modèle
        if (rawDate != null && !rawDate.isEmpty()) {
            try {
                Date d = isoParser.parse(rawDate);
                holder.dateView.setText(displayFormat.format(d));
            } catch (ParseException e) {
                // si parse foire, on affiche brut
                holder.dateView.setText(rawDate);
            }
        } else {
            holder.dateView.setText("Date inconnue");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onComplaintClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return complaints != null ? complaints.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleView;
        final TextView statusView;
        final TextView messageView;
        final TextView dateView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView   = itemView.findViewById(R.id.complaintTitle);
            statusView  = itemView.findViewById(R.id.complaintStatus);
            messageView = itemView.findViewById(R.id.complaintMessage);
            dateView    = itemView.findViewById(R.id.complaintDate);
        }
    }
}
