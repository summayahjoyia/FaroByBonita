// AddReminderFragment.java
// Place at: java/com/example/farocaretaker/AddReminderFragment.java

package com.example.farocaretaker;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class AddReminderFragment extends Fragment {

    private static final String TAG    = "AddReminderFragment";
    private static final String DB_URL = "https://farobybonita-default-rtdb.firebaseio.com/";

    // ── Views ──────────────────────────────────────────────────────────────
    private EditText       etMedicineName, etTime, etFrequency;
    private MaterialButton btnSave;
    private TextView       tvError;

    // ── Firebase ───────────────────────────────────────────────────────────
    private FirebaseDatabase  rtdb;
    private FirebaseFirestore firestore;
    private FirebaseAuth      mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_reminder_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rtdb      = FirebaseDatabase.getInstance(DB_URL);
        firestore = FirebaseFirestore.getInstance();
        mAuth     = FirebaseAuth.getInstance();

        etMedicineName = view.findViewById(R.id.et_medicine_name);
        etTime         = view.findViewById(R.id.et_time);
        etFrequency    = view.findViewById(R.id.et_frequency);
        btnSave        = view.findViewById(R.id.btn_save_reminder);
        tvError        = view.findViewById(R.id.tv_add_reminder_error);

        btnSave.setOnClickListener(v -> attemptSave());
    }

    private void attemptSave() {
        String medicine  = etMedicineName.getText().toString().trim();
        String time      = etTime.getText().toString().trim();
        String frequency = etFrequency.getText().toString().trim();

        if (TextUtils.isEmpty(medicine)) {
            tvError.setText("Medicine name is required");
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(time)) {
            tvError.setText("Time is required (e.g. 14:00)");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);
        btnSave.setEnabled(false);

        // Get deviceId from Firestore first
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

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
                    if (deviceId == null) {
                        Log.e(TAG, "No deviceId found");
                        return;
                    }
                    saveToRTDB(deviceId, medicine, time,
                            TextUtils.isEmpty(frequency) ? "daily" : frequency);
                });
    }

    private void saveToRTDB(String deviceId, String medicine, String time, String frequency) {
        Map<String, Object> reminder = new HashMap<>();
        reminder.put("medicine_name", medicine);
        reminder.put("time", time);
        reminder.put("frequency", frequency);
        reminder.put("active", true);

        rtdb.getReference("reminders")
                .child(deviceId)
                .push() // auto-generate ID
                .setValue(reminder)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Reminder saved");
                    // Go back to ReminderFragment
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Save failed: " + e.getMessage());
                    tvError.setText("Failed to save. Check connection.");
                    tvError.setVisibility(View.VISIBLE);
                    btnSave.setEnabled(true);
                });
    }
}