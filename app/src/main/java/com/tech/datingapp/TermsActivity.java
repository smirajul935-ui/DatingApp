package com.tech.datingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {

    CheckBox cbAgree1, cbAgree2;
    Button btnProceed;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🚨 Check if user has already accepted T&C
        sharedPreferences = getSharedPreferences("SecureDatingPrefs", MODE_PRIVATE);
        boolean isTermsAccepted = sharedPreferences.getBoolean("isTermsAccepted", false);

        if (isTermsAccepted) {
            startActivity(new Intent(TermsActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_terms);

        cbAgree1 = findViewById(R.id.cbAgree1);
        cbAgree2 = findViewById(R.id.cbAgree2);
        btnProceed = findViewById(R.id.btnProceed);

        cbAgree1.setOnCheckedChangeListener((buttonView, isChecked) -> checkAgreements());
        cbAgree2.setOnCheckedChangeListener((buttonView, isChecked) -> checkAgreements());

        btnProceed.setOnClickListener(v -> {
            // Save that user has accepted T&C
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isTermsAccepted", true);
            editor.apply();

            Intent intent = new Intent(TermsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkAgreements() {
        if (cbAgree1.isChecked() && cbAgree2.isChecked()) {
            btnProceed.setEnabled(true);
            btnProceed.setBackgroundColor(android.graphics.Color.parseColor("#E91E63"));
            btnProceed.setTextColor(android.graphics.Color.WHITE);
        } else {
            btnProceed.setEnabled(false);
            btnProceed.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
            btnProceed.setTextColor(android.graphics.Color.parseColor("#888888"));
        }
    }
}
