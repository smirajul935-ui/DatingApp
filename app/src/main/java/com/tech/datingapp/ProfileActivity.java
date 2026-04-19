package com.tech.datingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    ImageView ivAvatar;
    ImageView av1, av2, av3, av4, av5, av6; // Avatar list
    EditText etUserName;
    RadioButton rbMale, rbFemale;
    Button btnSaveProfile;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUserId;
    
    // Default Avatar (Agar koi select na kare)
    String currentAvatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=Sagar"; 

    // 6 Premium Avatars ki Links (Male & Female)
    String[] avatarLinks = {
        "https://api.dicebear.com/7.x/avataaars/png?seed=Sagar",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Priya",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Rahul",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Neha",
        "https://api.dicebear.com/7.x/avataaars/png?seed=King",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Queen"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if(mAuth.getCurrentUser() != null){
            currentUserId = mAuth.getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        ivAvatar = findViewById(R.id.ivAvatar);
        etUserName = findViewById(R.id.etUserName);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Link avatar image views
        av1 = findViewById(R.id.av1);
        av2 = findViewById(R.id.av2);
        av3 = findViewById(R.id.av3);
        av4 = findViewById(R.id.av4);
        av5 = findViewById(R.id.av5);
        av6 = findViewById(R.id.av6);

        // Load 6 Avatars into the list
        loadAvatarsIntoList();

        // Load Saved Data (Agar pehle se save hai)
        loadUserProfile();

        // Set Click Listeners for Avatar Selection
        setAvatarClickListeners();

        if(btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userName = etUserName.getText().toString().trim();
                    
                    if (userName.isEmpty()) {
                        Toast.makeText(ProfileActivity.this, "Please enter your name!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String gender = rbMale.isChecked() ? "Male" : "Female";

                    Map<String, Object> userProfile = new HashMap<>();
                    userProfile.put("userName", userName);
                    userProfile.put("gender", gender);
                    userProfile.put("avatarUrl", currentAvatarUrl); // 🔥 Selected Avatar URL saved!

                    btnSaveProfile.setEnabled(false);
                    btnSaveProfile.setText("Saving...");

                    db.collection("Users").document(currentUserId)
                        .set(userProfile, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ProfileActivity.this, "Profile Saved Successfully! ✅", Toast.LENGTH_LONG).show();
                                finish(); 
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                btnSaveProfile.setEnabled(true);
                                btnSaveProfile.setText("SAVE PROFILE");
                            }
                        });
                }
            });
        }
    }

    // Niche ki list me 6 photos load karna
    private void loadAvatarsIntoList() {
        try {
            Glide.with(this).load(avatarLinks[0]).circleCrop().into(av1);
            Glide.with(this).load(avatarLinks[1]).circleCrop().into(av2);
            Glide.with(this).load(avatarLinks[2]).circleCrop().into(av3);
            Glide.with(this).load(avatarLinks[3]).circleCrop().into(av4);
            Glide.with(this).load(avatarLinks[4]).circleCrop().into(av5);
            Glide.with(this).load(avatarLinks[5]).circleCrop().into(av6);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Jo bhi avatar touch hoga, wo main DP ban jayega
    private void setAvatarClickListeners() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.av1) currentAvatarUrl = avatarLinks[0];
                else if (id == R.id.av2) currentAvatarUrl = avatarLinks[1];
                else if (id == R.id.av3) currentAvatarUrl = avatarLinks[2];
                else if (id == R.id.av4) currentAvatarUrl = avatarLinks[3];
                else if (id == R.id.av5) currentAvatarUrl = avatarLinks[4];
                else if (id == R.id.av6) currentAvatarUrl = avatarLinks[5];

                // Main Image View ko update karo
                try {
                    Glide.with(ProfileActivity.this).load(currentAvatarUrl).circleCrop().into(ivAvatar);
                } catch (Exception e) {}
            }
        };

        av1.setOnClickListener(listener);
        av2.setOnClickListener(listener);
        av3.setOnClickListener(listener);
        av4.setOnClickListener(listener);
        av5.setOnClickListener(listener);
        av6.setOnClickListener(listener);
    }

    // Firebase se purani details padhna
    private void loadUserProfile() {
        db.collection("Users").document(currentUserId).get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String savedName = documentSnapshot.getString("userName");
                        String savedGender = documentSnapshot.getString("gender");
                        String savedAvatar = documentSnapshot.getString("avatarUrl");

                        if (savedName != null && !savedName.isEmpty() && etUserName != null) {
                            etUserName.setText(savedName);
                        }
                        if (savedGender != null && savedGender.equals("Female")) {
                            if(rbFemale != null) rbFemale.setChecked(true);
                        } else {
                            if(rbMale != null) rbMale.setChecked(true);
                        }
                        
                        // Purana Avatar Load karo main DP me
                        if(savedAvatar != null && !savedAvatar.isEmpty()) {
                            currentAvatarUrl = savedAvatar;
                            try {
                                Glide.with(ProfileActivity.this).load(currentAvatarUrl).circleCrop().into(ivAvatar);
                            } catch (Exception e) {}
                        }
                    }
                }
            });
    }
}
