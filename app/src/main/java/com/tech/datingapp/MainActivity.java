package com.tech.datingapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML wale dabbe aur buttons ko Java se jod rahe hain
        final LinearLayout layoutMessages = findViewById(R.id.layoutMessages);
        final EditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        // App khulte hi message aayega
        Toast.makeText(this, "Welcome to Secret Chat!", Toast.LENGTH_SHORT).show();

        // Jab user 'SEND' button dabayega:
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Jo type kiya hai usko get karo
                String message = etMessage.getText().toString().trim();

                // Agar message khali nahi hai, toh usko screen par dikhao
                if (!message.isEmpty()) {
                    
                    // Ek naya text banate hain message dikhane ke liye
                    TextView myMessage = new TextView(MainActivity.this);
                    myMessage.setText("You: " + message);
                    myMessage.setTextColor(Color.WHITE);
                    myMessage.setTextSize(18f);
                    myMessage.setPadding(20, 20, 20, 20);
                    
                    // Thoda sa gap dene ke liye
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 10, 0, 10);
                    myMessage.setLayoutParams(params);
                    myMessage.setBackgroundColor(Color.parseColor("#E91E63")); // Pink background for message

                    // Message ko main screen par daal do
                    layoutMessages.addView(myMessage);

                    // Type karne wala dabba wapas khali kar do agle message ke liye
                    etMessage.setText("");
                }
            }
        });
    }
}
