package com.example.gui_components_application;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Declare UI components
    private TextInputEditText etName;
    private RadioGroup rgGender;
    private CheckBox cbReading, cbCoding, cbSports;
    private Spinner spinnerCountry;
    private Button btnSubmit;
    private MaterialCardView resultCard;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeViews();

        // Setup Spinner
        setupSpinner();

        // Handle Submit Button Click
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        rgGender = findViewById(R.id.rgGender);
        cbReading = findViewById(R.id.cbReading);
        cbCoding = findViewById(R.id.cbCoding);
        cbSports = findViewById(R.id.cbSports);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        btnSubmit = findViewById(R.id.btnSubmit);
        resultCard = findViewById(R.id.resultCard);
        tvResult = findViewById(R.id.tvResult);
    }

    private void setupSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerCountry.setAdapter(adapter);
    }

    private void submitForm() {
        // 1. Validation
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError(getString(R.string.validation_error));
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rbGender = findViewById(selectedGenderId);
        String gender = rbGender.getText().toString();

        // 2. Gather Data
        List<String> hobbies = new ArrayList<>();
        if (cbReading.isChecked()) hobbies.add(getString(R.string.reading));
        if (cbCoding.isChecked()) hobbies.add(getString(R.string.coding));
        if (cbSports.isChecked()) hobbies.add(getString(R.string.sports));

        String country = spinnerCountry.getSelectedItem().toString();
        if (spinnerCountry.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Display Result
        StringBuilder result = new StringBuilder();
        result.append("Name: ").append(name).append("\n");
        result.append("Gender: ").append(gender).append("\n");
        // Using TextUtils.join for backward compatibility (minSdk 24)
        result.append("Hobbies: ").append(hobbies.isEmpty() ? "None" : TextUtils.join(", ", hobbies)).append("\n");
        result.append("Country: ").append(country);

        tvResult.setText(result.toString());
        resultCard.setVisibility(View.VISIBLE);

        // 4. Show Toast
        Toast.makeText(this, R.string.submission_success, Toast.LENGTH_LONG).show();
    }
}
