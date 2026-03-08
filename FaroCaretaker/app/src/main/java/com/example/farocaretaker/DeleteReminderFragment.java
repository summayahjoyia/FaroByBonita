// DeleteReminderFragment.java
// Place at: java/com/example/farocaretaker/DeleteReminderFragment.java

package com.example.farocaretaker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;

public class DeleteReminderFragment extends BottomSheetDialogFragment {

    private static final String DB_URL       = "https://farobybonita-default-rtdb.firebaseio.com/";
    private static final String ARG_ID       = "reminder_id";
    private static final String ARG_NAME     = "medicine_name";
    private static final String ARG_DEVICE   = "device_id";

    // ── Factory ────────────────────────────────────────────────────────────
    public static DeleteReminderFragment newInstance(String reminderId,
                                                     String medicineName,
                                                     String deviceId) {
        DeleteReminderFragment f = new DeleteReminderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID,     reminderId);
        args.putString(ARG_NAME,   medicineName);
        args.putString(ARG_DEVICE, deviceId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delete_reminder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String reminderId  = getArguments() != null ? getArguments().getString(ARG_ID)     : null;
        String medicineName = getArguments() != null ? getArguments().getString(ARG_NAME)   : null;
        String deviceId    = getArguments() != null ? getArguments().getString(ARG_DEVICE)  : null;

        TextView       tvMessage   = view.findViewById(R.id.tv_delete_message);
        MaterialButton btnConfirm  = view.findViewById(R.id.btn_confirm_delete);
        MaterialButton btnCancel   = view.findViewById(R.id.btn_cancel_delete);

        tvMessage.setText("Delete \"" + medicineName + "\"?");

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            if (reminderId != null && deviceId != null) {
                FirebaseDatabase.getInstance(DB_URL)
                        .getReference("reminders")
                        .child(deviceId)
                        .child(reminderId)
                        .removeValue();
            }
            dismiss();
        });
    }
}