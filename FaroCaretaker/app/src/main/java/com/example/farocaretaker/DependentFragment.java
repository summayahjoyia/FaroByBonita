// DependentFragment.java
// res/layout: dependent_fragment.xml
// Place at: java/com/example/farocaretaker/DependentFragment.java

package com.example.farocaretaker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependentFragment extends Fragment {

    // ── Relationship options ───────────────────────────────────────────────
    private static final List<String> RELATIONSHIPS = Arrays.asList(
            "Parent", "Grandparent", "Spouse / Partner",
            "Sibling", "Child", "Friend", "Other"
    );

    // ── Views ──────────────────────────────────────────────────────────────
    private TextInputLayout       tilDependentName, tilRelationship;
    private AutoCompleteTextView  actvRelationship;
    private android.widget.EditText etDependentName;
    private MaterialButton        btnContinue;
    private ProgressBar           progressDependent;
    private TextView              tvError, tvAddAnother;

    // ── State ──────────────────────────────────────────────────────────────
    private boolean addingAnother = false; // true when saving extra dependent mid-flow

    // ── Firebase ───────────────────────────────────────────────────────────
    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    // ── Lifecycle ──────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dependent_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        bindViews(view);
        setupRelationshipDropdown();
        setListeners();
    }

    // ── Bind ───────────────────────────────────────────────────────────────
    private void bindViews(View view) {
        tilDependentName  = view.findViewById(R.id.til_dependent_name);
        tilRelationship   = view.findViewById(R.id.til_relationship);
        actvRelationship  = view.findViewById(R.id.actv_relationship);
        etDependentName   = view.findViewById(R.id.et_dependent_name);
        btnContinue       = view.findViewById(R.id.btn_continue);
        progressDependent = view.findViewById(R.id.progress_dependent);
        tvError           = view.findViewById(R.id.tv_dependent_error);
        tvAddAnother      = view.findViewById(R.id.tv_add_another);
    }

    // ── Dropdown ───────────────────────────────────────────────────────────
    private void setupRelationshipDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                RELATIONSHIPS
        );
        actvRelationship.setAdapter(adapter);
    }

    // ── Listeners ──────────────────────────────────────────────────────────
    private void setListeners() {
        btnContinue.setOnClickListener(v -> attemptSave(false));

        // "Add another" saves current dependent then resets the form
        tvAddAnother.setOnClickListener(v -> attemptSave(true));
    }

    // ── Save Logic ─────────────────────────────────────────────────────────
    private void attemptSave(boolean addMore) {
        tilDependentName.setError(null);
        tilRelationship.setError(null);
        showError(null);

        String name         = etDependentName.getText() != null
                ? etDependentName.getText().toString().trim() : "";
        String relationship = actvRelationship.getText() != null
                ? actvRelationship.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            tilDependentName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(relationship)) {
            tilRelationship.setError("Please select a relationship");
            return;
        }

        this.addingAnother = addMore;
        setLoading(true);

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> dependent = new HashMap<>();
        dependent.put("name", name);
        dependent.put("relationship", relationship);
        dependent.put("caregiverId", uid);
        dependent.put("deviceId", "phone1"); // ← add this
        dependent.put("createdAt", System.currentTimeMillis());

        // Store under caregivers/{uid}/dependents/{auto-id}
        db.collection("caregivers")
                .document(uid)
                .collection("dependents")
                .add(dependent)
                .addOnSuccessListener(docRef -> {
                    setLoading(false);
                    if (addMore) {
                        resetForm(); // clear and let them add another
                    } else {
                        goToMain(); // all done — enter the app
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Failed to save. Please try again.");
                });
    }

    // ── Navigation ─────────────────────────────────────────────────────────
    private void goToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void resetForm() {
        etDependentName.setText("");
        actvRelationship.setText("");
        tilDependentName.setError(null);
        tilRelationship.setError(null);
        showError(null);

        // Update step hint to reflect they're adding another
        TextView tvSub = getView() != null
                ? getView().findViewById(R.id.tv_dependent_sub) : null;
        if (tvSub != null) tvSub.setText("Dependent saved! Add another below.");
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────
    private void setLoading(boolean loading) {
        btnContinue.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        progressDependent.setVisibility(loading ? View.VISIBLE : View.GONE);
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
}