// ReminderAdapter.java
// Place at: java/com/example/farocaretaker/ReminderAdapter.java

package com.example.farocaretaker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public interface OnToggleListener {
        void onToggle(Reminder reminder);
    }

    private final List<Reminder>   reminderList;
    private final OnToggleListener toggleListener;

    public ReminderAdapter(List<Reminder> reminderList, OnToggleListener toggleListener) {
        this.reminderList   = reminderList;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder r = reminderList.get(position);

        holder.textMedicineName.setText(r.getMedicineName() != null ? r.getMedicineName() : "—");
        holder.textTime.setText(r.getTime() != null ? r.getTime() : "—");
        holder.textFrequency.setText(r.getFrequency() != null ? r.getFrequency() : "—");

        // Set switch without triggering listener
        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(r.isActive());
        holder.switchActive.setOnCheckedChangeListener((btn, isChecked) ->
                toggleListener.onToggle(r));
    }

    @Override
    public int getItemCount() { return reminderList.size(); }

    public void updateList(List<Reminder> newList) {
        reminderList.clear();
        reminderList.addAll(newList);
        notifyDataSetChanged();
    }

    // ── ViewHolder ─────────────────────────────────────────────────────────
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView     textMedicineName, textTime, textFrequency;
        MaterialSwitch switchActive;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textMedicineName = itemView.findViewById(R.id.textMedicineName);
            textTime         = itemView.findViewById(R.id.textTime);
            textFrequency    = itemView.findViewById(R.id.textFrequency);
            switchActive     = itemView.findViewById(R.id.switchActive);
        }
    }
}