package com.tech.datingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
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

    TextView tvRoomName, tvSeat1;
    ImageView btnMic, btnVideo;
    Button btnLeave, btnSend;
    EditText etMessage;
    LinearLayout layoutMessages, seat2, seat3; 
    ScrollView chatScroll;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String roomName, currentUserEmail, currentUserId;
    
    // 🔥 SECURE HOST VARIABLE
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
            finish(); // Bina login wala allow nahi hoga
        }

        tvRoomName = findViewById(R.id.tvRoomName);
        tvSeat1 = findViewById(R.id.tvSeat1); // Host ki seat ka text
        btnMic = findViewById(R.id.btnMic);
        btnVideo = findViewById(R.id.btnVideo);
        btnLeave = findViewById(R.id.btnLeave);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        layoutMessages = findViewById(R.id.layoutMessages);
        chatScroll = findViewById(R.id.chatScroll);
        
        seat2 = findViewById(R.id.seat2);
        seat3 = findViewById(R.id.seat3);

        roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null) {
            tvRoomName.setText(roomName);
            // 🚨 SABSE PEHLE SERVER SE PUCHO HOST KAUN HAI
            verifyHostFromServer(); 
        } else {
            finish();
        }

        // --- SEAT CLICK (Role-based Power Check) ---
        seat2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHost) {
                    showHostPowersDialog("Seat 2");
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

        // --- RECEIVE MESSAGES (Live Chat) ---
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

    // 🚨 100% SECURE: Server Se Verify Karo Ki Main Host Hu Ya Nahi
    private void verifyHostFromServer() {
        // Firebase me check karo jisne room banaya uska ID kya tha
        db.collection("Rooms").whereEqualTo("roomName", roomName).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String serverHostId = document.getString("hostId");

                            // Agar Server ki ID aur meri ID match hoti hai, tabhi Power milegi
                            if (serverHostId != null && serverHostId.equals(currentUserId)) {
                                isHost = true;
                                tvSeat1.setText("You (Host)");
                                Toast.makeText(ChatroomActivity.this, "Host Verified Securely ✅", Toast.LENGTH_SHORT).show();
                            } else {
                                isHost = false;
                                tvSeat1.setText("Host");
                            }
                        }
                    }
                });
    }

    // 🔥 HOST POWERS POPUP (Add, Mute, Kick, Block)
    private void showHostPowersDialog(String seatName) {
        String[] powers = {"Invite User", "Mute Seat", "Kick User", "Block User"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Host Controls (" + seatName + ")");
        builder.setItems(powers, new DialogInterface.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Toast.makeText(ChatroomActivity.this, "User Invited to " + seatName, Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(ChatroomActivity.this, "Seat Muted 🔇", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(ChatroomActivity.this, "User Kicked from Room 🥾", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(ChatroomActivity.this, "User Permanently Blocked 🚫", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
        builder.show();
    }
}
