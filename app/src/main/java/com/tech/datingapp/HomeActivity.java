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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnMessages = findViewById(R.id.btnMessages);
        btnProfile = findViewById(R.id.btnProfile);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        
        // RecyclerView Setup
        rvRooms = findViewById(R.id.rvRooms);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        roomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(this, roomList);
        rvRooms.setAdapter(roomAdapter);

        // Fetch Rooms from Firebase
        fetchRoomsFromFirebase();

        // CREATE ROOM BUTTON CLICK
        if (btnCreateRoom != null) {
            btnCreateRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCreateRoomDialog();
                }
            });
        }
    }

    // 🔥 FIREBASE SE REAL ROOMS NIKALNA
    private void fetchRoomsFromFirebase() {
        db.collection("Rooms").addSnapshotListener((value, error) -> {
            if (error != null || value == null) {
                Toast.makeText(HomeActivity.this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
                return;
            }
            
            roomList.clear(); // Purani list saaf karo
            for (DocumentSnapshot doc : value.getDocuments()) {
                if (doc.exists()) {
                    roomList.add(doc.getData());
                }
            }
            roomAdapter.notifyDataSetChanged(); // Screen update karo
        });
    }

    // Naya Room Banane ka Popup
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
                String hostId = "Guest";
                if(mAuth.getCurrentUser() != null) {
                    hostId = mAuth.getCurrentUser().getUid();
                }

                Map<String, Object> roomData = new HashMap<>();
                roomData.put("roomName", roomName);
                roomData.put("roomType", roomType);
                roomData.put("hostId", hostId);
                roomData.put("onlineCount", 1);
                
                Toast.makeText(HomeActivity.this, "Creating Room...", Toast.LENGTH_SHORT).show();
                btnSubmitRoom.setEnabled(false);

                // Document ID room name banaya hai taaki unique rahe aur same naam ka dobara na bane
                db.collection("Rooms").document(roomName)
                    .set(roomData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(HomeActivity.this, roomType + " Room Created Successfully!", Toast.LENGTH_LONG).show();
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
