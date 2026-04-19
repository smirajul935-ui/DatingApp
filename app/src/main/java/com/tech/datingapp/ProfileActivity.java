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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
    ImageView av1, av2, av3, av4, av5, av6; 
    EditText etUserName;
    RadioButton rbMale, rbFemale;
    Button btnSaveProfile;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUserId;
    
    // Default DP (Agar koi load na ho)
    String currentAvatarUrl = "https://raw.githubusercontent.com/smirajul935/DatingApp/main/dp1.png"; 

    // 🔥 GITHUB CLOUD DPs (Tumhare GitHub se live images aayengi)
    // Dhyan rahe, GitHub pe inhi naamo se (dp1.png, dp2.png...) photo upload karni hogi!
    String[] avatarLinks = {
        "https://github.com/smirajul935-ui/Datingappdp/blob/main/dp1.png", 
        "https://raw.githubusercontent.com/smirajul935/DatingApp/main/dp2.png",
        "https://raw.githubusercontent.com/smirajul935/DatingApp/main/dp3.png",
        "https://raw.githubusercontent.com/smirajul935/DatingApp/main/dp4.png",
        "https://raw.githubusercontent.com/smirajul935/DatingApp/main/dp5.png",
        "https://raw.githubusercontent.com/smirajul935/DatingApp/main/dp6.png"
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

        av1 = findViewById(R.id.av1);
        av2 = findViewById(R.id.av2);
        av3 = findViewById(R.id.av3);
        av4 = findViewById(R.id.av4);
        av5 = findViewById(R.id.av5);
        av6 = findViewById(R.id.av6);

        // Niche ki list me GitHub se Live DPs load karo
        loadAvatarsIntoList();
        
        // Firebase se purana data load karo
        loadUserProfile();
        
        // Clicks suno
        setAvatarClickListeners();

        if(btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> {
                String userName = etUserName.getText().toString().trim();
                if (userName.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please enter your name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String gender = rbMale.isChecked() ? "Male" : "Female";

                Map<String, Object> userProfile = new HashMap<>();
                userProfile.put("userName", userName);
                userProfile.put("gender", gender);
                userProfile.put("avatarUrl", currentAvatarUrl); // 🔥 Selected GitHub DP saved!

                btnSaveProfile.setEnabled(false);
                btnSaveProfile.setText("Saving...");

                db.collection("Users").document(currentUserId)
                    .set(userProfile, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Profile Saved Successfully! ✅", Toast.LENGTH_LONG).show();
                        finish(); 
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText("SAVE PROFILE");
                    });
            });
        }
    }

    // 🚨 GLIDE ENGINE (Cloud Images)
    // DiskCacheStrategy.NONE isliye lagaya hai taaki jab tum GitHub pe photo badlo, toh app me turant nayi photo dikhe, purani na atak jaye.
    private void loadAvatarsIntoList() {
        try {
            Glide.with(this).load(avatarLinks[0]).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(android.R.drawable.sym_def_app_icon).circleCrop().into(av1);
            Glide.with(this).load(avatarLinks[1]).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(android.R.drawable.sym_def_app_icon).circleCrop().into(av2);
            Glide.with(this).load(avatarLinks[2]).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(android.R.drawable.sym_def_app_icon).circleCrop().into(av3);
            Glide.with(this).load(avatarLinks[3]).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(android.R.drawable.sym_def_app_icon).circleCrop().into(av4);
            Glide.with(this).load(avatarLinks[4]).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(android.R.drawable.sym_def_app_icon).circleCrop().into(av5);
            Glide.with(this).load(avatarLinks[5]).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(android.R.drawable.sym_def_app_icon).circleCrop().into(av6);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setAvatarClickListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.av1) currentAvatarUrl = avatarLinks[0];
            else if (id == R.id.av2) currentAvatarUrl = avatarLinks[1];
            else if (id == R.id.av3) currentAvatarUrl = avatarLinks[2];
            else if (id == R.id.av4) currentAvatarUrl = avatarLinks[3];
            else if (id == R.id.av5) currentAvatarUrl = avatarLinks[4];
            else if (id == R.id.av6) currentAvatarUrl = avatarLinks[5];

            try { 
                // Badi DP change karo
                Glide.with(ProfileActivity.this)
                     .load(currentAvatarUrl)
                     .diskCacheStrategy(DiskCacheStrategy.NONE)
                     .skipMemoryCache(true)
                     .circleCrop()
                     .into(ivAvatar); 
            } catch (Exception e) {}
        };

        av1.setOnClickListener(listener); av2.setOnClickListener(listener); av3.setOnClickListener(listener);
        av4.setOnClickListener(listener); av5.setOnClickListener(listener); av6.setOnClickListener(listener);
    }

    private void loadUserProfile() {
        db.collection("Users").document(currentUserId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String savedName = documentSnapshot.getString("userName");
                    String savedGender = documentSnapshot.getString("gender");
                    String savedAvatar = documentSnapshot.getString("avatarUrl");

                    if (savedName != null && !savedName.isEmpty() && etUserName != null) etUserName.setText(savedName);
                    if (savedGender != null && savedGender.equals("Female")) {
                        if(rbFemale != null) rbFemale.setChecked(true);
                    } else {
                        if(rbMale != null) rbMale.setChecked(true);
                    }
                    if(savedAvatar != null && !savedAvatar.isEmpty()) {
                        currentAvatarUrl = savedAvatar;
                        try { 
                            Glide.with(ProfileActivity.this)
                                 .load(currentAvatarUrl)
                                 .diskCacheStrategy(DiskCacheStrategy.NONE)
                                 .skipMemoryCache(true)
                                 .circleCrop()
                                 .into(ivAvatar); 
                        } catch (Exception e) {}
                    }
                }
            });
    }
}
