package com.tech.datingapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

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
import com.google.firebase.firestore.SetOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class ChatroomActivity extends AppCompatActivity {

    TextView tvRoomName, tvMicStatus;
    ImageView btnMic, btnSend, btnClose, btnShare;
    EditText etMessage;
    LinearLayout layoutMessages, hostInfo, micIndicator;
    ScrollView chatScroll;
    RelativeLayout mainLayout;

    // 🪑 SEAT ARRAYS
    RelativeLayout[] seatLayouts = new RelativeLayout[8];
    TextView[] seatTexts = new TextView[8];
    ImageView[] seatImages = new ImageView[8];
    ImageView[] seatMuteIcons = new ImageView[8];

    // 🔥 LIVE SEAT DATA (From Firebase)
    String[] seatUserIds = new String[8];
    String[] seatUserNames = new String[8];
    String[] seatAvatarUrls = new String[8];
    boolean[] seatIsMuted = new boolean[8];

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String roomName, currentUserEmail, currentUserId, currentUserName;
    String myAvatarUrl = "https://raw.githubusercontent.com/smirajul935-ui/Datingappdp/main/dp1.png"; 
    
    boolean isHost = false; 
    boolean isOnSeat = false; 
    int mySeatIndex = -1;
    boolean isMicMuted = false;

    private RtcEngine mRtcEngine;
    private int agoraUid = 0; 
    private String SERVER_URL = "https://datingserver-ymcg.onrender.com/api/agora-token";
    private String AGORA_APP_ID = "YOUR_AGORA_APP_ID_HERE"; // TUMHARI APP ID DAALNA

    List<String> pendingRequestsIds = new ArrayList<>();
    List<String> pendingRequestsNames = new ArrayList<>();
    List<String> pendingRequestsAvatars = new ArrayList<>(); 

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() -> Toast.makeText(ChatroomActivity.this, "Voice Connected! ⚡", Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 22);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if(mAuth.getCurrentUser() != null) {
            currentUserEmail = mAuth.getCurrentUser().getEmail();
            currentUserId = mAuth.getCurrentUser().getUid();
            agoraUid = Math.abs(currentUserId.hashCode()) % 10000;
        } else {
            finish(); 
            return;
        }

        // Initialize Arrays
        for(int i=0; i<8; i++) {
            seatUserIds[i] = "";
            seatUserNames[i] = "Empty";
            seatAvatarUrls[i] = "";
            seatIsMuted[i] = false;
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
        
        try { btnShare = findViewById(R.id.btnShare); } catch (Exception e) {}
        
        seatLayouts[0] = findViewById(R.id.seat1); seatTexts[0] = findViewById(R.id.tvSeat1); seatImages[0] = findViewById(R.id.ivSeat1); seatMuteIcons[0] = findViewById(R.id.muteIcon1);
        seatLayouts[1] = findViewById(R.id.seat2); seatTexts[1] = findViewById(R.id.tvSeat2); seatImages[1] = findViewById(R.id.ivSeat2); seatMuteIcons[1] = findViewById(R.id.muteIcon2);
        seatLayouts[2] = findViewById(R.id.seat3); seatTexts[2] = findViewById(R.id.tvSeat3); seatImages[2] = findViewById(R.id.ivSeat3); seatMuteIcons[2] = findViewById(R.id.muteIcon3);
        seatLayouts[3] = findViewById(R.id.seat4); seatTexts[3] = findViewById(R.id.tvSeat4); seatImages[3] = findViewById(R.id.ivSeat4); seatMuteIcons[3] = findViewById(R.id.muteIcon4);
        seatLayouts[4] = findViewById(R.id.seat5); seatTexts[4] = findViewById(R.id.tvSeat5); seatImages[4] = findViewById(R.id.ivSeat5); seatMuteIcons[4] = findViewById(R.id.muteIcon5);
        seatLayouts[5] = findViewById(R.id.seat6); seatTexts[5] = findViewById(R.id.tvSeat6); seatImages[5] = findViewById(R.id.ivSeat6); seatMuteIcons[5] = findViewById(R.id.muteIcon6);
        seatLayouts[6] = findViewById(R.id.seat7); seatTexts[6] = findViewById(R.id.tvSeat7); seatImages[6] = findViewById(R.id.ivSeat7); seatMuteIcons[6] = findViewById(R.id.muteIcon7);
        seatLayouts[7] = findViewById(R.id.seat8); seatTexts[7] = findViewById(R.id.tvSeat8); seatImages[7] = findViewById(R.id.ivSeat8); seatMuteIcons[7] = findViewById(R.id.muteIcon8);

        roomName = getIntent().getStringExtra("ROOM_NAME");
        if(roomName != null && !roomName.isEmpty()) {
            if(tvRoomName != null) tvRoomName.setText("👑 " + roomName);
            // 🚨 Pehle apna data load karo, fir Room setup karo
            fetchMyProfileInfo(); 
            listenForSeatRequests(); 
            listenToLiveSeats(); 
        } else {
            finish();
            return;
        }

        try {
            mainLayout = getWindow().getDecorView().getRootView().findViewById(android.R.id.content);
            Glide.with(this).load("https://images.unsplash.com/photo-1550684848-fac1c5b4e853").centerCrop().into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    if (mainLayout != null) mainLayout.setBackground(resource);
                }
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        } catch (Exception e) {}

        if(btnClose != null) btnClose.setOnClickListener(v -> showExitDialog());
        if(btnShare != null) btnShare.setOnClickListener(v -> shareChatroomLink());

        if(btnMic != null) {
            btnMic.setOnClickListener(v -> {
                if (isHost || isOnSeat) toggleMic();
                else Toast.makeText(ChatroomActivity.this, "❌ You are in Audience. Wait for Host to invite you!", Toast.LENGTH_LONG).show();
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

    // 🔥 PROFILE INFO FETCH AUR USKE BAAD HOST VERIFICATION
    private void fetchMyProfileInfo() {
        db.collection("Users").document(currentUserId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.getString("userName") != null) currentUserName = documentSnapshot.getString("userName");
                    if (documentSnapshot.getString("avatarUrl") != null) myAvatarUrl = documentSnapshot.getString("avatarUrl");
                }
                verifyHostFromServer(); 
            });
    }

    private void verifyHostFromServer() {
        db.collection("Rooms").whereEqualTo("roomName", roomName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String serverHostId = document.getString("hostId");

                        if (serverHostId != null && serverHostId.equals(currentUserId)) {
                            isHost = true; 
                            
                            // 🔥 HOST KO SEAT 1 PAR BITHAO AUR USKI DP FIREBASE ME DAALO
                            Map<String, Object> hostSeat = new HashMap<>();
                            hostSeat.put("userId", currentUserId);
                            hostSeat.put("userName", currentUserName != null ? currentUserName : "Host");
                            hostSeat.put("avatarUrl", myAvatarUrl); // Asli DP!
                            hostSeat.put("isMuted", false);
                            db.collection("Rooms").document(roomName).collection("Seats").document("0").set(hostSeat);
                        } else {
                            isHost = false; 
                        }
                        initializeAndJoinAgora();
                    }
                });
    }

    private void initializeAndJoinAgora() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = AGORA_APP_ID;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);
            
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            fetchSecureAgoraToken(roomName, agoraUid);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fetchSecureAgoraToken(String channelName, int uid) {
        String finalUrl = SERVER_URL + "?channelName=" + channelName + "&uid=" + uid;
        StringRequest request = new StringRequest(Request.Method.GET, finalUrl,
            response -> {
                try {
                    JSONObject obj = new JSONObject(response);
                    String secureToken = obj.getString("token");
                    if (mRtcEngine != null) mRtcEngine.joinChannel(secureToken, channelName, "", uid);
                } catch (Exception e) {}
            }, error -> Toast.makeText(ChatroomActivity.this, "Voice Server Slow!", Toast.LENGTH_SHORT).show());
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

    private void shareChatroomLink() {
        String appLink = "https://securedating.app/join?room=" + roomName.replace(" ", "%20");
        String shareMessage = "Hey! Join my live chatroom *" + roomName + "* on Secure Dating App.\n" + appLink;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share Room via"));
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
            if (mRtcEngine != null) {
                mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
                mRtcEngine.enableLocalAudio(true); 
            }
        } else {
            if (micIndicator != null) micIndicator.setVisibility(View.GONE);
            if (btnMic != null) btnMic.setVisibility(View.GONE);
            if (mRtcEngine != null) {
                mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                mRtcEngine.enableLocalAudio(false); 
            }
        }
    }

    private void toggleMic() {
        isMicMuted = !isMicMuted;
        
        if(isOnSeat && mySeatIndex != -1) {
            db.collection("Rooms").document(roomName).collection("Seats").document(String.valueOf(mySeatIndex))
              .update("isMuted", isMicMuted);
        }
        
        if (isMicMuted) {
            if(tvMicStatus != null) { tvMicStatus.setText("Your mic is Muted 🔇"); tvMicStatus.setTextColor(android.graphics.Color.RED); }
            if(btnMic != null) btnMic.setColorFilter(android.graphics.Color.RED);
            if (mRtcEngine != null) mRtcEngine.muteLocalAudioStream(true); 
        } else {
            if(tvMicStatus != null) { tvMicStatus.setText("Your mic is ON. Say hello to others! 🎙️"); tvMicStatus.setTextColor(android.graphics.Color.GREEN); }
            if(btnMic != null) btnMic.setColorFilter(android.graphics.Color.GREEN);
            if (mRtcEngine != null) mRtcEngine.muteLocalAudioStream(false); 
        }
    }

    // 🚨 FIREBASE SE SEATS AUR DP UPDATE KARNA
    private void listenToLiveSeats() {
        db.collection("Rooms").document(roomName).collection("Seats")
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null) return;
                
                for(int i=0; i<8; i++) {
                    seatUserIds[i] = "";
                    seatUserNames[i] = "Empty";
                    seatAvatarUrls[i] = "";
                    seatIsMuted[i] = false;
                }
                
                isOnSeat = false;
                mySeatIndex = -1;

                for (DocumentSnapshot doc : value.getDocuments()) {
                    int seatIndex = Integer.parseInt(doc.getId()); 
                    if(seatIndex < 8) {
                        seatUserIds[seatIndex] = doc.getString("userId");
                        seatUserNames[seatIndex] = doc.getString("userName");
                        seatAvatarUrls[seatIndex] = doc.getString("avatarUrl"); // 🔥 DP Url Aaya
                        if (doc.getBoolean("isMuted") != null) seatIsMuted[seatIndex] = doc.getBoolean("isMuted");
                        
                        if(currentUserId.equals(seatUserIds[seatIndex])) {
                            isOnSeat = true;
                            mySeatIndex = seatIndex;
                        }
                    }
                }
                updateMicVisibility(); 
                updateSeatsDisplay(); 
            });
    }

    // 🔥 SCREEN PAR DP AUR NAMES SET KARNA
    private void updateSeatsDisplay() {
        int nextAvailableSeat = 0;
        while (nextAvailableSeat < 8 && !seatUserIds[nextAvailableSeat].isEmpty()) {
            nextAvailableSeat++;
        }

        for(int i = 0; i < 8; i++) {
            if(seatTexts[i] == null || seatImages[i] == null) continue;

            if (!seatUserIds[i].isEmpty()) {
                // SEAT BHARI HAI
                seatTexts[i].setText(seatUserNames[i]);
                if(i == 0) seatTexts[i].setTextColor(android.graphics.Color.parseColor("#E91E63"));
                else seatTexts[i].setTextColor(android.graphics.Color.WHITE); 
                
                seatImages[i].clearColorFilter(); 
                
                // 🚨 GLIDE SE DP LOAD HOGI
                try {
                    String dpToLoad = seatAvatarUrls[i];
                    if (dpToLoad == null || dpToLoad.isEmpty()) dpToLoad = "https://raw.githubusercontent.com/smirajul935-ui/Datingappdp/main/dp1.png";
                    
                    Glide.with(ChatroomActivity.this)
                         .load(dpToLoad)
                         .diskCacheStrategy(DiskCacheStrategy.NONE)
                         .skipMemoryCache(true)
                         .placeholder(android.R.drawable.sym_def_app_icon)
                         .circleCrop()
                         .into(seatImages[i]);
                } catch (Exception e) {}
                
                if(seatIsMuted[i]) seatMuteIcons[i].setVisibility(View.VISIBLE);
                else seatMuteIcons[i].setVisibility(View.GONE);

            } else if (i == nextAvailableSeat) { 
                // AGLI KHALI SEAT (+)
                seatTexts[i].setText(isHost ? "+ Invite" : "Request");
                seatTexts[i].setTextColor(isHost ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#FFC107"));
                seatImages[i].setImageResource(android.R.drawable.ic_menu_add);
                seatImages[i].setColorFilter(isHost ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#FFC107"));
                seatMuteIcons[i].setVisibility(View.GONE);
            } else { 
                // LOCKED SEATS
                seatTexts[i].setText("Empty");
                seatTexts[i].setTextColor(android.graphics.Color.parseColor("#888888"));
                seatImages[i].setImageResource(android.R.drawable.ic_secure); 
                seatImages[i].setColorFilter(android.graphics.Color.parseColor("#888888"));
                seatMuteIcons[i].setVisibility(View.GONE);
            }
        }
    }

    private void handleSeatClick(int seatIndex) {
        if (isOnSeat && seatIndex == mySeatIndex) {
            showSelfSeatControls();
            return;
        }
        
        if (isHost) {
            if (!seatUserIds[seatIndex].isEmpty() && seatIndex != 0) {
                showHostPowersDialog(seatIndex); 
            } else if (seatTexts[seatIndex].getText().toString().contains("Invite")) {
                showPendingRequestsDialog(seatIndex); 
            }
        } else {
            if (seatTexts[seatIndex].getText().toString().contains("Request")) {
                sendSeatRequestToHost(); 
            } else {
                Toast.makeText(ChatroomActivity.this, "You can't control this seat.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSeatRequestToHost() {
        Map<String, Object> reqData = new HashMap<>();
        reqData.put("userId", currentUserId);
        reqData.put("userName", currentUserName != null ? currentUserName : "Guest");
        reqData.put("avatarUrl", myAvatarUrl); 
        reqData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Rooms").document(roomName).collection("Requests").document(currentUserId)
                .set(reqData)
                .addOnSuccessListener(aVoid -> Toast.makeText(ChatroomActivity.this, "Request Sent to Host! Please wait.", Toast.LENGTH_LONG).show());
    }

    private void listenForSeatRequests() {
        if (!isHost) return; 

        db.collection("Rooms").document(roomName).collection("Requests")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    pendingRequestsIds.clear();
                    pendingRequestsNames.clear();
                    pendingRequestsAvatars.clear();
                    
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        pendingRequestsIds.add(doc.getId());
                        pendingRequestsNames.add(doc.getString("userName") + " (Wants to speak)");
                        pendingRequestsAvatars.add(doc.getString("avatarUrl"));
                    }
                });
    }

    private void showPendingRequestsDialog(int seatIndexToFill) {
        if (pendingRequestsNames.isEmpty()) {
            Toast.makeText(this, "No pending requests right now.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pendingRequestsNames);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select user to add on Seat " + (seatIndexToFill + 1) + ":");
        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedUserId = pendingRequestsIds.get(which);
            String selectedUserName = pendingRequestsNames.get(which).replace(" (Wants to speak)", "");
            String selectedUserAvatar = pendingRequestsAvatars.get(which);
            
            db.collection("Rooms").document(roomName).collection("Requests").document(selectedUserId).delete();
            
            Map<String, Object> newSeat = new HashMap<>();
            newSeat.put("userId", selectedUserId);
            newSeat.put("userName", selectedUserName);
            newSeat.put("avatarUrl", selectedUserAvatar);
            newSeat.put("isMuted", false);
            db.collection("Rooms").document(roomName).collection("Seats").document(String.valueOf(seatIndexToFill)).set(newSeat);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showHostPowersDialog(final int seatIndex) {
        String muteOption = seatIsMuted[seatIndex] ? "Unmute User 🔊" : "Mute User 🔇";
        String[] powers = {muteOption, "Kick User 🥾", "Block User 🚫"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Host Controls (Seat " + (seatIndex + 1) + ")");
        builder.setItems(powers, (dialog, which) -> {
            if (which == 0) { 
                db.collection("Rooms").document(roomName).collection("Seats").document(String.valueOf(seatIndex))
                  .update("isMuted", !seatIsMuted[seatIndex]);
                Toast.makeText(ChatroomActivity.this, "Mute settings changed!", Toast.LENGTH_SHORT).show();
            } else if (which == 1) { 
                db.collection("Rooms").document(roomName).collection("Seats").document(String.valueOf(seatIndex)).delete();
                Toast.makeText(ChatroomActivity.this, "User Kicked from Seat!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void showSelfSeatControls() {
        String micOption = isMicMuted ? "Unmute Mic 🎙️" : "Mute Mic 🔇";
        String[] options = {micOption, "Leave Seat ⬇️"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isHost ? "Host Controls" : "My Seat Controls");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { toggleMic(); } 
            else if (which == 1) {
                if (isHost) Toast.makeText(ChatroomActivity.this, "Host cannot leave seat!", Toast.LENGTH_SHORT).show();
                else {
                    db.collection("Rooms").document(roomName).collection("Seats").document(String.valueOf(mySeatIndex)).delete();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcEngine != null) { mRtcEngine.leaveChannel(); RtcEngine.destroy(); mRtcEngine = null; }
    }

    // 🔥 FIX: SAFE MINIMIZE / EXIT LOGIC
    @Override
    public void onBackPressed() { showExitDialog(); }

    private void showExitDialog() {
        new AlertDialog.Builder(this).setTitle("Exit Chat?").setMessage("Do you want to minimize the chat (stay online) or exit completely?")
            .setPositiveButton("Exit", (dialog, which) -> {
                if(isHost) {
                    db.collection("Rooms").document(currentUserId).delete();
                } else if(isOnSeat && mySeatIndex != -1) {
                    db.collection("Rooms").document(roomName).collection("Seats").document(String.valueOf(mySeatIndex)).delete();
                }
                if (mRtcEngine != null) {
                    mRtcEngine.leaveChannel();
                }
                finish(); 
            })
            .setNegativeButton("Minimize", (dialog, which) -> {
                // Return to Home screen without killing the chatroom
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            })
            .setNeutralButton("Cancel", null).show();
    }
}
