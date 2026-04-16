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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    ImageView ivAvatar;
    EditText etUserName;
    RadioButton rbMale, rbFemale;
    Button btnSaveProfile;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUserId;
    String currentAvatarUrl = "default_avatar"; 

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

        if(ivAvatar != null) {
            ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ProfileActivity.this, "Avatar selection coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(btnSaveProfile != null && etUserName != null && rbMale != null) {
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
                    userProfile.put("avatarUrl", currentAvatarUrl);

                    btnSaveProfile.setEnabled(false);
                    btnSaveProfile.setText("Saving...");

                    db.collection("Users").document(currentUserId)
                        .set(userProfile)
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
}
