package com.tech.datingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    // 🔒 Tumhara Render Server Link 
    String SERVER_URL = "https://datingserver-ymcg.onrender.com";
    
    TextView tvStatus;
    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ye line layout ko screen par dikhayegi
        setContentView(R.layout.activity_main); 

        tvStatus = findViewById(R.id.tvStatus);
        btnConnect = findViewById(R.id.btnConnect);

        // 🚨 APP START HOTE HI HACKER CHECK
        if (SecurityUtil.isHackerDevice()) {
            Toast.makeText(this, "Modded Device Blocked!", Toast.LENGTH_LONG).show();
            finishAffinity(); 
            return;
        }

        // Button dabane par server se token mangenge
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvStatus.setText("Status: Connecting to Server...");
                btnConnect.setEnabled(false); // Double click rokne ke liye
                testServerConnection();
            }
        });
    }

    private void testServerConnection() {
        String finalUrl = SERVER_URL + "?channelName=testRoom&uid=123";

        StringRequest request = new StringRequest(Request.Method.GET, finalUrl,
            response -> {
                // Screen par token dikhayega
                tvStatus.setText("Secure Token Received:\n" + response);
                tvStatus.setTextColor(android.graphics.Color.GREEN);
                btnConnect.setEnabled(true);
            },
            error -> {
                tvStatus.setText("Status: Server Connection Failed!");
                tvStatus.setTextColor(android.graphics.Color.RED);
                btnConnect.setEnabled(true);
            });

        Volley.newRequestQueue(this).add(request);
    }
}
