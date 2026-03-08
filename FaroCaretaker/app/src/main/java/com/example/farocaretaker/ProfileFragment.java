// ProfileFragment.java
// Place at: java/com/example/farocaretaker/ProfileFragment.java
// Shown as a BottomSheetDialogFragment — small floating popup

package com.example.farocaretaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends BottomSheetDialogFragment {

    // ── Views ──────────────────────────────────────────────────────────────
    private TextView     tvProfileEmail;
    private LinearLayout rowSignOut;

    // ── Firebase ───────────────────────────────────────────────────────────
    private FirebaseAuth mAuth;

    // ── Lifecycle ──────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        rowSignOut     = view.findViewById(R.id.row_sign_out);

        // Show current user email
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            tvProfileEmail.setText(user.getEmail());
        }

        rowSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            dismiss();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}