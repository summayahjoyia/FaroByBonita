package com.example.farocaretaker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminderList;

    public ReminderAdapter(List<Reminder> reminderList) {
        this.reminderList = reminderList;
    }

    // Called when RecyclerView needs a new card — inflates item_reminder.xml
    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    // Called for each card — fills it with actual reminder data
    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder currentReminder = reminderList.get(position);
        holder.textMedicineName.setText(currentReminder.getMedicineName());
        holder.textTime.setText("Time: " + currentReminder.getTime());
        holder.textFrequency.setText("Frequency: " + currentReminder.getFrequency());
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    // ViewHolder represents one single card on screen
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {

        TextView textMedicineName;
        TextView textTime;
        TextView textFrequency;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textMedicineName = itemView.findViewById(R.id.textMedicineName);
            textTime = itemView.findViewById(R.id.textTime);
            textFrequency = itemView.findViewById(R.id.textFrequency);
        }
    }

    // Call this from ReminderFragment when Firebase sends new data
    public void updateList(List<Reminder> newList) {
        reminderList.clear();
        reminderList.addAll(newList);
        notifyDataSetChanged();
    }
}