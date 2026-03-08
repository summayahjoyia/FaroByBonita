package com.example.farodependent;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String DEVICE_ID = "phone1";
    private DatabaseReference deviceRef;
    private DatabaseReference visitorsRef;
    private DatabaseReference remindersRef;
    private ValueEventListener deviceListener;
    private ValueEventListener visitorsListener;
    private ValueEventListener remindersListener;
    private Handler clockHandler = new Handler();
    private Handler visitorHandler = new Handler();
    private Handler medicineHandler = new Handler();
    private TextView dateText, monthDayText, yearText, timeText, reminderText, visitorName;
    private ImageView visitorPhoto;
    private boolean visitorVisible = false;
    private boolean medicineVisible = false;
    private String defaultMessage = "";

    private List<String[]> reminderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setContentView(R.layout.activity_main);
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
        remindersRef = database.getReference("reminders").child(DEVICE_ID);

        deviceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String reminder = snapshot.child("reminder").getValue(String.class);
                if (reminder != null && !reminder.isEmpty()) {
                    defaultMessage = reminder;
                    if (!visitorVisible && !medicineVisible) {
                        reminderText.setText(defaultMessage);
                        reminderText.setAlpha(1f);
                    }
                } else {
                    defaultMessage = "";
                    fadeOut(reminderText);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Cancelled: " + error.getMessage());
            }
        };
        deviceRef.addValueEventListener(deviceListener);

        remindersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reminderList.clear();
                for (DataSnapshot reminder : snapshot.getChildren()) {
                    Boolean active = reminder.child("active").getValue(Boolean.class);
                    String time = reminder.child("time").getValue(String.class);
                    String medicineName = reminder.child("medicine_name").getValue(String.class);
                    if (active != null && active && time != null && medicineName != null) {
                        reminderList.add(new String[]{time, medicineName});
                    }
                }
                Log.d("Reminders", "Loaded " + reminderList.size() + " reminders");
                for (String[] r : reminderList) {
                    Log.d("Reminders", "Time: " + r[0] + " Medicine: " + r[1]);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Reminders cancelled: " + error.getMessage());
            }
        };
        remindersRef.addValueEventListener(remindersListener);

        visitorsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot visitor : snapshot.getChildren()) {
                    Boolean active = visitor.child("active").getValue(Boolean.class);
                    if (active != null && active) {
                        String name = visitor.child("name").getValue(String.class);
                        String imageUrl = visitor.child("image_url").getValue(String.class);
                        showVisitor(name, imageUrl, visitor.getRef());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Visitor listener cancelled: " + error.getMessage());
            }
        };
        visitorsRef.addValueEventListener(visitorsListener);
    }

    private void showVisitor(String name, String imageUrl, DatabaseReference visitorRef) {
        visitorVisible = true;
        fadeOut(reminderText);
        visitorName.setText(name + " is home");
        fadeIn(visitorName);

        visitorPhoto.setAlpha(1f);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .circleCrop()
                    .into(visitorPhoto);
        }

        visitorHandler.removeCallbacksAndMessages(null);
        visitorHandler.postDelayed(() -> {
            fadeOut(visitorName);
            fadeOut(visitorPhoto);
            visitorVisible = false;
            visitorRef.child("active").setValue(false);
            if (!defaultMessage.isEmpty()) {
                reminderText.setText(defaultMessage);
                fadeIn(reminderText);
            }
        }, 60000);
    }

    private void showMedicineReminder(String medicineName) {
        if (visitorVisible) return;
        medicineVisible = true;
        reminderText.setText("Time for your " + medicineName);
        fadeIn(reminderText);

        medicineHandler.removeCallbacksAndMessages(null);
        medicineHandler.postDelayed(() -> {
            medicineVisible = false;
            if (!defaultMessage.isEmpty()) {
                reminderText.setText(defaultMessage);
                reminderText.setAlpha(1f);
            } else {
                fadeOut(reminderText);
            }
        }, 60000);
    }

    private void startClock() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Edmonton"));
        clockHandler.post(new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now);

                dateText.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(now));
                monthDayText.setText(new SimpleDateFormat("MMMM d", Locale.getDefault()).format(now));
                yearText.setText(new SimpleDateFormat("yyyy", Locale.getDefault()).format(now));
                timeText.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(now));

                Log.d("Reminders", "Current time: " + currentTime + " checking " + reminderList.size() + " reminders");

                for (String[] reminder : reminderList) {
                    String reminderTime = reminder[0];
                    String medicineName = reminder[1];
                    if (currentTime.equals(reminderTime) && !medicineVisible) {
                        showMedicineReminder(medicineName);
                    }
                }

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
        medicineHandler.removeCallbacksAndMessages(null);
        if (deviceRef != null && deviceListener != null) {
            deviceRef.removeEventListener(deviceListener);
        }
        if (visitorsRef != null && visitorsListener != null) {
            visitorsRef.removeEventListener(visitorsListener);
        }
        if (remindersRef != null && remindersListener != null) {
            remindersRef.removeEventListener(remindersListener);
        }
    }
}