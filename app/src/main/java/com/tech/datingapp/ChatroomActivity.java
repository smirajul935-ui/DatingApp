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

    TextView tvRoomName, tvMicStatus;
    ImageView btnMic, btnSend;
    EditText etMessage;
    LinearLayout layoutMessages, hostInfo; 
    ScrollView chatScroll;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String roomName, currentUserEmail, currentUserId;
    
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
        } else {
            finish(); 
            return;
        }

        tvRoomName = findViewById(R.id.tvRoomName);
        tvMicStatus = findViewById(R.id.tvMicStatus);
        btnMic = findViewById(R.id.btnMic);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        layoutMessages = findViewById(R.id.layoutMessages);
        chatScroll = findViewById(R.id.chatScroll);
        hostInfo = findViewById(R.id.hostInfo);
        
        roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null) {
            verifyHostFromServer(); 
        } else {
            finish();
            return;
        }

        if(hostInfo != null) {
            hostInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isHost) {
                        showHostPowersDialog("Seat Controls");
                    } else {
                        Toast.makeText(ChatroomActivity.this, "Only Host can control seats!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        if(btnMic != null) {
            btnMic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isHost || isOnSeat) {
                        tvMicStatus.setText("Your mic is ON. Say hello to others! 🎙️");
                        tvMicStatus.setTextColor(android.graphics.Color.GREEN);
                        Toast.makeText(ChatroomActivity.this, "Mic is ON 🎙️", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatroomActivity.this, "❌ You are in Audience. Wait for Host to invite you!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        if(btnSend != null && etMessage != null) {
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
        }

        if(layoutMessages != null && chatScroll != null) {
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
        }
    }

    // 🚨 FIREBASE SE HOST KA NAAM NIKALNA
    private void verifyHostFromServer() {
        db.collection("Rooms").whereEqualTo("roomName", roomName).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String serverHostId = document.getString("hostId");
                            String serverHostName = document.getString("hostName"); // 🔥 Host ka naam nikala

                            // Screen par Room Name (by HostName) set kiya
                            if (tvRoomName != null) {
                                if (serverHostName != null) {
                                    tvRoomName.setText("👑 " + roomName + " (by " + serverHostName + ")");
                                } else {
                                    tvRoomName.setText("👑 " + roomName);
                                }
                            }

                            if (serverHostId != null && serverHostId.equals(currentUserId)) {
                                isHost = true;
                                Toast.makeText(ChatroomActivity.this, "Host Verified Securely ✅", Toast.LENGTH_SHORT).show();
                            } else {
                                isHost = false;
                            }
                        }
                    }
                });
    }

    private void showHostPowersDialog(String title) {
        String[] powers = {"Invite User", "Mute Seat", "Kick User", "Block User"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(powers, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: Toast.makeText(ChatroomActivity.this, "User Invited!", Toast.LENGTH_SHORT).show(); break;
                    case 1: Toast.makeText(ChatroomActivity.this, "Seat Muted 🔇", Toast.LENGTH_SHORT).show(); break;
                    case 2: Toast.makeText(ChatroomActivity.this, "User Kicked 🥾", Toast.LENGTH_SHORT).show(); break;
                    case 3: Toast.makeText(ChatroomActivity.this, "User Blocked 🚫", Toast.LENGTH_LONG).show(); break;
                }
            }
        });
        builder.show();
    }
}
