package com.example.tax_loan_emi_calculator;

public class EmiCalculatorHelper {

    public static double calculateEMI(double principal, double annualRate, int years) {
        double monthlyRate = annualRate / (12 * 100);
        int months = years * 12;
        
        return (principal * monthlyRate * Math.pow(1 + monthlyRate, months)) / 
               (Math.pow(1 + monthlyRate, months) - 1);
    }

    public static double calculateTotalPayment(double emi, int years) {
        return emi * years * 12;
    }

    public static double calculateTotalInterest(double totalPayment, double principal) {
        return totalPayment - principal;
    }
}
