// SplashActivity.java
// Place at: java/com/example/farocaretaker/SplashActivity.java

package com.example.farocaretaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final long FADE_DURATION = 600L;
    private static final long HOLD_DURATION = 900L;
    private static final long TAGLINE_DELAY = 200L;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvTagline = findViewById(R.id.tv_tagline);

        tvAppName.animate()
                .alpha(1f)
                .setDuration(FADE_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() ->
                        tvTagline.animate()
                                .alpha(1f)
                                .setDuration(FADE_DURATION)
                                .setStartDelay(TAGLINE_DELAY)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(this::routeAfterDelay)
                                .start()
                )
                .start();
    }

    private void routeAfterDelay() {
        findViewById(R.id.tv_app_name).postDelayed(this::route, HOLD_DURATION);
    }

    private void route() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}