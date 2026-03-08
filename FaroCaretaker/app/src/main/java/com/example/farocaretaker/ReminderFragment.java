// ReminderFragment.java
// Place at: java/com/example/farocaretaker/ReminderFragment.java

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReminderFragment extends Fragment {

    private static final String DB_URL = "https://farobybonita-default-rtdb.firebaseio.com/";

    // ── Views ──────────────────────────────────────────────────────────────
    private RecyclerView            recyclerView;
    private ReminderAdapter         adapter;
    private FloatingActionButton    fabAddReminder;

    // ── State ──────────────────────────────────────────────────────────────
    private final List<Reminder>    reminderList   = new ArrayList<>();
    private String                  currentDeviceId = null;
    private ValueEventListener      reminderListener = null;
    private DatabaseReference       remindersRef    = null;

    // ── Firebase ───────────────────────────────────────────────────────────
    private FirebaseDatabase  rtdb;
    private FirebaseFirestore firestore;
    private FirebaseAuth      mAuth;

    // ── Lifecycle ──────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reminder_fragment, container, false);

        rtdb      = FirebaseDatabase.getInstance(DB_URL);
        firestore = FirebaseFirestore.getInstance();
        mAuth     = FirebaseAuth.getInstance();

        recyclerView   = view.findViewById(R.id.recyclerViewReminders);
        fabAddReminder = view.findViewById(R.id.fabAddReminder);

        adapter = new ReminderAdapter(reminderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadDeviceIdThenReminders();
        setListeners();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (remindersRef != null && reminderListener != null) {
            remindersRef.removeEventListener(reminderListener);
        }
    }

    // ── Load deviceId from Firestore, then listen to RTDB ─────────────────
    private void loadDeviceIdThenReminders() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // Load first dependent's deviceId to start
        firestore.collection("caregivers")
                .document(uid)
                .collection("dependents")
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) return;
                    QueryDocumentSnapshot doc =
                            (QueryDocumentSnapshot) query.getDocuments().get(0);
                    String deviceId = doc.getString("deviceId");
                    if (deviceId != null) {
                        currentDeviceId = deviceId;
                        listenToReminders(deviceId);
                    }
                });
    }

    // ── Realtime Database listener ─────────────────────────────────────────
    private void listenToReminders(String deviceId) {
        remindersRef     = rtdb.getReference("reminders").child(deviceId);
        reminderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Reminder> fresh = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String  id           = child.getKey();
                    String  medicineName = child.child("medicine_name").getValue(String.class);
                    String  time         = child.child("time").getValue(String.class);
                    String  frequency    = child.child("frequency").getValue(String.class);
                    Boolean active       = child.child("active").getValue(Boolean.class);

                    Reminder r = new Reminder();
                    r.setId(id);
                    r.setMedicineName(medicineName);
                    r.setTime(time);
                    r.setFrequency(frequency);
                    r.setActive(Boolean.TRUE.equals(active));
                    fresh.add(r);
                }
                adapter.updateList(fresh);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        remindersRef.addValueEventListener(reminderListener);
    }

    // ── Listeners ──────────────────────────────────────────────────────────
    private void setListeners() {
        fabAddReminder.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddReminderFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }
}