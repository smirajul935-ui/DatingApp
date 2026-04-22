package com.tech.datingapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    ImageView btnMessages, btnProfile, btnCreateRoom;
    RecyclerView rvRooms;
    RoomAdapter roomAdapter;
    ArrayList<Map<String, Object>> roomList;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUserId;
    String myUserName = "User"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            fetchMyProfileName(); 
        } else {
            currentUserId = "Guest";
        }

        btnMessages = findViewById(R.id.btnMessages);
        btnProfile = findViewById(R.id.btnProfile);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        
        rvRooms = findViewById(R.id.rvRooms);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        roomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(this, roomList);
        rvRooms.setAdapter(roomAdapter);

        fetchRoomsFromFirebase();
        
        // 🔥 SERVER WAKE-UP LOGIC
        wakeUpRenderServer();

        if (btnCreateRoom != null) {
            btnCreateRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkIfUserAlreadyHasRoom();
                }
            });
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        if (btnMessages != null) {
            btnMessages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, "Opening Messages... (Coming Soon)", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 🚨 SERVER KO SONE NAHI DEGA TAAKI VOICE JALDI CONNECT HO
    private void wakeUpRenderServer() {
        String url = "https://datingserver-ymcg.onrender.com/api/agora-token?channelName=wakeup&uid=0";
        StringRequest request = new StringRequest(Request.Method.GET, url, 
            response -> { }, 
            error -> { });
            
        request.setRetryPolicy(new DefaultRetryPolicy(
                20000, // 20 Seconds wait
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                
        Volley.newRequestQueue(this).add(request);
    }

    private void fetchMyProfileName() {
        db.collection("Users").document(currentUserId).get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists() && documentSnapshot.getString("userName") != null) {
                        myUserName = documentSnapshot.getString("userName");
                    }
                }
            });
    }

    private void checkIfUserAlreadyHasRoom() {
        db.collection("Rooms").whereEqualTo("hostId", currentUserId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            Toast.makeText(HomeActivity.this, "Aap ek hi room create kar sakte hain!", Toast.LENGTH_LONG).show();
                        } else {
                            showCreateRoomDialog();
                        }
                    }
                });
    }

    private void fetchRoomsFromFirebase() {
        db.collection("Rooms").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            
            roomList.clear(); 
            for (DocumentSnapshot doc : value.getDocuments()) {
                if (doc.exists()) {
                    roomList.add(doc.getData());
                }
            }
            roomAdapter.notifyDataSetChanged(); 
        });
    }

    private void showCreateRoomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText etRoomName = dialog.findViewById(R.id.etRoomName);
        final RadioButton rbVoice = dialog.findViewById(R.id.rbVoice);
        final Button btnSubmitRoom = dialog.findViewById(R.id.btnSubmitRoom);

        btnSubmitRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String roomName = etRoomName.getText().toString().trim();
                if (roomName.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Room ka naam likho!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String roomType = rbVoice.isChecked() ? "Voice" : "Video";

                Map<String, Object> roomData = new HashMap<>();
                roomData.put("roomName", roomName);
                roomData.put("roomType", roomType);
                roomData.put("hostId", currentUserId);
                roomData.put("hostName", myUserName); 
                roomData.put("onlineCount", 1);
                
                btnSubmitRoom.setEnabled(false);

                db.collection("Rooms").document(currentUserId)
                    .set(roomData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(HomeActivity.this, "Room Created!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            Intent intent = new Intent(HomeActivity.this, ChatroomActivity.class);
                            intent.putExtra("ROOM_NAME", roomName);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            btnSubmitRoom.setEnabled(true);
                        }
                    });
            }
        });
        dialog.show();
    }
}
