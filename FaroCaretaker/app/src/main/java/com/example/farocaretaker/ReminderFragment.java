package com.example.farocaretaker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReminderFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reminder_fragment, container, false);

        // Set up the list
        reminderList = new ArrayList<>();
        adapter = new ReminderAdapter(reminderList);
        recyclerView = view.findViewById(R.id.recyclerViewReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Point to the reminders node in Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("reminders");

        // Listen for any changes in Firebase in real time
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Reminder> freshList = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Reminder reminder = child.getValue(Reminder.class);

                    if (reminder != null) {
                        reminder.setId(child.getKey());
                        freshList.add(reminder);
                    }
                }

                // Push fresh data to the adapter so the list updates on screen
                adapter.updateList(freshList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Firebase read failed — safe to leave empty for now
            }
        });

        // FAB opens AddReminderFragment
        FloatingActionButton fab = view.findViewById(R.id.fabAddReminder);
        fab.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new AddReminderFragment())
                        .addToBackStack(null)
                        .commit()
        );

        return view;
    }
}