package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.TimetableEntry;

import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private final List<TimetableEntry> entries;

    public TimetableAdapter(List<TimetableEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableEntry e = entries.get(position);
        holder.tvDay.setText(e.getDay());
        holder.tvSubject.setText(e.getSubject());
        holder.tvTime.setText(e.getStart() + " - " + e.getEnd());
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvSubject, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay     = itemView.findViewById(R.id.tvDay);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvTime    = itemView.findViewById(R.id.tvTime);
        }
    }
}
