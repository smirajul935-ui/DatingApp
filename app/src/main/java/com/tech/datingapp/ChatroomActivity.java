package com.tech.datingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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

        // 1. SEND BUTTON (Message save karna)
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if(!message.isEmpty()){
                    etMessage.setText(""); 

                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("sender", currentUserEmail);
                    chatData.put("message", message);
                    chatData.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("Chatrooms").document(roomName).collection("Messages").add(chatData);
                }
            }
        });

        // 2. RECEIVE MESSAGES (Real-time Chat)
        db.collection("Chatrooms").document(roomName).collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null || value == null) return;

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String msgText = dc.getDocument().getString("message");
                                String sender = dc.getDocument().getString("sender");

                                if (msgText != null && sender != null) {
                                    TextView newMsg = new TextView(ChatroomActivity.this);
                                    String shortName = sender.split("@")[0];
                                    
                                    newMsg.setText(shortName + ": " + msgText);
                                    newMsg.setTextColor(android.graphics.Color.WHITE);
                                    newMsg.setTextSize(16f);
                                    newMsg.setPadding(0, 0, 0, 20);

                                    layoutMessages.addView(newMsg);

                                    chatScroll.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatScroll.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });

        // Other buttons
        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatroomActivity.this, "Mic toggled!", Toast.LENGTH_SHORT).show();
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatroomActivity.this, "Video Camera toggled!", Toast.LENGTH_SHORT).show();
            }
        });

        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
