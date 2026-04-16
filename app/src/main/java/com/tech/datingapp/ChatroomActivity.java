package com.tech.datingapp;

import android.app.AlertDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChatroomActivity extends AppCompatActivity {

    TextView tvRoomName, tvMicStatus;
    ImageView btnMic, btnSend, btnClose;
    EditText etMessage;
    LinearLayout layoutMessages, hostInfo, micIndicator;
    ScrollView chatScroll;

    // 🪑 SEAT ARRAYS
    TextView[] seatTexts = new TextView[8];
    ImageView[] seatImages = new ImageView[8];
    LinearLayout[] seatLayouts = new LinearLayout[8];

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String roomName, currentUserEmail, currentUserId, currentUserName;

    // 🔥 SECURITY VARIABLES
    boolean isHost = false;
    boolean isOnSeat = false;
    int mySeatIndex = -1; // -1 matlab kisi seat par nahi hai
    boolean isMicMuted = false;

    int filledSeats = 1; // Default 1 for host

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
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
        hostInfo = findViewById(R.id.hostInfo);
        micIndicator = findViewById(R.id.micIndicator);

        seatLayouts[0] = findViewById(R.id.seat1); seatTexts[0] = findViewById(R.id.tvSeat1); seatImages[0] = seatLayouts[0].findViewById(R.id.ivHostAvatar);
        seatLayouts[1] = findViewById(R.id.seat2); seatTexts[1] = findViewById(R.id.tvSeat2); seatImages[1] = seatLayouts[1].findViewById(R.id.ivHostAvatar);
        seatLayouts[2] = findViewById(R.id.seat3); seatTexts[2] = findViewById(R.id.tvSeat3); seatImages[2] = seatLayouts[2].findViewById(R.id.ivHostAvatar);
        seatLayouts[3] = findViewById(R.id.seat4); seatTexts[3] = findViewById(R.id.tvSeat4); seatImages[3] = seatLayouts[3].findViewById(R.id.ivHostAvatar);
        seatLayouts[4] = findViewById(R.id.seat5); seatTexts[4] = findViewById(R.id.tvSeat5); seatImages[4] = seatLayouts[4].findViewById(R.id.ivHostAvatar);
        seatLayouts[5] = findViewById(R.id.seat6); seatTexts[5] = findViewById(R.id.tvSeat6); seatImages[5] = seatLayouts[5].findViewById(R.id.ivHostAvatar);
        seatLayouts[6] = findViewById(R.id.seat7); seatTexts[6] = findViewById(R.id.tvSeat7); seatImages[6] = seatLayouts[6].findViewById(R.id.ivHostAvatar);
        seatLayouts[7] = findViewById(R.id.seat8); seatTexts[7] = findViewById(R.id.tvSeat8); seatImages[7] = seatLayouts[7].findViewById(R.id.ivHostAvatar);

        roomName = getIntent().getStringExtra("ROOM_NAME");
        if (roomName != null) {
            if (tvRoomName != null) tvRoomName.setText("👑 " + roomName);
            verifyHostFromServer();
        } else {
            finish();
            return;
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> showExitDialog());

        // --- MIC CLICK LOGIC ---
        if (btnMic != null) {
            btnMic.setOnClickListener(v -> {
                if (isOnSeat) {
                    toggleMic();
                }
            });
        }

        if (btnSend != null && etMessage != null) {
            btnSend.setOnClickListener(v -> {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
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

        if (layoutMessages != null && chatScroll != null) {
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

        // --- SEAT CLICK LOGIC ---
        for (int i = 0; i < 8; i++) {
            final int seatIndex = i;
            if (seatLayouts[i] != null) {
                seatLayouts[i].setOnClickListener(v -> handleSeatClick(seatIndex));
            }
        }
    }

    private void fetchMyProfileInfo() {
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getString("userName") != null) {
                        currentUserName = documentSnapshot.getString("userName");
                    }
                });
    }

    // 🚨 FIREBASE SE HOST VERIFY KAREGA AUR MIC GAYAB KAREGA
    private void verifyHostFromServer() {
        db.collection("Rooms").whereEqualTo("roomName", roomName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String serverHostId = document.getString("hostId");

                        if (serverHostId != null && serverHostId.equals(currentUserId)) {
                            // MAIN HOST HU
                            isHost = true;
                            isOnSeat = true;
                            mySeatIndex = 0; // Host ki seat 0 hoti hai
                            updateMicVisibility(); // Mic On dikhayega
                            updateSeatsDisplay();
                            Toast.makeText(ChatroomActivity.this, "Host Verified Securely ✅", Toast.LENGTH_SHORT).show();
                        } else {
                            // MAIN AUDIENCE HU
                            isHost = false;
                            isOnSeat = false;
                            mySeatIndex = -1;
                            updateMicVisibility(); // Mic Gayab kar dega
                            updateSeatsDisplay();
                        }
                    }
                });
    }

    // 🔥 DYNAMIC MIC VISIBILITY & AUTO-ON
    private void updateMicVisibility() {
        if (isOnSeat) {
            // Seat par baithe ho toh Mic dikhega aur automatically ON hoga
            if (micIndicator != null) micIndicator.setVisibility(View.VISIBLE);
            if (btnMic != null) btnMic.setVisibility(View.VISIBLE);
            isMicMuted = false; // Auto ON
            if (tvMicStatus != null) {
                tvMicStatus.setText("Your mic is ON. Say hello to others! 🎙️");
                tvMicStatus.setTextColor(android.graphics.Color.GREEN);
            }
            if (btnMic != null) btnMic.setColorFilter(android.graphics.Color.GREEN);
        } else {
            // Audience me ho toh Mic puri tarah gayab
            if (micIndicator != null) micIndicator.setVisibility(View.GONE);
            if (btnMic != null) btnMic.setVisibility(View.GONE);
        }
    }

    // MIC MUTE / UNMUTE LOGIC
    private void toggleMic() {
        isMicMuted = !isMicMuted;
        if (isMicMuted) {
            tvMicStatus.setText("Your mic is Muted 🔇");
            tvMicStatus.setTextColor(android.graphics.Color.RED);
            btnMic.setColorFilter(android.graphics.Color.RED);
        } else {
            tvMicStatus.setText("Your mic is ON. Say hello to others! 🎙️");
            tvMicStatus.setTextColor(android.graphics.Color.GREEN);
            btnMic.setColorFilter(android.graphics.Color.GREEN);
        }
    }

    // 🔥 HANDLE SEAT CLICKS (Self Control & Host Powers)
    private void handleSeatClick(int seatIndex) {
        // 1. Agar user khud ki seat par click kare
        if (isOnSeat && seatIndex == mySeatIndex) {
            showSelfSeatControls();
            return;
        }

        // 2. Agar Host dusre ki seat par click kare
        if (isHost) {
            if (seatIndex < filledSeats && seatIndex != 0) {
                showHostPowersDialog("Seat " + (seatIndex + 1)); // User baitha hai (Mute/Kick)
            } else if (seatIndex == filledSeats) {
                Toast.makeText(ChatroomActivity.this, "Inviting User...", Toast.LENGTH_SHORT).show();
                addDummyUserToSeat(); // Host ne '+' click kiya
            }
        } else {
            // 3. Agar Audience kisi aur ki seat par click kare
            if (seatIndex == filledSeats) {
                Toast.makeText(ChatroomActivity.this, "Request Sent to Host!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChatroomActivity.this, "You can't control this seat.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔥 SELF SEAT CONTROLS (Khud ki seat)
    private void showSelfSeatControls() {
        String micOption = isMicMuted ? "Unmute Mic 🎙️" : "Mute Mic 🔇";
        String[] options = {micOption, "Leave Seat ⬇️"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isHost ? "Host Controls" : "My Seat Controls");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                toggleMic();
            } else if (which == 1) {
                if (isHost) {
                    Toast.makeText(ChatroomActivity.this, "Host cannot leave seat! Exit room instead.", Toast.LENGTH_LONG).show();
                } else {
                    // Leave seat logic
                    isOnSeat = false;
                    mySeatIndex = -1;
                    updateMicVisibility(); // Mic gayab
                    updateSeatsDisplay();
                    Toast.makeText(ChatroomActivity.this, "You left the seat.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    // HOST POWER DIALOG
    private void showHostPowersDialog(String title) {
        String[] powers = {"Mute User 🔇", "Kick User 🥾", "Block User 🚫"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(powers, (dialog, which) -> {
            Toast.makeText(ChatroomActivity.this, "Host Power Applied!", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    // SEATS DISPLAY LOGIC
    private void updateSeatsDisplay() {
        for(int i = 0; i < 8; i++) {
            if(seatTexts[i] == null || seatImages[i] == null) continue;

            if (i < filledSeats) {
                if (i == 0) { // Host
                    seatTexts[i].setText(isHost ? (currentUserName != null ? currentUserName : "Host") : "Host");
                    seatTexts[i].setTextColor(android.graphics.Color.parseColor("#E91E63"));
                    seatImages[i].setImageResource(android.R.drawable.sym_def_app_icon);
                } else { // Normal User
                    seatTexts[i].setText("User");
                    seatTexts[i].setTextColor(android.graphics.Color.WHITE);
                    seatImages[i].setImageResource(android.R.drawable.sym_def_app_icon);
                }
                seatImages[i].setColorFilter(android.graphics.Color.WHITE);
            } else if (i == filledSeats) { // '+' icon
                seatTexts[i].setText(isHost ? "+ Invite" : "Request");
                seatTexts[i].setTextColor(isHost ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#FFC107"));
                seatImages[i].setImageResource(android.R.drawable.ic_menu_add);
                seatImages[i].setColorFilter(isHost ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#FFC107"));
            } else { // Empty Locks
                seatTexts[i].setText("Empty");
                seatTexts[i].setTextColor(android.graphics.Color.parseColor("#888888"));
                seatImages[i].setImageResource(android.R.drawable.ic_secure);
                seatImages[i].setColorFilter(android.graphics.Color.parseColor("#888888"));
            }
        }
    }

    private void addDummyUserToSeat() {
        if(filledSeats < 8) {
            filledSeats++;
            updateSeatsDisplay();
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog(); 
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Chat?");
        builder.setMessage("Do you want to minimize or exit?");
        builder.setPositiveButton("Exit", (dialog, which) -> finish());
        builder.setNegativeButton("Minimize", (dialog, which) -> moveTaskToBack(true));
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }
}
