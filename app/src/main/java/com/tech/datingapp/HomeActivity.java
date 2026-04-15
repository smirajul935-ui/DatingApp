package com.tech.datingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    ImageView btnMessages, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ye line humari nayi premium screen ko load karegi
        setContentView(R.layout.activity_home);

        // Top bar ke buttons ko link kiya
        btnMessages = findViewById(R.id.btnMessages);
        btnProfile = findViewById(R.id.btnProfile);

        // Agar buttons successfully find ho gaye, tabhi unpe click listener lagega
        if (btnMessages != null) {
            btnMessages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, "Opening Messages...", Toast.LENGTH_SHORT).show();
                    // Aage chalkar hum yaha "Message Activity" open karenge
                }
            });
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, "Opening Profile Settings...", Toast.LENGTH_SHORT).show();
                    // Aage chalkar hum yaha "Profile Activity" open karenge
                }
            });
        }
    }
}
