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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private static final String DB_URL = "https://farobybonita-default-rtdb.firebaseio.com/";
    private static final String DEVICE_ID = "phone1";

    private TextView          tvNoReminder, tvReminderMedicine, tvReminderTime;
    private TextView          tvReminderBadge, tvSaveStatus;
    private TextView          tvDependentName, tvDependentRelationship;
    private LinearLayout      llReminderContent;
    private TextInputEditText etDisplayMessage;
    private MaterialButton    btnClearMessage;

    private ValueEventListener reminderListener = null;
    private ValueEventListener messageListener  = null;
    private DatabaseReference  deviceRef        = null;

    private FirebaseDatabase rtdb;

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
        deviceRef = rtdb.getReference("devices").child(DEVICE_ID);

        bindViews(view);
        attachMessageListener();
        attachReminderListener();
        setListeners();

        tvDependentName.setText("Mom");
        tvDependentRelationship.setText("Mother");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachListeners();
    }

    private void bindViews(View view) {
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
        deviceRef.child("reminder").addValueEventListener(messageListener);
    }

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
                        break;
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
        rtdb.getReference("reminders").child(DEVICE_ID).addValueEventListener(reminderListener);
    }

    private void detachListeners() {
        if (deviceRef != null && messageListener != null) {
            deviceRef.child("reminder").removeEventListener(messageListener);
        }
        if (reminderListener != null) {
            rtdb.getReference("reminders").child(DEVICE_ID).removeEventListener(reminderListener);
        }
        messageListener  = null;
        reminderListener = null;
    }

    private void setListeners() {
        etDisplayMessage.addTextChangedListener(messageWatcher);

        btnClearMessage.setOnClickListener(v -> {
            etDisplayMessage.setText("");
            saveMessage("");
        });
    }

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
        if (deviceRef == null) return;
        deviceRef.child("reminder").setValue(message)
                .addOnSuccessListener(unused ->
                        tvSaveStatus.setText("Changes save automatically"))
                .addOnFailureListener(e ->
                        tvSaveStatus.setText("Save failed — check connection"));
    }

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