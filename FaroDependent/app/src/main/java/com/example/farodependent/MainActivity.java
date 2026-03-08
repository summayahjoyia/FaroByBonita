package com.example.farodependent;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {

    private static final String DEVICE_ID = "phone1";
    private DatabaseReference deviceRef;
    private DatabaseReference visitorsRef;
    private ValueEventListener deviceListener;
    private ValueEventListener visitorsListener;
    private Handler clockHandler = new Handler();
    private Handler visitorHandler = new Handler();
    private TextView dateText, monthDayText, yearText, timeText, reminderText, visitorName;
    private ImageView visitorPhoto;
    private boolean visitorVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Hide status bar for full kiosk look
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setContentView(R.layout.activity_main);

        // Enter kiosk mode
        startLockTask();

        dateText = findViewById(R.id.dateText);
        monthDayText = findViewById(R.id.monthDayText);
        yearText = findViewById(R.id.yearText);
        timeText = findViewById(R.id.timeText);
        reminderText = findViewById(R.id.reminderText);
        visitorName = findViewById(R.id.visitorName);
        visitorPhoto = findViewById(R.id.visitorPhoto);

        startClock();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://farobybonita-default-rtdb.firebaseio.com/");
        deviceRef = database.getReference("devices").child(DEVICE_ID);
        visitorsRef = database.getReference("visitors").child(DEVICE_ID);

        deviceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String reminder = snapshot.child("reminder").getValue(String.class);
                if (reminder != null && !reminder.isEmpty()) {
                    reminderText.setText(reminder);
                    if (!visitorVisible) {
                        reminderText.setAlpha(1f);
                        fadeIn(reminderText);
                    }
                } else {
                    fadeOut(reminderText);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Cancelled: " + error.getMessage());
            }
        };
        deviceRef.addValueEventListener(deviceListener);

        visitorsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String reminder = snapshot.child("reminder").getValue(String.class);
                Toast.makeText(MainActivity.this, "Reminder: " + reminder, Toast.LENGTH_LONG).show();
                if (reminder != null && !reminder.isEmpty()) {
                    reminderText.setText(reminder);
                    if (!visitorVisible) {
                        reminderText.setAlpha(1f);
                    }
                } else {
                    fadeOut(reminderText);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Visitor listener cancelled: " + error.getMessage());
            }
        };
        visitorsRef.addValueEventListener(visitorsListener);
    }

    private void showVisitor(String name, DatabaseReference visitorRef) {
        visitorVisible = true;
        fadeOut(reminderText);
        visitorName.setText(name + " is home");
        fadeIn(visitorName);
        fadeIn(visitorPhoto);

        visitorHandler.removeCallbacksAndMessages(null);
        visitorHandler.postDelayed(() -> {
            fadeOut(visitorName);
            fadeOut(visitorPhoto);
            visitorVisible = false;
            visitorRef.child("active").setValue(false);
        }, 60000);
    }

    private void startClock() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Edmonton"));
        clockHandler.post(new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                dateText.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(now));
                monthDayText.setText(new SimpleDateFormat("MMMM d", Locale.getDefault()).format(now));
                yearText.setText(new SimpleDateFormat("yyyy", Locale.getDefault()).format(now));
                timeText.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(now));
                clockHandler.postDelayed(this, 1000);
            }
        });
    }

    private void fadeIn(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        anim.setDuration(1500);
        anim.start();
    }

    private void fadeOut(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        anim.setDuration(1500);
        anim.start();
    }

    @Override
    public void onBackPressed() {
        // Prevent exiting
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacksAndMessages(null);
        visitorHandler.removeCallbacksAndMessages(null);
        if (deviceRef != null && deviceListener != null) {
            deviceRef.removeEventListener(deviceListener);
        }
        if (visitorsRef != null && visitorsListener != null) {
            visitorsRef.removeEventListener(visitorsListener);
        }
    }
}