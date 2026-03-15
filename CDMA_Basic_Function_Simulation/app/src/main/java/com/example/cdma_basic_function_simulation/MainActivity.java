package com.example.cdma_basic_function_simulation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etUser1Data, etUser2Data;
    private Button btnEncode, btnDecode;
    private TextView tvResults;

    // Fixed spreading codes (Walsh codes of length 2)
    // Code 1: [1, 1], Code 2: [1, -1]
    private final int[] code1 = {1, 1};
    private final int[] code2 = {1, -1};

    private List<Integer> combinedSignal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUser1Data = findViewById(R.id.etUser1Data);
        etUser2Data = findViewById(R.id.etUser2Data);
        btnEncode = findViewById(R.id.btnEncode);
        btnDecode = findViewById(R.id.btnDecode);
        tvResults = findViewById(R.id.tvResults);

        btnEncode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performEncoding();
            }
        });

        btnDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performDecoding();
            }
        });
    }

    private void performEncoding() {
        String data1Str = etUser1Data.getText().toString().trim();
        String data2Str = etUser2Data.getText().toString().trim();

        if (data1Str.isEmpty() || data2Str.isEmpty()) {
            Toast.makeText(this, "Please enter binary data for both users", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data1Str.length() != data2Str.length()) {
            Toast.makeText(this, "Data streams must have the same length", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Convert binary strings to polar form (0 -> -1, 1 -> 1)
        int[] user1Bits = convertToPolar(data1Str);
        int[] user2Bits = convertToPolar(data2Str);

        // Step 2: Spreading - Multiply each bit by the spreading code
        List<Integer> signal1 = spread(user1Bits, code1);
        List<Integer> signal2 = spread(user2Bits, code2);

        // Step 3: Combining - Add the signals together
        combinedSignal.clear();
        for (int i = 0; i < signal1.size(); i++) {
            combinedSignal.add(signal1.get(i) + signal2.get(i));
        }

        // Display Results
        StringBuilder sb = new StringBuilder();
        sb.append("--- ENCODING PROCESS ---\n\n");
        sb.append("User 1 Data: ").append(data1Str).append("\n");
        sb.append("Code 1: ").append(Arrays.toString(code1)).append("\n");
        sb.append("Signal 1: ").append(signal1.toString()).append("\n\n");

        sb.append("User 2 Data: ").append(data2Str).append("\n");
        sb.append("Code 2: ").append(Arrays.toString(code2)).append("\n");
        sb.append("Signal 2: ").append(signal2.toString()).append("\n\n");

        sb.append("COMBINED SIGNAL (Multiplexed):\n");
        sb.append(combinedSignal.toString()).append("\n");

        tvResults.setText(sb.toString());
        btnDecode.setEnabled(true);
    }

    private void performDecoding() {
        if (combinedSignal.isEmpty()) {
            Toast.makeText(this, "Encode signal first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 4: Despreading - Multiply combined signal by user's code and integrate
        String decoded1 = despread(combinedSignal, code1);
        String decoded2 = despread(combinedSignal, code2);

        StringBuilder sb = new StringBuilder(tvResults.getText().toString());
        sb.append("\n--- DECODING PROCESS ---\n\n");
        sb.append("Decoded User 1: ").append(decoded1).append("\n");
        sb.append("Decoded User 2: ").append(decoded2).append("\n");

        if (decoded1.equals(etUser1Data.getText().toString()) && 
            decoded2.equals(etUser2Data.getText().toString())) {
            sb.append("\nSUCCESS: Data recovered correctly!");
        } else {
            sb.append("\nFAILURE: Data mismatch.");
        }

        tvResults.setText(sb.toString());
    }

    private int[] convertToPolar(String data) {
        int[] polar = new int[data.length()];
        for (int i = 0; i < data.length(); i++) {
            // CDMA convention: bit '0' is represented as -1, bit '1' as +1
            polar[i] = (data.charAt(i) == '1') ? 1 : -1;
        }
        return polar;
    }

    private List<Integer> spread(int[] data, int[] code) {
        List<Integer> result = new ArrayList<>();
        for (int bit : data) {
            for (int chip : code) {
                result.add(bit * chip);
            }
        }
        return result;
    }

    private String despread(List<Integer> signal, int[] code) {
        StringBuilder result = new StringBuilder();
        int chipLen = code.length;
        
        for (int i = 0; i < signal.size(); i += chipLen) {
            int sum = 0;
            for (int j = 0; j < chipLen; j++) {
                sum += signal.get(i + j) * code[j];
            }
            // If the inner product is positive, the bit was 1. If negative, 0.
            // (sum / chipLen) would give back the original polar value (1 or -1)
            result.append(sum > 0 ? "1" : "0");
        }
        return result.toString();
    }
}
