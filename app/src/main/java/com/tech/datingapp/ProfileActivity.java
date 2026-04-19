package com.tech.datingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    ImageView ivAvatar;
    EditText etUserName;
    RadioButton rbMale, rbFemale;
    Button btnSaveProfile;
    RecyclerView rvAvatars;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUserId;
    
    // Default DP
    String currentAvatarUrl = "https://raw.githubusercontent.com/smirajul935-ui/Datingappdp/main/dp1.png"; 

    // 🔥 DYNAMIC LIST (Aage chalkar ye Firebase se fetch hoga)
    List<String> avatarList = new ArrayList<>();

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
        rvAvatars = findViewById(R.id.rvAvatars);

        // Add tumhari repo ke links (Tum inko baad me badha sakte ho)
        avatarList.add("https://raw.githubusercontent.com/smirajul935-ui/Datingappdp/main/dp1.png");
        avatarList.add("https://raw.githubusercontent.com/smirajul935-ui/Datingappdp/main/dp2.png");
        avatarList.add("https://raw.githubusercontent.com/smirajul935-ui/Datingappdp/main/dp3.png");
        avatarList.add("https://raw.githubusercontent.com/smirajul935-ui/Datingappdp/main/dp4.png");
        avatarList.add("https://raw.githubusercontent.com/smirajul935-ui/Datingappdp/main/dp5.png");

        // Setup RecyclerView
        if(rvAvatars != null) {
            rvAvatars.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            AvatarAdapter adapter = new AvatarAdapter(avatarList);
            rvAvatars.setAdapter(adapter);
        }

        loadUserProfile();

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
                    userProfile.put("avatarUrl", currentAvatarUrl); 

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

    private void loadUserProfile() {
        db.collection("Users").document(currentUserId).get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String savedName = documentSnapshot.getString("userName");
                        String savedGender = documentSnapshot.getString("gender");
                        String savedAvatar = documentSnapshot.getString("avatarUrl");

                        if (savedName != null && etUserName != null) etUserName.setText(savedName);
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
                }
            });
    }

    // 🔥 INNER ADAPTER CLASS FOR RECYCLERVIEW (100% Safe Java Code)
    class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {
        List<String> list;
        
        public AvatarAdapter(List<String> list) { 
            this.list = list; 
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.item_avatar, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final String url = list.get(position);
            
            try {
                Glide.with(ProfileActivity.this)
                     .load(url)
                     .diskCacheStrategy(DiskCacheStrategy.NONE)
                     .skipMemoryCache(true)
                     .placeholder(android.R.drawable.sym_def_app_icon)
                     .circleCrop()
                     .into(holder.img);
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentAvatarUrl = url;
                    try { 
                        Glide.with(ProfileActivity.this)
                             .load(currentAvatarUrl)
                             .diskCacheStrategy(DiskCacheStrategy.NONE)
                             .skipMemoryCache(true)
                             .circleCrop()
                             .into(ivAvatar); 
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public int getItemCount() { 
            return list.size(); 
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.ivAvatarItem);
            }
        }
    }
}
