package com.tech.datingapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    // 🔒 Tumhara Render Server Link (Yaha apni Render wali link daalna)
    String SERVER_URL = "https://datingserver-ymcg.onrender.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🚨 APP START HOTE HI HACKER CHECK HOGA
        if (SecurityUtil.isHackerDevice()) {
            Toast.makeText(this, "Modded / Emulator Device Not Allowed! Blocked.", Toast.LENGTH_LONG).show();
            finishAffinity(); // App turant band
            return;
        }

        // Agar safe hai toh ye chalega
        Toast.makeText(this, "Device Safe. Server Se Connect kar rahe hain...", Toast.LENGTH_SHORT).show();
        testServerConnection();
    }

    private void testServerConnection() {
        String finalUrl = SERVER_URL + "?channelName=testRoom&uid=123";

        StringRequest request = new StringRequest(Request.Method.GET, finalUrl,
            response -> {
                // Token mil gaya! Hacker nahi chura sakta kyuki render par safe hai.
                Toast.makeText(this, "Token Received Securely: " + response.substring(0, 15) + "...", Toast.LENGTH_LONG).show();
            },
            error -> {
                Toast.makeText(this, "Connection Failed!", Toast.LENGTH_SHORT).show();
            });

        Volley.newRequestQueue(this).add(request);
    }
}
