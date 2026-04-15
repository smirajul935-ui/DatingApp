package com.tech.datingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChatroomActivity extends AppCompatActivity {

    TextView tvRoomName;
    ImageView btnMic, btnVideo;
    Button btnLeave, btnSend;
    EditText etMessage;
    LinearLayout layoutMessages;
    ScrollView chatScroll;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String roomName;
    String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        // Firebase initialize
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if(mAuth.getCurrentUser() != null) {
            currentUserEmail = mAuth.getCurrentUser().getEmail();
        } else {
            currentUserEmail = "Guest";
        }

        tvRoomName = findViewById(R.id.tvRoomName);
        btnMic = findViewById(R.id.btnMic);
        btnVideo = findViewById(R.id.btnVideo);
        btnLeave = findViewById(R.id.btnLeave);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        layoutMessages = findViewById(R.id.layoutMessages);
        chatScroll = findViewById(R.id.chatScroll);

        // Room ka naam set karna
        roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null) {
            tvRoomName.setText(roomName);
        } else {
            roomName = "Public Room";
        }

        // 1. MESSAGES KO FIREBASE ME SAVE KARNA (Send Button)
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if(!message.isEmpty()){
                etMessage.setText(""); // Box khali karo

                // Database me bhejne ke liye data taiyar karna
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("sender", currentUserEmail);
                chatData.put("message", message);
                chatData.put("timestamp", FieldValue.serverTimestamp());

                // Firebase me bhejna
                db.collection("Chatrooms").document(roomName).collection("Messages")
                        .add(chatData);
            }
        });

        // 2. FIREBASE SE MESSAGES SCREEN PAR DIKHANA (Real-time Live Chat)
        db.collection("Chatrooms").document(roomName).collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String msgText = dc.getDocument().getString("message");
                            String sender = dc.getDocument().getString("sender");

                            // Screen par naya message dikhane ke liye Design banana
                            TextView newMsg = new TextView(ChatroomActivity.this);
                            
                            // Email me se naam nikalna (jaise test@gmail.com ka "test")
                            String shortName = sender.split("@")[0];
                            
                            newMsg.setText(shortName + ": " + msgText);
                            newMsg.setTextColor(android.graphics.Color.WHITE);
                            newMsg.setTextSize(16f);
                            newMsg.setPadding(0, 0, 0, 20);

                            // Message ko screen (LinearLayout) me add karna
                            layoutMessages.addView(newMsg);

                            // Scroll karke sabse naye message par aana
                            chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
                        }
                    }
                });

        // Other buttons
        btnMic.setOnClickListener(v -> Toast.makeText(this, "Mic toggled!", Toast.LENGTH_SHORT).show());
        btnVideo.setOnClickListener(v -> Toast.makeText(this, "Video Camera toggled!", Toast.LENGTH_SHORT).show());
        btnLeave.setOnClickListener(v -> finish());
    }
}
