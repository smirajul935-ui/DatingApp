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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
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
    LinearLayout layoutMessages, seat2, seat3; // Example seats
    ScrollView chatScroll;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String roomName;
    String currentUserEmail;
    String currentUserId;
    
    // 🔥 HOST CHECKING VARIABLE
    boolean isHost = false; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if(mAuth.getCurrentUser() != null) {
            currentUserEmail = mAuth.getCurrentUser().getEmail();
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            currentUserEmail = "Guest";
            currentUserId = "GuestID";
        }

        tvRoomName = findViewById(R.id.tvRoomName);
        btnMic = findViewById(R.id.btnMic);
        btnVideo = findViewById(R.id.btnVideo);
        btnLeave = findViewById(R.id.btnLeave);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        layoutMessages = findViewById(R.id.layoutMessages);
        chatScroll = findViewById(R.id.chatScroll);
        
        // Seats
        seat2 = findViewById(R.id.seat2);
        seat3 = findViewById(R.id.seat3);

        roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null) {
            tvRoomName.setText(roomName);
            checkIfUserIsHost(); // 🔥 Firebase se puchenge kya hum host hain?
        } else {
            roomName = "Public Room";
        }

        // --- SEAT CLICKS ---
        seat2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHost) {
                    Toast.makeText(ChatroomActivity.this, "HOST POWER: Invite User to Seat", Toast.LENGTH_SHORT).show();
                    // Host invite list open karega (Aage banayenge)
                } else {
                    Toast.makeText(ChatroomActivity.this, "Only Host can add you to a seat!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // --- SEND MESSAGES ---
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

        // --- RECEIVE MESSAGES ---
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

        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // 🔥 Check Firebase ki is room ka Host kaun hai
    private void checkIfUserIsHost() {
        // Note: Hum aage chal kar yaha Firebase fetch query lagayenge. 
        // Abhi test karne ke liye: "Jo room banata hai, uske paas host power hoti hai"
        // Hum next phase me Host ki ID check karenge.
        isHost = true; // Yaha True/False change karke tum Host aur Audience test kar sakte ho.
    }
}
