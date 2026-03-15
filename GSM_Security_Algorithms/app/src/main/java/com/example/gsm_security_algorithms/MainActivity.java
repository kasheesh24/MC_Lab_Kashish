package com.example.gsm_security_algorithms;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etRand, etKi, etMessage;
    private TextView tvSres, tvKc, tvEncrypted;
    private Button btnAuth, btnEncrypt;

    private String currentKc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        etRand = findViewById(R.id.etRand);
        etKi = findViewById(R.id.etKi);
        etMessage = findViewById(R.id.etMessage);
        tvSres = findViewById(R.id.tvSres);
        tvKc = findViewById(R.id.tvKc);
        tvEncrypted = findViewById(R.id.tvEncrypted);
        btnAuth = findViewById(R.id.btnAuth);
        btnEncrypt = findViewById(R.id.btnEncrypt);

        // Authentication Button Listener
        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAuthentication();
            }
        });

        // Encryption Button Listener
        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptMessage();
            }
        });
    }

    private void generateAuthentication() {
        String randStr = etRand.getText().toString().trim();
        String kiStr = etKi.getText().toString().trim();

        if (randStr.isEmpty() || kiStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_input), Toast.LENGTH_SHORT).show();
            return;
        }

        // A3 Algorithm Simulation: Generate SRES
        // In reality, A3 takes 128-bit RAND and 128-bit Ki to produce a 32-bit SRES.
        // For simulation, we perform XOR and basic bitwise operations.
        String sres = runA3(randStr, kiStr);
        tvSres.setText(getString(R.string.sres_format, sres));

        // A8 Algorithm Simulation: Generate Session Key (Kc)
        // In reality, A8 takes 128-bit RAND and 128-bit Ki to produce a 64-bit Kc.
        currentKc = runA8(randStr, kiStr);
        tvKc.setText(getString(R.string.kc_format, currentKc));
        
        Toast.makeText(this, getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
    }

    private void encryptMessage() {
        String message = etMessage.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentKc.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_kc), Toast.LENGTH_SHORT).show();
            return;
        }

        // A5 Algorithm Simulation: Encrypt message using Kc
        // A5 is a stream cipher. Here we simulate it by XORing the message with the Kc.
        String encrypted = runA5(message, currentKc);
        tvEncrypted.setText(getString(R.string.encrypted_format, encrypted));
    }

    /**
     * A3 Algorithm Simulation
     * Purpose: Subscriber Authentication.
     * Logic: Simplified XOR and modulo operation to simulate cryptographic one-way function.
     */
    private String runA3(String rand, String ki) {
        long hash = 0;
        int len = Math.min(rand.length(), ki.length());
        
        for (int i = 0; i < len; i++) {
            hash += (rand.charAt(i) ^ ki.charAt(i)) * (i + 1);
        }
        
        // Return a 8-character (32-bit) hex string
        return String.format(Locale.US, "%08X", (int)(hash & 0xFFFFFFFFL));
    }

    /**
     * A8 Algorithm Simulation
     * Purpose: Session Key Generation.
     * Logic: Uses RAND and Ki to generate a 64-bit session key.
     */
    private String runA8(String rand, String ki) {
        String combined = rand + ki;
        long hash1 = 0;
        long hash2 = 0;
        
        for (int i = 0; i < combined.length(); i++) {
            if (i % 2 == 0) hash1 = (hash1 << 5) - hash1 + combined.charAt(i);
            else hash2 = (hash2 << 5) - hash2 + combined.charAt(i);
        }
        
        // Return a 16-character (64-bit) hex string
        return String.format(Locale.US, "%08X%08X", (int)hash1, (int)hash2);
    }

    /**
     * A5 Algorithm Simulation
     * Purpose: Over-the-air Encryption.
     * Logic: Stream cipher simulation using XOR between plaintext and Kc.
     */
    private String runA5(String message, String kc) {
        StringBuilder encrypted = new StringBuilder();
        if (kc.isEmpty()) return "";
        
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            char k = kc.charAt(i % kc.length());
            int xor = c ^ k;
            encrypted.append(String.format("%02X", xor));
        }
        return encrypted.toString();
    }
}
