package com.tech.datingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChatroomActivity extends AppCompatActivity {

    TextView tvRoomName;
    ImageView btnMic;
    EditText etMessage;
    Button btnSend;
    LinearLayout layoutMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        tvRoomName = findViewById(R.id.tvRoomName);
        btnMic = findViewById(R.id.btnMic);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        layoutMessages = findViewById(R.id.layoutMessages);

        // Pichli screen se room ka naam lena (Jaise "Love Talk")
        String roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null) {
            tvRoomName.setText(roomName);
        }

        // Send Button Click
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if(!message.isEmpty()){
                Toast.makeText(this, "Message Sent: " + message, Toast.LENGTH_SHORT).show();
                etMessage.setText(""); // Box khali karna
                // Aage chalkar hum yaha Firebase me message bhejenge
            }
        });

        // Mic Button Click (Voice Chat on/off ke liye)
        btnMic.setOnClickListener(v -> {
            Toast.makeText(this, "Voice Chat feature coming soon!", Toast.LENGTH_SHORT).show();
            // Aage chalkar hum yaha Agora connect karenge
        });
    }
}
