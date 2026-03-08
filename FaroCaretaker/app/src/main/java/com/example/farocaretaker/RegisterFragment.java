// RegisterFragment.java
// res/layout: fragment_register.xml
// Place at: java/com/example/farocaretaker/RegisterFragment.java

package com.example.farocaretaker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    // ── Views ──────────────────────────────────────────────────────────────
    private TextInputLayout   tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton    btnCreateAccount;
    private ProgressBar       progressRegister;
    private TextView          tvError, tvGoLogin;
    private ImageButton       btnBack;

    // ── Firebase ───────────────────────────────────────────────────────────
    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    // ── Lifecycle ──────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        bindViews(view);
        setListeners();
    }

    // ── Bind ───────────────────────────────────────────────────────────────
    private void bindViews(View view) {
        tilName            = view.findViewById(R.id.til_name);
        tilEmail           = view.findViewById(R.id.til_email);
        tilPassword        = view.findViewById(R.id.til_password);
        tilConfirmPassword = view.findViewById(R.id.til_confirm_password);
        etName             = view.findViewById(R.id.et_name);
        etEmail            = view.findViewById(R.id.et_email);
        etPassword         = view.findViewById(R.id.et_password);
        etConfirmPassword  = view.findViewById(R.id.et_confirm_password);
        btnCreateAccount   = view.findViewById(R.id.btn_create_account);
        progressRegister   = view.findViewById(R.id.progress_register);
        tvError            = view.findViewById(R.id.tv_register_error);
        tvGoLogin          = view.findViewById(R.id.tv_go_login);
        btnBack            = view.findViewById(R.id.btn_back);
    }

    // ── Listeners ──────────────────────────────────────────────────────────
    private void setListeners() {
        btnCreateAccount.setOnClickListener(v -> attemptRegister());

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        tvGoLogin.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    // ── Register Logic ─────────────────────────────────────────────────────
    private void attemptRegister() {
        // Clear errors
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        showError(null);

        String name     = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm  = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        // Validation
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        setLoading(true);

        // Create Firebase Auth account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, name, email);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(friendlyError(e.getMessage()));
                });
    }

    // ── Firestore ──────────────────────────────────────────────────────────
    private void saveUserToFirestore(String uid, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("caregivers")
                .document(uid)
                .set(user)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    // Go to dependent setup — first time flow
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.login_fragment_container, new DependentFragment())
                            .commit(); // no addToBackStack — can't go back to register
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Account created but profile save failed. Please try again.");
                });
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────
    private void setLoading(boolean loading) {
        btnCreateAccount.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        progressRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (TextUtils.isEmpty(message)) {
            tvError.setVisibility(View.GONE);
            tvError.setText("");
        } else {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private String friendlyError(String raw) {
        if (raw == null) return "Something went wrong. Please try again.";
        if (raw.contains("email address is already in use")) return "An account with this email already exists.";
        if (raw.contains("badly formatted"))                 return "Please enter a valid email address.";
        if (raw.contains("network"))                         return "Network error. Check your connection.";
        return "Registration failed. Please try again.";
    }
}