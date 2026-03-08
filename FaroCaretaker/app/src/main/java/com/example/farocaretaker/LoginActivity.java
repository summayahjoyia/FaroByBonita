// LoginActivity.java
// Place at: java/com/example/farocaretaker/LoginActivity.java
// This is a thin host activity — all UI logic lives in LoginFragment.java

package com.example.farocaretaker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Load LoginFragment into the container on first create only
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.login_fragment_container, new LoginFragment())
                    .commit();
        }
    }
}