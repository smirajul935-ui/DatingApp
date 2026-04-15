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
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    ImageView btnMessages, btnProfile, btnCreateRoom;
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

        // CREATE ROOM BUTTON CLICK
        if (btnCreateRoom != null) {
            btnCreateRoom.setOnClickListener(v -> showCreateRoomDialog());
        }
    }

    // Naya Room Banane ka Popup
    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText etRoomName = dialog.findViewById(R.id.etRoomName);
        RadioButton rbVoice = dialog.findViewById(R.id.rbVoice);
        Button btnSubmitRoom = dialog.findViewById(R.id.btnSubmitRoom);

        btnSubmitRoom.setOnClickListener(v -> {
            String roomName = etRoomName.getText().toString().trim();
            if (roomName.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Room ka naam likho!", Toast.LENGTH_SHORT).show();
                return;
            }

            String roomType = rbVoice.isChecked() ? "Voice" : "Video";
            String hostId = mAuth.getCurrentUser().getUid();

            // Firebase me save karne ka data
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("roomName", roomName);
            roomData.put("roomType", roomType);
            roomData.put("hostId", hostId);
            roomData.put("onlineCount", 1);
            
            Toast.makeText(HomeActivity.this, "Creating Room...", Toast.LENGTH_SHORT).show();
            btnSubmitRoom.setEnabled(false);

            // Firebase me "Rooms" naam ka folder banakar waha save karna
            db.collection("Rooms").document(hostId + "_" + roomType)
                .set(roomData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeActivity.this, roomType + " Room Created Successfully!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    // Room banne ke baad direct room me bhejna
                    Intent intent = new Intent(HomeActivity.this, ChatroomActivity.class);
                    intent.putExtra("ROOM_NAME", roomName);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmitRoom.setEnabled(true);
                });
        });

        dialog.show();
    }

    // Dummy Button Clicks (Jo already the)
    public void joinRoom(View view) {
        String roomName = "Chat Room";
        int id = view.getId();
        if (id == R.id.btnJoinLoveTalk) roomName = "Love Talk 💕";
        else if (id == R.id.btnJoinVibe) roomName = "Vibe and Chill 🎵";
        else if (id == R.id.btnJoinFlirt) roomName = "Flirting Zone 🔥";
        else if (id == R.id.btnJoinConnect) roomName = "Let's Connect 👋";

        Intent intent = new Intent(HomeActivity.this, ChatroomActivity.class);
        intent.putExtra("ROOM_NAME", roomName);
        startActivity(intent);
    }
}
