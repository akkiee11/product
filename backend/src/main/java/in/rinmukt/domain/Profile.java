package in.rinmukt.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    private double monthlyIncome;
    private double monthlyExpenses;
    private int age;
    private int dependents;
    private int cityTier;
    private int cibilScore;
    private boolean hasDefault;
    private boolean canIncreaseIncome;
    private double assetsValue;
    private String debtOrigin;

    @Builder.Default
    private List<Debt> debts = new ArrayList<>();

    public double disposableIncome() {
        return monthlyIncome - monthlyExpenses;
    }

    public double totalDebt() {
        return debts.stream().mapToDouble(Debt::getOutstanding).sum();
    }

    public double monthlyDebtService() {
        return debts.stream().mapToDouble(Debt::getEmi).sum();
    }

    public double currentSurplus() {
        return disposableIncome() - monthlyDebtService();
    }

    public double toxicDebt() {
        return debts.stream()
                .filter(d -> d.getInterestRate() > 0.25)
                .mapToDouble(Debt::getOutstanding)
                .sum();
    }

    public double debtToIncomeRatio() {
        return monthlyIncome == 0 ? 0 : monthlyDebtService() / monthlyIncome;
    }
}
