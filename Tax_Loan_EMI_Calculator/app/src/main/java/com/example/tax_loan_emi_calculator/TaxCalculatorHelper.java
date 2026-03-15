package com.example.tax_loan_emi_calculator;

public class TaxCalculatorHelper {

    public static double calculateTaxableIncome(double annualIncome, double standardDeduction) {
        double taxableIncome = annualIncome - standardDeduction;
        return Math.max(0, taxableIncome);
    }

    public static double calculateTax(double taxableIncome, boolean isNewRegime) {
        if (isNewRegime) {
            return calculateNewRegimeTax(taxableIncome);
        } else {
            return calculateOldRegimeTax(taxableIncome);
        }
    }

    private static double calculateNewRegimeTax(double income) {
        // Simplified FY 2023-24 New Regime Slabs
        double tax = 0;
        if (income <= 300000) return 0;
        if (income <= 700000) return 0; // Rebate u/s 87A

        if (income > 300000) tax += Math.min(income - 300000, 300000) * 0.05;
        if (income > 600000) tax += Math.min(income - 600000, 300000) * 0.10;
        if (income > 900000) tax += Math.min(income - 900000, 300000) * 0.15;
        if (income > 1200000) tax += Math.min(income - 1200000, 300000) * 0.20;
        if (income > 1500000) tax += (income - 1500000) * 0.30;

        return tax;
    }

    private static double calculateOldRegimeTax(double income) {
        // Simplified FY 2023-24 Old Regime Slabs (General Category)
        double tax = 0;
        if (income <= 250000) return 0;
        
        if (income > 250000) tax += Math.min(income - 250000, 250000) * 0.05;
        if (income > 500000) tax += Math.min(income - 500000, 500000) * 0.20;
        if (income > 1000000) tax += (income - 1000000) * 0.30;
        
        // Rebate u/s 87A for income up to 5L
        if (income <= 500000) return 0;

        return tax;
    }
}
