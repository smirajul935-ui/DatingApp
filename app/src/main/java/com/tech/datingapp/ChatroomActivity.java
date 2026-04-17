package com.tech.datingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import android.graphics.drawable.Drawable;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    String myAvatarUrl = "https://raw.githubusercontent.com/smirajul935/DatingApp/main/avatar.png"; 
    
    // 🔥 SECURITY VARIABLES
    boolean isHost = false; 
    boolean isOnSeat = false; 
    int mySeatIndex = -1;
    boolean isMicMuted = false;

    // Seat shifting logic
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
        
        seatLayouts[0] = findViewById(R.id.seat1); seatTexts[0] = findViewById(R.id.tvSeat1); seatImages[0] = findViewById(R.id.ivSeat1);
        seatLayouts[1] = findViewById(R.id.seat2); seatTexts[1] = findViewById(R.id.tvSeat2); seatImages[1] = findViewById(R.id.ivSeat2);
        seatLayouts[2] = findViewById(R.id.seat3); seatTexts[2] = findViewById(R.id.tvSeat3); seatImages[2] = findViewById(R.id.ivSeat3);
        seatLayouts[3] = findViewById(R.id.seat4); seatTexts[3] = findViewById(R.id.tvSeat4); seatImages[3] = findViewById(R.id.ivSeat4);
        seatLayouts[4] = findViewById(R.id.seat5); seatTexts[4] = findViewById(R.id.tvSeat5); seatImages[4] = findViewById(R.id.ivSeat5);
        seatLayouts[5] = findViewById(R.id.seat6); seatTexts[5] = findViewById(R.id.tvSeat6); seatImages[5] = findViewById(R.id.ivSeat6);
        seatLayouts[6] = findViewById(R.id.seat7); seatTexts[6] = findViewById(R.id.tvSeat7); seatImages[6] = findViewById(R.id.ivSeat7);
        seatLayouts[7] = findViewById(R.id.seat8); seatTexts[7] = findViewById(R.id.tvSeat8); seatImages[7] = findViewById(R.id.ivSeat8);
        
        roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null && !roomName.isEmpty()) {
            if(tvRoomName != null) tvRoomName.setText("👑 " + roomName);
            fetchMyProfileInfo(); 
        } else {
            finish();
            return;
        }

        try {
            final View mainView = getWindow().getDecorView().getRootView();
            String githubImageUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853"; 
            Glide.with(this).load(githubImageUrl).centerCrop().into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    if (mainView != null) mainView.setBackground(resource);
                }
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        } catch (Exception e) {}

        if(btnClose != null) btnClose.setOnClickListener(v -> showExitDialog());

        if(btnMic != null) {
            btnMic.setOnClickListener(v -> {
                if (isHost || isOnSeat) {
                    toggleMic();
                } else {
                    Toast.makeText(ChatroomActivity.this, "❌ You are in Audience. Wait for Host to invite you!", Toast.LENGTH_LONG).show();
                }
            });
        }

        if(btnSend != null && etMessage != null) {
            btnSend.setOnClickListener(v -> {
                String message = etMessage.getText().toString().trim();
                if(!message.isEmpty()){
                    etMessage.setText(""); 
                    Map<String, Object> chatData = new HashMap<>();
                    String nameToUse = (currentUserName != null && !currentUserName.isEmpty()) ? currentUserName : currentUserEmail.split("@")[0];
                    chatData.put("senderName", nameToUse);
                    chatData.put("message", message);
                    chatData.put("avatarUrl", myAvatarUrl); 
                    chatData.put("timestamp", FieldValue.serverTimestamp());
                    db.collection("Chatrooms").document(roomName).collection("Messages").add(chatData);
                }
            });
        }

        if(layoutMessages != null && chatScroll != null && roomName != null) {
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
                if (documentSnapshot.exists()) {
                    currentUserName = documentSnapshot.getString("userName");
                    if (documentSnapshot.getString("avatarUrl") != null) {
                        myAvatarUrl = documentSnapshot.getString("avatarUrl");
                    }
                }
                verifyHostFromServer(); 
            });
    }

    private void verifyHostFromServer() {
        if(roomName == null || roomName.isEmpty()) return;
        db.collection("Rooms").whereEqualTo("roomName", roomName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String serverHostId = document.getString("hostId");

                        if (serverHostId != null && serverHostId.equals(currentUserId)) {
                            isHost = true;
                            isOnSeat = true;
                            mySeatIndex = 0; 
                            updateMicVisibility(); 
                            updateSeatsDisplay();
                        } else {
                            isHost = false;
                            isOnSeat = false;
                            mySeatIndex = -1;
                            updateMicVisibility(); 
                            updateSeatsDisplay();
                        }
                    }
                });
    }

    private void updateMicVisibility() {
        if (isOnSeat) {
            if (micIndicator != null) micIndicator.setVisibility(View.VISIBLE);
            if (btnMic != null) btnMic.setVisibility(View.VISIBLE);
            isMicMuted = false; 
            if (tvMicStatus != null) {
                tvMicStatus.setText("Your mic is ON. Say hello to others! 🎙️");
                tvMicStatus.setTextColor(android.graphics.Color.GREEN);
            }
            if (btnMic != null) btnMic.setColorFilter(android.graphics.Color.GREEN);
        } else {
            if (micIndicator != null) micIndicator.setVisibility(View.GONE);
            if (btnMic != null) btnMic.setVisibility(View.GONE);
        }
    }

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

    private void updateSeatsDisplay() {
        for(int i = 0; i < 8; i++) {
            if(seatTexts[i] == null || seatImages[i] == null) continue;

            if (i < filledSeats) {
                if (i == 0) { 
                    seatTexts[i].setText(isHost ? (currentUserName != null ? currentUserName : "Host") : "Host");
                    seatTexts[i].setTextColor(android.graphics.Color.parseColor("#E91E63"));
                } else { 
                    seatTexts[i].setText("User " + i);
                    seatTexts[i].setTextColor(android.graphics.Color.WHITE);
                }
                
                seatImages[i].clearColorFilter(); 
                try {
                    Glide.with(this).load(myAvatarUrl).circleCrop().into(seatImages[i]);
                } catch (Exception e) {
                    seatImages[i].setImageResource(android.R.drawable.sym_def_app_icon);
                }

            } else if (i == filledSeats) { 
                seatTexts[i].setText(isHost ? "+ Invite" : "Request");
                seatTexts[i].setTextColor(isHost ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#FFC107"));
                seatImages[i].setImageResource(android.R.drawable.ic_menu_add);
                seatImages[i].setColorFilter(isHost ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#FFC107"));
                
            } else { 
                seatTexts[i].setText("Empty");
                seatTexts[i].setTextColor(android.graphics.Color.parseColor("#888888"));
                seatImages[i].setImageResource(android.R.drawable.ic_secure); 
                seatImages[i].setColorFilter(android.graphics.Color.parseColor("#888888"));
            }
        }
    }

    private void handleSeatClick(int seatIndex) {
        if (isOnSeat && seatIndex == mySeatIndex) {
            showSelfSeatControls();
            return;
        }
        if (isHost) {
            if (seatIndex < filledSeats && seatIndex != 0) {
                showHostPowersDialog("Seat " + (seatIndex + 1)); 
            } else if (seatIndex == filledSeats) {
                // 🔥 NAYA FEATURE: PENDING REQUESTS LIST DIKHEGI
                showPendingRequestsDialog(); 
            }
        } else {
            if (seatIndex == filledSeats) {
                // 🔥 AUDIENCE NE REQUEST BHEJI
                Toast.makeText(ChatroomActivity.this, "Request Sent to Host! Please wait for approval.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChatroomActivity.this, "You can't control this seat.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🚨 🔥 NAYA FEATURE: HOST REQUEST POPUP
    private void showPendingRequestsDialog() {
        // Abhi hum dummy users ka naam de rahe hain test karne ke liye.
        // Aage chal kar ye list Firebase se aayegi un logo ki jinhone request bheji hai.
        List<String> requests = new ArrayList<>();
        requests.add("Emma (Pending Request)");
        requests.add("Rahul (Pending Request)");
        requests.add("Priya (Pending Request)");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, requests);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select user to add on seat:");
        
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedUser = requests.get(which);
                Toast.makeText(ChatroomActivity.this, selectedUser + " added to the seat!", Toast.LENGTH_SHORT).show();
                addDummyUserToSeat(); // Us user ko seat par add kar dega
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addDummyUserToSeat() {
        if(filledSeats < 8) {
            filledSeats++; 
            updateSeatsDisplay();
        }
    }

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
                    isOnSeat = false;
                    mySeatIndex = -1;
                    updateMicVisibility(); 
                    updateSeatsDisplay();
                }
            }
        });
        builder.show();
    }

    private void showHostPowersDialog(String title) {
        String[] powers = {"Mute User 🔇", "Kick User 🥾", "Block User 🚫"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(powers, (dialog, which) -> {
            Toast.makeText(ChatroomActivity.this, "Host Power Applied!", Toast.LENGTH_SHORT).show();
        });
        builder.show();
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
