package com.example.tax_loan_emi_calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etLoanAmount, etInterestRate, etTenure;
    private TextInputEditText etAnnualIncome, etDeduction;
    private RadioGroup rgRegime;
    private TextView tvEmiResult, tvTaxResult;
    private CardView emiCard, taxCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        emiCard = findViewById(R.id.emiCard);
        taxCard = findViewById(R.id.taxCard);

        // EMI Views
        etLoanAmount = findViewById(R.id.etLoanAmount);
        etInterestRate = findViewById(R.id.etInterestRate);
        etTenure = findViewById(R.id.etTenure);
        Button btnCalculateEmi = findViewById(R.id.btnCalculateEmi);
        tvEmiResult = findViewById(R.id.tvEmiResult);

        // Tax Views
        etAnnualIncome = findViewById(R.id.etAnnualIncome);
        etDeduction = findViewById(R.id.etDeduction);
        rgRegime = findViewById(R.id.rgRegime);
        Button btnCalculateTax = findViewById(R.id.btnCalculateTax);
        tvTaxResult = findViewById(R.id.tvTaxResult);

        // Tab Selection Logic
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    emiCard.setVisibility(View.VISIBLE);
                    taxCard.setVisibility(View.GONE);
                } else {
                    emiCard.setVisibility(View.GONE);
                    taxCard.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // EMI Calculation
        btnCalculateEmi.setOnClickListener(v -> calculateEMI());

        // Tax Calculation
        btnCalculateTax.setOnClickListener(v -> calculateTax());
    }

    private void calculateEMI() {
        String amountStr = etLoanAmount.getText().toString();
        String rateStr = etInterestRate.getText().toString();
        String tenureStr = etTenure.getText().toString();

        if (amountStr.isEmpty() || rateStr.isEmpty() || tenureStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double principal = Double.parseDouble(amountStr);
        double rate = Double.parseDouble(rateStr);
        int years = Integer.parseInt(tenureStr);

        double emi = EmiCalculatorHelper.calculateEMI(principal, rate, years);
        double totalPayment = EmiCalculatorHelper.calculateTotalPayment(emi, years);
        double totalInterest = EmiCalculatorHelper.calculateTotalInterest(totalPayment, principal);

        tvEmiResult.setVisibility(View.VISIBLE);
        tvEmiResult.setText(String.format(Locale.getDefault(),
                "Monthly EMI: ₹%.2f\nTotal Interest: ₹%.2f\nTotal Payment: ₹%.2f",
                emi, totalInterest, totalPayment));
    }

    private void calculateTax() {
        String incomeStr = etAnnualIncome.getText().toString();
        String deductionStr = etDeduction.getText().toString();

        if (incomeStr.isEmpty() || deductionStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double annualIncome = Double.parseDouble(incomeStr);
        double standardDeduction = Double.parseDouble(deductionStr);
        boolean isNewRegime = rgRegime.getCheckedRadioButtonId() == R.id.rbNewRegime;

        double taxableIncome = TaxCalculatorHelper.calculateTaxableIncome(annualIncome, standardDeduction);
        double taxAmount = TaxCalculatorHelper.calculateTax(taxableIncome, isNewRegime);
        double netIncome = annualIncome - taxAmount;

        tvTaxResult.setVisibility(View.VISIBLE);
        tvTaxResult.setText(String.format(Locale.getDefault(),
                "Taxable Income: ₹%.2f\nTax Amount: ₹%.2f\nNet Income After Tax: ₹%.2f",
                taxableIncome, taxAmount, netIncome));
    }
}
