
package com.example.farocaretaker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class EditToolsFragment extends Fragment {

    // ── Views ──────────────────────────────────────────────────────────────
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchLocation;
    private LinearLayout rowBluetooth;
    private LinearLayout rowRelinkDevice;

    // ── Lifecycle ──────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_tools_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setInitialStates();
        setListeners();
    }

    // ── Bind ───────────────────────────────────────────────────────────────
    private void bindViews(View view) {
        switchDarkMode   = view.findViewById(R.id.switch_dark_mode);
        switchLocation   = view.findViewById(R.id.switch_location);
        rowBluetooth     = view.findViewById(R.id.row_bluetooth);
        rowRelinkDevice  = view.findViewById(R.id.row_relink_device);
    }

    // ── Initial States ─────────────────────────────────────────────────────
    private void setInitialStates() {
        // Reflect current night mode setting in the switch
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // TODO: restore switchLocation state from SharedPreferences
    }

    // ── Listeners ──────────────────────────────────────────────────────────
    private void setListeners() {

        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // TODO: persist choice to SharedPreferences
        });

        // Location toggle
        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: request ACCESS_FINE_LOCATION permission if isChecked
            // TODO: persist to SharedPreferences
        });

        // Bluetooth arrival ping → navigate to bluetooth setup screen
        rowBluetooth.setOnClickListener(v -> {
            // TODO: replace with NavController navigation when nav graph is ready
            // NavHostFragment.findNavController(this)
            //     .navigate(R.id.action_editTools_to_bluetoothSetup);
        });

        // Re-link dependent device → navigate to setup fragment
        rowRelinkDevice.setOnClickListener(v -> {
            // TODO: replace with NavController navigation when nav graph is ready
            // NavHostFragment.findNavController(this)
            //     .navigate(R.id.action_editTools_to_setup);
        });
    }
}