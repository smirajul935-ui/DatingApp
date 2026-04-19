package com.tech.datingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnSignup;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();

        // 🔥 DEEP LINKING: Check if app was opened via WhatsApp Link
        Uri data = getIntent().getData();
        String roomFromLink = null;
        if (data != null && data.getHost() != null && data.getHost().equals("securedating.app")) {
            // URL aisi hogi: https://securedating.app/join?room=Love%20Talk
            roomFromLink = data.getQueryParameter("room");
        }

        // 🚨 AUTO-LOGIN & ROUTING
        if (mAuth.getCurrentUser() != null) {
            // Agar Link se aaya hai toh direct Room me bhejo
            if (roomFromLink != null && !roomFromLink.isEmpty()) {
                Toast.makeText(this, "Joining Room from Link... 🔗", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ChatroomActivity.class);
                intent.putExtra("ROOM_NAME", roomFromLink);
                startActivity(intent);
                finish();
                return;
            } else {
                // Normally app khola hai toh T&C / Home pe bhejo
                Intent intent = new Intent(MainActivity.this, TermsActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        } else if (roomFromLink != null && !roomFromLink.isEmpty()) {
            // Agar Login nahi hai par Link dabaya hai, toh usko batao ki pehle login karo
            Toast.makeText(this, "Please Login first to join the room!", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Email aur Password dalo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(MainActivity.this, "Account ban raha hai...", Toast.LENGTH_SHORT).show();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Signup Successful! ✅", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MainActivity.this, TermsActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Email aur Password dalo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(MainActivity.this, "Login ho raha hai...", Toast.LENGTH_SHORT).show();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Login Successful! 🎉", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MainActivity.this, TermsActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "Login Failed! Wrong Password?", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}
