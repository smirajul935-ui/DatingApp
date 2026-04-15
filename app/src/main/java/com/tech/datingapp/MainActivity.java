package com.tech.datingapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ye line tumhari us black wali XML screen ko app me layegi
        setContentView(R.layout.activity_main);

        // XML wale Button aur TextView ko Java se jod rahe hain
        final TextView tvStatus = findViewById(R.id.tvStatus);
        Button btnConnect = findViewById(R.id.btnConnect);

        // App khulte hi ye message aayega
        Toast.makeText(this, "Security Checks Passed! Device Safe.", Toast.LENGTH_SHORT).show();

        // Jab user 'Connect to Server' button dabayega tab kya hoga:
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Text badal kar connected ho jayega aur Hara (Green) ho jayega
                tvStatus.setText("Status: Connected to Secure Server ✅");
                tvStatus.setTextColor(Color.GREEN);
                
                // Niche ek popup message bhi aayega
                Toast.makeText(MainActivity.this, "Server Connected Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
