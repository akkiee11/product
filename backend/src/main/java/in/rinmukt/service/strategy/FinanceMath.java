package in.rinmukt.service.strategy;

/**
 * Shared loan math used across strategies.
 */
public final class FinanceMath {

    private FinanceMath() {}

    /**
     * Standard EMI formula: P * r * (1+r)^n / ((1+r)^n - 1)
     * @param principal      loan principal
     * @param annualRate     decimal annual rate (e.g., 0.16 for 16%)
     * @param months         tenure in months
     */
    public static double emi(double principal, double annualRate, int months) {
        if (months <= 0) return 0;
        double r = annualRate / 12.0;
        if (r == 0) return principal / months;
        double pow = Math.pow(1 + r, months);
        return principal * r * pow / (pow - 1);
    }

    /**
     * Months to clear a balance with a fixed payment, accounting for compounding interest.
     * Returns Integer.MAX_VALUE if payment doesn't even cover interest.
     */
    public static int monthsToClear(double balance, double annualRate, double monthlyPayment) {
        if (balance <= 0) return 0;
        double r = annualRate / 12.0;
        double monthlyInterest = balance * r;
        if (monthlyPayment <= monthlyInterest + 1) return Integer.MAX_VALUE;
        if (r == 0) return (int) Math.ceil(balance / monthlyPayment);
        double n = -Math.log(1 - (balance * r / monthlyPayment)) / Math.log(1 + r);
        return (int) Math.ceil(n);
    }

    /**
     * Total interest paid if `monthlyPayment` is paid until cleared.
     */
    public static double totalInterest(double balance, double annualRate, double monthlyPayment) {
        int n = monthsToClear(balance, annualRate, monthlyPayment);
        if (n == Integer.MAX_VALUE) return Double.POSITIVE_INFINITY;
        return (monthlyPayment * n) - balance;
    }
}
