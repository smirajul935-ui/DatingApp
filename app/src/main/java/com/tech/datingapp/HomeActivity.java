package com.tech.datingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    ImageView btnMessages, btnProfile;
    // Humare 2 buttons ko link karne ke liye variable
    Button btnJoinLoveTalk, btnJoinFlirtingZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnMessages = findViewById(R.id.btnMessages);
        btnProfile = findViewById(R.id.btnProfile);
        
        // Buttons ko unki id se dhundho (Abhi humne design me ID nahi di thi, isliye pehle ID set karni hogi, main next step me bata raha hu)
        // btnJoinLoveTalk = findViewById(R.id.btnJoinLoveTalk);
    }
    
    // Ye function XML design se direct call hoga
    public void joinRoom(View view) {
        String roomName = "Chat Room";
        
        // Konsa button dabaya hai uske hisab se naam set karo
        int id = view.getId();
        if (id == R.id.btnJoinLoveTalk) roomName = "Love Talk 💕";
        else if (id == R.id.btnJoinVibe) roomName = "Vibe and Chill 🎵";
        else if (id == R.id.btnJoinFlirt) roomName = "Flirting Zone 🔥";
        else if (id == R.id.btnJoinConnect) roomName = "Let's Connect 👋";

        // Nayi screen (Chatroom) par bhejna
        Intent intent = new Intent(HomeActivity.this, ChatroomActivity.class);
        intent.putExtra("ROOM_NAME", roomName);
        startActivity(intent);
    }
}
