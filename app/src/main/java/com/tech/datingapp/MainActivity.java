package com.tech.datingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    String SERVER_URL = "https://datingserver-ymcg.onrender.com";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 

        TextView tvStatus = findViewById(R.id.tvStatus);
        Button btnConnect = findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(v -> {
            tvStatus.setText("Connecting...");
            btnConnect.setEnabled(false); 
            
            String finalUrl = SERVER_URL + "?channelName=testRoom&uid=123";
            StringRequest request = new StringRequest(Request.Method.GET, finalUrl,
                response -> {
                    tvStatus.setText("Token:\n" + response);
                    btnConnect.setEnabled(true);
                },
                error -> {
                    tvStatus.setText("Failed!");
                    btnConnect.setEnabled(true);
                });
            Volley.newRequestQueue(this).add(request);
        });
    }
}
