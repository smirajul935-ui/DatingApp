package com.tech.datingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    String SERVER_URL = "https://datingserver-ymcg.onrender.com";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 

        // Variables
        final TextView tvStatus = findViewById(R.id.tvStatus);
        final Button btnConnect = findViewById(R.id.btnConnect);

        // Button Click Event (Bina shortcut ke)
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvStatus.setText("Connecting to Server...");
                btnConnect.setEnabled(false); 
                
                String finalUrl = SERVER_URL + "?channelName=testRoom&uid=123";
                
                StringRequest request = new StringRequest(Request.Method.GET, finalUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            tvStatus.setText("Token Received:\n" + response);
                            btnConnect.setEnabled(true);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            tvStatus.setText("Connection Failed!");
                            btnConnect.setEnabled(true);
                        }
                    });
                    
                Volley.newRequestQueue(MainActivity.this).add(request);
            }
        });
    }
}
