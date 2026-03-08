// LoginFragment.java
// res/layout: login_fragment.xml
// Place at: java/com/example/farocaretaker/LoginFragment.java

package com.example.farocaretaker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    // ── Views ──────────────────────────────────────────────────────────────
    private TextInputLayout   tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton    btnSignIn;
    private ProgressBar       progressLogin;
    private TextView          tvError, tvGoRegister;

    // ── Firebase ───────────────────────────────────────────────────────────
    private FirebaseAuth mAuth;

    // ── Lifecycle ──────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        bindViews(view);
        setListeners();
    }

    // ── Bind ───────────────────────────────────────────────────────────────
    private void bindViews(View view) {
        tilEmail      = view.findViewById(R.id.til_email);
        tilPassword   = view.findViewById(R.id.til_password);
        etEmail       = view.findViewById(R.id.et_email);
        etPassword    = view.findViewById(R.id.et_password);
        btnSignIn     = view.findViewById(R.id.btn_sign_in);
        progressLogin = view.findViewById(R.id.progress_login);
        tvError       = view.findViewById(R.id.tv_login_error);
        tvGoRegister  = view.findViewById(R.id.tv_go_register);
    }

    // ── Listeners ──────────────────────────────────────────────────────────
    private void setListeners() {
        btnSignIn.setOnClickListener(v -> attemptLogin());

        tvGoRegister.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.login_fragment_container, new RegisterFragment())
                        .addToBackStack(null) // back arrow returns to login
                        .commit()
        );
    }

    // ── Login Logic ────────────────────────────────────────────────────────
    private void attemptLogin() {
        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);
        showError(null);

        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        // Basic validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    // Navigate to MainActivity — clear back stack so user can't go back to login
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(friendlyError(e.getMessage()));
                });
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────
    private void setLoading(boolean loading) {
        btnSignIn.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
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

    /** Maps Firebase error messages to user-friendly strings */
    private String friendlyError(String rawMessage) {
        if (rawMessage == null) return "Something went wrong. Please try again.";
        if (rawMessage.contains("no user record"))      return "No account found with that email.";
        if (rawMessage.contains("password is invalid")) return "Incorrect password. Please try again.";
        if (rawMessage.contains("badly formatted"))     return "Please enter a valid email address.";
        if (rawMessage.contains("network"))             return "Network error. Check your connection.";
        return "Sign in failed. Please try again.";
    }
}