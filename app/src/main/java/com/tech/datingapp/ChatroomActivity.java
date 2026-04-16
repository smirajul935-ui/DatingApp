package com.tech.datingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.gms.tasks.OnSuccessListener;
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

    TextView tvRoomName, tvMicStatus;
    ImageView btnMic, btnSend, btnClose;
    EditText etMessage;
    LinearLayout layoutMessages, hostInfo; 
    ScrollView chatScroll;

    // Seats variables
    TextView tvSeat1, tvSeat2, tvSeat3, tvSeat4, tvSeat5, tvSeat6, tvSeat7, tvSeat8;
    ImageView ivSeat1, ivSeat2, ivSeat3, ivSeat4, ivSeat5, ivSeat6, ivSeat7, ivSeat8;
    LinearLayout seat1, seat2, seat3, seat4, seat5, seat6, seat7, seat8;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String roomName, currentUserEmail, currentUserId, currentUserName;
    
    boolean isHost = false; 
    boolean isOnSeat = false; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if(mAuth.getCurrentUser() != null) {
            currentUserEmail = mAuth.getCurrentUser().getEmail();
            currentUserId = mAuth.getCurrentUser().getUid();
            fetchMyProfileName(); // Humara asli naam nikalne ke liye
        } else {
            finish(); 
            return;
        }

        tvRoomName = findViewById(R.id.tvRoomName);
        tvMicStatus = findViewById(R.id.tvMicStatus);
        btnMic = findViewById(R.id.btnMic);
        btnSend = findViewById(R.id.btnSend);
        btnClose = findViewById(R.id.btnClose);
        etMessage = findViewById(R.id.etMessage);
        layoutMessages = findViewById(R.id.layoutMessages);
        chatScroll = findViewById(R.id.chatScroll);
        hostInfo = findViewById(R.id.hostInfo);
        
        // Seat mappings
        seat1 = findViewById(R.id.seat1); tvSeat1 = findViewById(R.id.tvSeat1); ivSeat1 = findViewById(R.id.seat1).findViewById(R.id.ivHostAvatar); // Host
        seat2 = findViewById(R.id.seat2); tvSeat2 = findViewById(R.id.tvSeat2); ivSeat2 = findViewById(R.id.seat2).findViewById(R.id.ivHostAvatar);
        // Baki seats ko baad me link karenge jab hum "+" logic fully implement karenge
        
        roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null) {
            if(tvRoomName != null) tvRoomName.setText("👑 " + roomName);
            verifyHostFromServer(); 
        } else {
            finish();
            return;
        }

        if(btnClose != null) {
            btnClose.setOnClickListener(v -> showExitDialog());
        }

        if(btnMic != null) {
            btnMic.setOnClickListener(v -> {
                if (isHost || isOnSeat) {
                    tvMicStatus.setText("Your mic is ON. Say hello to others! 🎙️");
                    tvMicStatus.setTextColor(android.graphics.Color.GREEN);
                    btnMic.setColorFilter(android.graphics.Color.GREEN);
                } else {
                    Toast.makeText(ChatroomActivity.this, "❌ You are in Audience. Wait for Host to invite you!", Toast.LENGTH_LONG).show();
                }
            });
        }

        // --- SEND MESSAGES (Ab UserName bhejega Email nahi) ---
        if(btnSend != null && etMessage != null) {
            btnSend.setOnClickListener(v -> {
                String message = etMessage.getText().toString().trim();
                if(!message.isEmpty()){
                    etMessage.setText(""); 
                    Map<String, Object> chatData = new HashMap<>();
                    // Use actual name if available, else email prefix
                    String nameToUse = (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : currentUserEmail.split("@")[0];
                    chatData.put("senderName", nameToUse);
                    chatData.put("message", message);
                    chatData.put("timestamp", FieldValue.serverTimestamp());
                    db.collection("Chatrooms").document(roomName).collection("Messages").add(chatData);
                }
            });
        }

        // --- RECEIVE MESSAGES (Asli Naam Dikhayega) ---
        if(layoutMessages != null && chatScroll != null) {
            db.collection("Chatrooms").document(roomName).collection("Messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null || value == null) return;
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String msgText = dc.getDocument().getString("message");
                                String senderName = dc.getDocument().getString("senderName"); // Email ki jagah Name
                                
                                if (msgText != null && senderName != null) {
                                    TextView newMsg = new TextView(ChatroomActivity.this);
                                    newMsg.setText(senderName + ": " + msgText);
                                    newMsg.setTextColor(android.graphics.Color.WHITE);
                                    newMsg.setTextSize(16f);
                                    newMsg.setPadding(0, 0, 0, 20);
                                    layoutMessages.addView(newMsg);
                                    chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
                                }
                            }
                        }
                    });
        }
    }

    private void fetchMyProfileName() {
        db.collection("Users").document(currentUserId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.getString("userName") != null) {
                    currentUserName = documentSnapshot.getString("userName");
                }
            });
    }

    private void verifyHostFromServer() {
        db.collection("Rooms").whereEqualTo("roomName", roomName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String serverHostId = document.getString("hostId");
                        String serverHostName = document.getString("hostName"); 

                        if (serverHostId != null && serverHostId.equals(currentUserId)) {
                            isHost = true;
                            isOnSeat = true; // Host hamesha seat par hota hai
                            
                            // Host ko Seat 1 par lagao
                            if(tvSeat1 != null) {
                                tvSeat1.setText(currentUserName != null ? currentUserName : "You (Host)");
                                tvSeat1.setTextColor(android.graphics.Color.parseColor("#E91E63"));
                            }
                            
                            // Seat 2 par '+' dikhao
                            if(tvSeat2 != null) {
                                tvSeat2.setText("+ Invite");
                                tvSeat2.setTextColor(android.graphics.Color.parseColor("#2196F3"));
                            }
                        } else {
                            isHost = false;
                            isOnSeat = false;
                            
                            // Audience ke liye Host ka naam dikhao Seat 1 par
                            if(tvSeat1 != null && serverHostName != null) {
                                tvSeat1.setText(serverHostName);
                            }
                            
                            // Audience ke liye Seat 2 par 'Request' dikhao
                            if(tvSeat2 != null) {
                                tvSeat2.setText("Request");
                                tvSeat2.setTextColor(android.graphics.Color.parseColor("#FFC107"));
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        showExitDialog(); 
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Chat?");
        builder.setMessage("Do you want to minimize the chat or exit completely?");
        
        builder.setPositiveButton("Exit", (dialog, which) -> finish());
        builder.setNegativeButton("Minimize", (dialog, which) -> moveTaskToBack(true));
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }
}
