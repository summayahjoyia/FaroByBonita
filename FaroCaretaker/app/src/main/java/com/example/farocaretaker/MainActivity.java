package com.example.farocaretaker;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            Toast.makeText(this, "Step 1: Started", Toast.LENGTH_SHORT).show();

            FirebaseDatabase database = FirebaseDatabase.getInstance("https://farobybonita-default-rtdb.firebaseio.com/");
            DatabaseReference testRef = database.getReference("connectionTest");

            Toast.makeText(this, "Step 2: Got DB reference", Toast.LENGTH_SHORT).show();

            testRef.setValue("Caretaker connected!")
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Firebase connected!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

            Toast.makeText(this, "Step 3: setValue called", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Crash: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}