package com.tech.datingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {

    CheckBox cbAgree1, cbAgree2;
    Button btnProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        cbAgree1 = findViewById(R.id.cbAgree1);
        cbAgree2 = findViewById(R.id.cbAgree2);
        btnProceed = findViewById(R.id.btnProceed);

        cbAgree1.setOnCheckedChangeListener((buttonView, isChecked) -> checkAgreements());
        cbAgree2.setOnCheckedChangeListener((buttonView, isChecked) -> checkAgreements());

        btnProceed.setOnClickListener(v -> {
            Intent intent = new Intent(TermsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Wapas is page par nahi aa payega
        });
    }

    private void checkAgreements() {
        if (cbAgree1.isChecked() && cbAgree2.isChecked()) {
            btnProceed.setEnabled(true);
            // Button Pink color ka ho jayega
            btnProceed.setBackgroundColor(android.graphics.Color.parseColor("#E91E63"));
            btnProceed.setTextColor(android.graphics.Color.WHITE);
        } else {
            btnProceed.setEnabled(false);
            // Button Grey rahega
            btnProceed.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
            btnProceed.setTextColor(android.graphics.Color.parseColor("#888888"));
        }
    }
}
