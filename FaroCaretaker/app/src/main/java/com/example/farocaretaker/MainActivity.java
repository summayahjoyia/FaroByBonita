// MainActivity.java
// Place at: java/com/example/farocaretaker/MainActivity.java

package com.example.farocaretaker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_reminder) {
                loadFragment(new ReminderFragment());
                return true;
            } else if (id == R.id.nav_edit) {
                loadFragment(new EditToolsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                // Show as floating bottom sheet, don't replace fragment
                new ProfileFragment()
                        .show(getSupportFragmentManager(), "profile");
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}