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
    LinearLayout layoutMessages; 
    ScrollView chatScroll;

    // 🪑 SEAT ARRAYS (Taki easily manage ho sake)
    TextView[] seatTexts = new TextView[8];
    ImageView[] seatImages = new ImageView[8];
    LinearLayout[] seatLayouts = new LinearLayout[8];

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String roomName, currentUserEmail, currentUserId, currentUserName, currentUserAvatar;
    
    // 🔥 SECURITY VARIABLES
    boolean isHost = false; 
    boolean isOnSeat = false; 

    // Kitni seats bhari hain (Starting me sirf Host baitha hai)
    int filledSeats = 1; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if(mAuth.getCurrentUser() != null) {
            currentUserEmail = mAuth.getCurrentUser().getEmail();
            currentUserId = mAuth.getCurrentUser().getUid();
            fetchMyProfileInfo(); 
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
        
        // 🪑 Link all 8 Seats
        seatLayouts[0] = findViewById(R.id.seat1); seatTexts[0] = findViewById(R.id.tvSeat1); seatImages[0] = seatLayouts[0].findViewById(R.id.ivHostAvatar);
        seatLayouts[1] = findViewById(R.id.seat2); seatTexts[1] = findViewById(R.id.tvSeat2); seatImages[1] = seatLayouts[1].findViewById(R.id.ivHostAvatar);
        seatLayouts[2] = findViewById(R.id.seat3); seatTexts[2] = findViewById(R.id.tvSeat3); seatImages[2] = seatLayouts[2].findViewById(R.id.ivHostAvatar);
        seatLayouts[3] = findViewById(R.id.seat4); seatTexts[3] = findViewById(R.id.tvSeat4); seatImages[3] = seatLayouts[3].findViewById(R.id.ivHostAvatar);
        seatLayouts[4] = findViewById(R.id.seat5); seatTexts[4] = findViewById(R.id.tvSeat5); seatImages[4] = seatLayouts[4].findViewById(R.id.ivHostAvatar);
        seatLayouts[5] = findViewById(R.id.seat6); seatTexts[5] = findViewById(R.id.tvSeat6); seatImages[5] = seatLayouts[5].findViewById(R.id.ivHostAvatar);
        seatLayouts[6] = findViewById(R.id.seat7); seatTexts[6] = findViewById(R.id.tvSeat7); seatImages[6] = seatLayouts[6].findViewById(R.id.ivHostAvatar);
        seatLayouts[7] = findViewById(R.id.seat8); seatTexts[7] = findViewById(R.id.tvSeat8); seatImages[7] = seatLayouts[7].findViewById(R.id.ivHostAvatar);
        
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

        // --- SEND MESSAGES ---
        if(btnSend != null && etMessage != null) {
            btnSend.setOnClickListener(v -> {
                String message = etMessage.getText().toString().trim();
                if(!message.isEmpty()){
                    etMessage.setText(""); 
                    Map<String, Object> chatData = new HashMap<>();
                    String nameToUse = (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : currentUserEmail.split("@")[0];
                    chatData.put("senderName", nameToUse);
                    chatData.put("message", message);
                    chatData.put("timestamp", FieldValue.serverTimestamp());
                    db.collection("Chatrooms").document(roomName).collection("Messages").add(chatData);
                }
            });
        }

        // --- RECEIVE MESSAGES ---
        if(layoutMessages != null && chatScroll != null) {
            db.collection("Chatrooms").document(roomName).collection("Messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null || value == null) return;
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String msgText = dc.getDocument().getString("message");
                                String senderName = dc.getDocument().getString("senderName"); 
                                
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

        // 🔥 DUMMY SEAT CLICK LOGIC (Testing shifting "+")
        // Abhi hum test karne ke liye "+" dabane par seat bhar rahe hain.
        // Asli app me yeh Request aur Host Approve ke baad hoga (Firebase ke through).
        for(int i = 1; i < 8; i++) {
            final int seatIndex = i;
            if(seatLayouts[i] != null) {
                seatLayouts[i].setOnClickListener(v -> {
                    if(seatIndex == filledSeats) {
                        if(isHost) {
                            // Host ne "+" pe click kiya (Invite karne ke liye)
                            Toast.makeText(ChatroomActivity.this, "Host Inviting someone...", Toast.LENGTH_SHORT).show();
                            addDummyUserToSeat(seatIndex); // Test function
                        } else {
                            // Audience ne "+" pe click kiya (Request bhej rahi hai)
                            Toast.makeText(ChatroomActivity.this, "Seat Request Sent to Host!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (seatIndex < filledSeats) {
                         // Jo seat bhar chuki hai uspar click karna
                         if(isHost) showHostPowersDialog("Seat " + (seatIndex + 1));
                    }
                });
            }
        }
    }

    private void fetchMyProfileInfo() {
        db.collection("Users").document(currentUserId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUserName = documentSnapshot.getString("userName");
                    currentUserAvatar = documentSnapshot.getString("avatarUrl");
                    // Abhi ke liye hum default DP ki jagah Android ka icon lagaenge jiska naam aayega.
                    // Asli DP load karne ke liye Glide use hoga Firebase seats update me.
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
                            isOnSeat = true; 
                            updateSeatsDisplay(); // Seats ko naye logic se draw karo
                        } else {
                            isHost = false;
                            isOnSeat = false;
                            
                            // Audience ke liye Host ka naam dikhao Seat 1 par
                            if(seatTexts[0] != null && serverHostName != null) {
                                seatTexts[0].setText(serverHostName);
                            }
                            updateSeatsDisplay();
                        }
                    }
                });
    }

    // 🔥 DYNAMIC SHIFTING LOGIC FUNCTION
    private void updateSeatsDisplay() {
        for(int i = 0; i < 8; i++) {
            if(seatTexts[i] == null || seatImages[i] == null) continue;

            if (i < filledSeats) {
                // Seat Bhari Hui Hai (Host ya User)
                if (i == 0) {
                    // Host (Seat 1)
                    seatTexts[i].setText(isHost ? (currentUserName != null ? currentUserName : "Host") : seatTexts[0].getText());
                    seatTexts[i].setTextColor(android.graphics.Color.parseColor("#E91E63"));
                    seatImages[i].setImageResource(android.R.drawable.sym_def_app_icon); // Dummy Avatar DP
                    seatImages[i].setColorFilter(android.graphics.Color.WHITE);
                } else {
                    // Koi normal user seat par baitha hai
                    seatTexts[i].setText("User " + i);
                    seatTexts[i].setTextColor(android.graphics.Color.WHITE);
                    seatImages[i].setImageResource(android.R.drawable.sym_def_app_icon); // Dummy Avatar DP
                    seatImages[i].setColorFilter(android.graphics.Color.WHITE);
                }
            } else if (i == filledSeats) {
                // Yaha par "+" dikhana hai
                seatTexts[i].setText(isHost ? "+ Invite" : "Request");
                seatTexts[i].setTextColor(isHost ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#FFC107"));
                seatImages[i].setImageResource(android.R.drawable.ic_menu_add);
                seatImages[i].setColorFilter(isHost ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#FFC107"));
            } else {
                // Baki aage ki seats khali aur Locked dikhengi
                seatTexts[i].setText("Empty");
                seatTexts[i].setTextColor(android.graphics.Color.parseColor("#888888"));
                seatImages[i].setImageResource(android.R.drawable.ic_secure); // Lock Photo
                seatImages[i].setColorFilter(android.graphics.Color.parseColor("#888888"));
            }
        }
    }

    // 🚨 DUMMY FUNCTION (Taki tum test kar sako ki "+" shift ho raha hai)
    private void addDummyUserToSeat(int seatIndex) {
        if(filledSeats < 8) {
            filledSeats++; // Ek seat bhar gayi
            updateSeatsDisplay(); // Wapas screen ko draw karo shifting ke sath
        }
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

    private void showHostPowersDialog(String title) {
        String[] powers = {"Mute Seat 🔇", "Kick User 🥾", "Block User 🚫"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(powers, (dialog, which) -> {
            // Yaha aage Firebase se user remove karne ka code aayega
            Toast.makeText(ChatroomActivity.this, "Host Power Used!", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
}
