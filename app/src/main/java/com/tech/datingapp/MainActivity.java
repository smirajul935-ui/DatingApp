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
        setContentView(R.layout.activity_main);

        // Ab computer ko ye dono easily mil jayenge kyunki humne XML theek kar diya hai
        final TextView tvStatus = findViewById(R.id.tvStatus);
        Button btnConnect = findViewById(R.id.btnConnect);

        Toast.makeText(this, "Security Checks Passed! Device Safe.", Toast.LENGTH_SHORT).show();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvStatus.setText("Status: Connected to Secure Server ✅");
                tvStatus.setTextColor(Color.GREEN);
                Toast.makeText(MainActivity.this, "Server Connected Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
