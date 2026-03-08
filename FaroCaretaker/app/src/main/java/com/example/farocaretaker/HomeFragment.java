package com.example.farocaretaker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
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

public class HomeFragment extends Fragment {

    private static final String DB_URL = "https://farobybonita-default-rtdb.firebaseio.com/";

    // ── Views ──────────────────────────────────────────────────────────────
    private TabLayout           tabLayoutDependents;
    private TextView            tvNoReminder, tvReminderMedicine, tvReminderTime;
    private TextView            tvReminderBadge, tvSaveStatus;
    private TextView            tvDependentName, tvDependentRelationship;
    private LinearLayout        llReminderContent;
    private TextInputEditText   etDisplayMessage;
    private MaterialButton      btnClearMessage;

    // ── State ──────────────────────────────────────────────────────────────
    private final List<String> dependentIds        = new ArrayList<>();
    private final List<String> dependentNames      = new ArrayList<>();
    private final List<String> deviceIds           = new ArrayList<>();
    private String             currentDeviceId     = null;
    private ValueEventListener reminderListener    = null;
    private ValueEventListener messageListener     = null;
    private DatabaseReference  currentDeviceRef    = null;

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
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rtdb      = FirebaseDatabase.getInstance(DB_URL);
        firestore = FirebaseFirestore.getInstance();
        mAuth     = FirebaseAuth.getInstance();

