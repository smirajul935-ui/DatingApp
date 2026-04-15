package com.tech.datingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChatroomActivity extends AppCompatActivity {

    TextView tvRoomName;
    ImageView btnMic, btnVideo;
    Button btnLeave, btnSend;
    EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        tvRoomName = findViewById(R.id.tvRoomName);
        btnMic = findViewById(R.id.btnMic);
        btnVideo = findViewById(R.id.btnVideo);
        btnLeave = findViewById(R.id.btnLeave);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);

        // Home screen se room ka naam lena
        String roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null) {
            tvRoomName.setText(roomName);
        }

        // Send Message
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if(!message.isEmpty()){
                Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show();
                etMessage.setText(""); // Box khali karo
            }
        });

        // Mic Click (Mute / Unmute)
        btnMic.setOnClickListener(v -> {
            Toast.makeText(this, "Mic toggled!", Toast.LENGTH_SHORT).show();
        });

        // Video Click
        btnVideo.setOnClickListener(v -> {
            Toast.makeText(this, "Video Camera toggled!", Toast.LENGTH_SHORT).show();
        });

        // Leave Room Click (Wapas pichli screen pe jana)
        btnLeave.setOnClickListener(v -> {
            finish(); // Room band karke wapas home pe bhej dega
        });
    }
}