        bindViews(view);
        loadDependents();
        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachListeners();
    }

    // ── Bind ───────────────────────────────────────────────────────────────
    private void bindViews(View view) {
        //tabLayoutDependents     = view.findViewById(R.id.tab_layout_dependents);
        tvNoReminder            = view.findViewById(R.id.tv_no_reminder);
        llReminderContent       = view.findViewById(R.id.ll_reminder_content);
        tvReminderMedicine      = view.findViewById(R.id.tv_reminder_medicine);
        tvReminderTime          = view.findViewById(R.id.tv_reminder_time);
        tvReminderBadge         = view.findViewById(R.id.tv_reminder_badge);
        etDisplayMessage        = view.findViewById(R.id.et_display_message);
        tvSaveStatus            = view.findViewById(R.id.tv_save_status);
        btnClearMessage         = view.findViewById(R.id.btn_clear_message);
        tvDependentName         = view.findViewById(R.id.tv_dependent_name);
        tvDependentRelationship = view.findViewById(R.id.tv_dependent_relationship);
    }

    // ── Load Dependents from Firestore ─────────────────────────────────────
    private void loadDependents() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        firestore.collection("caregivers")
                .document(uid)
                .collection("dependents")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    tabLayoutDependents.removeAllTabs();
                    dependentIds.clear();
                    dependentNames.clear();
                    deviceIds.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name     = doc.getString("name");
                        String deviceId = doc.getString("deviceId"); // e.g. "phone1"

                        dependentIds.add(doc.getId());
                        dependentNames.add(name != null ? name : "Dependent");
                        deviceIds.add(deviceId != null ? deviceId : "");

                        TabLayout.Tab tab = tabLayoutDependents.newTab();
                        tab.setText(name);
                        tabLayoutDependents.addTab(tab);
                    }

                    // "+" tab for adding more dependents
                    TabLayout.Tab addTab = tabLayoutDependents.newTab();
                    addTab.setText("+");
                    tabLayoutDependents.addTab(addTab);

                    if (!deviceIds.isEmpty()) {
                        selectDependent(0);
                    }
                });
    }

    // ── Select Dependent ──────────────────────────────────────────────────
    private void selectDependent(int index) {
        if (index >= deviceIds.size()) return;

        detachListeners();

        currentDeviceId  = deviceIds.get(index);
        currentDeviceRef = rtdb.getReference("devices").child(currentDeviceId);

        tvDependentName.setText(dependentNames.get(index));

        // Load relationship from Firestore
        if (mAuth.getCurrentUser() != null) {
            firestore.collection("caregivers")
                    .document(mAuth.getCurrentUser().getUid())
                    .collection("dependents")
                    .document(dependentIds.get(index))
                    .get()
                    .addOnSuccessListener(doc -> {
                        String rel = doc.getString("relationship");
                        if (rel != null) tvDependentRelationship.setText(rel);
                    });
        }

        attachMessageListener();
        attachReminderListener();
    }

    // ── Realtime: message listener ─────────────────────────────────────────
    private void attachMessageListener() {
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String msg = snapshot.getValue(String.class);
                etDisplayMessage.removeTextChangedListener(messageWatcher);
                etDisplayMessage.setText(msg != null ? msg : "");
                etDisplayMessage.addTextChangedListener(messageWatcher);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        currentDeviceRef.child("reminder").addValueEventListener(messageListener);
    }

    // ── Realtime: next reminder listener ──────────────────────────────────
    private void attachReminderListener() {
        reminderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nextMedicine = null;
                String nextTime     = null;

                for (DataSnapshot child : snapshot.getChildren()) {
                    Boolean active = child.child("active").getValue(Boolean.class);
                    if (Boolean.TRUE.equals(active)) {
                        nextMedicine = child.child("medicine_name").getValue(String.class);
                        nextTime     = child.child("time").getValue(String.class);
                        break; // take first active reminder
                    }
                }

                if (nextMedicine != null) {
                    showReminder(nextMedicine, nextTime != null ? nextTime : "");
                } else {
                    showNoReminder();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showNoReminder();
            }
        };
        rtdb.getReference("reminders")
                .child(currentDeviceId)
                .addValueEventListener(reminderListener);
    }

    // ── Detach listeners when switching tabs or leaving ────────────────────
    private void detachListeners() {
        if (currentDeviceRef != null && messageListener != null) {
            currentDeviceRef.child("reminder").removeEventListener(messageListener);
        }
        if (currentDeviceId != null && reminderListener != null) {
            rtdb.getReference("reminders")
                    .child(currentDeviceId)
                    .removeEventListener(reminderListener);
        }
        messageListener  = null;
        reminderListener = null;
    }

    // ── Tab listener ──────────────────────────────────────────────────────
    private void setListeners() {
        tabLayoutDependents.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                if (index < deviceIds.size()) {
                    selectDependent(index);
                }
                // "+" tab: TODO open DependentFragment to add new dependent
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        etDisplayMessage.addTextChangedListener(messageWatcher);

        btnClearMessage.setOnClickListener(v -> {
            etDisplayMessage.setText("");
            saveMessage("");
        });
    }

    // ── Message TextWatcher — debounced write to RTDB ─────────────────────
    private final TextWatcher messageWatcher = new TextWatcher() {
        private final android.os.Handler handler = new android.os.Handler();
        private Runnable runnable;

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            tvSaveStatus.setText("Saving…");
            if (runnable != null) handler.removeCallbacks(runnable);
            runnable = () -> saveMessage(s.toString());
            handler.postDelayed(runnable, 800);
        }
        @Override public void afterTextChanged(Editable s) {}
    };

    private void saveMessage(String message) {
        if (currentDeviceRef == null) return;
        currentDeviceRef.child("reminder").setValue(message)
                .addOnSuccessListener(unused ->
                        tvSaveStatus.setText("Changes save automatically"))
                .addOnFailureListener(e ->
                        tvSaveStatus.setText("Save failed — check connection"));
    }

    // ── UI Helpers ────────────────────────────────────────────────────────
    private void showNoReminder() {
        if (tvNoReminder == null) return;
        tvNoReminder.setVisibility(View.VISIBLE);
        llReminderContent.setVisibility(View.GONE);
    }

    private void showReminder(String medicine, String time) {
        if (tvNoReminder == null) return;
        tvNoReminder.setVisibility(View.GONE);
        llReminderContent.setVisibility(View.VISIBLE);
        tvReminderMedicine.setText(medicine);
        tvReminderTime.setText(time);
        tvReminderBadge.setText(time);
    }
}