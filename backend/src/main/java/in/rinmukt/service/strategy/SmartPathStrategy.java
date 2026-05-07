package in.rinmukt.service.strategy;

import in.rinmukt.domain.Debt;
import in.rinmukt.domain.DebtType;
import in.rinmukt.domain.MonthlySnapshot;
import in.rinmukt.domain.PathResult;
import in.rinmukt.domain.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The recommended path:
 *   1. Convert all revolving credit-card outstanding to EMI loans at 16% / 36 mo.
 *   2. Apply contracted EMIs each month.
 *   3. Direct ALL surplus cashflow to highest-rate active debt (avalanche).
 *   4. As debts close, surplus snowballs.
 *
 * Does NOT trigger settlement → CIBIL is preserved.
 */
@Component
public class SmartPathStrategy implements DebtStrategy {

    private static final double CC_CONVERSION_RATE = 0.16;
    private static final int CC_CONVERSION_TENURE = 36;
    private static final int MAX_MONTHS = 240;

    @Override public String pathId() { return "SMART_PATH"; }

    @Override
    public PathResult calculate(Profile profile) {
        List<Debt> active = new ArrayList<>();
        for (Debt d : profile.getDebts()) {
            Debt copy = d.copy();
            if (copy.getType() == DebtType.CREDIT_CARD) {
                copy.setType(DebtType.CC_EMI);
                copy.setInterestRate(CC_CONVERSION_RATE);
                copy.setEmi(FinanceMath.emi(copy.getOutstanding(), CC_CONVERSION_RATE, CC_CONVERSION_TENURE));
                copy.setMonthsLeft(CC_CONVERSION_TENURE);
            }
            active.add(copy);
        }

        List<MonthlySnapshot> timeline = new ArrayList<>();
        double totalInterest = 0;
        int month = 0;

        while (anyActive(active) && month < MAX_MONTHS) {
            month++;

            // Step 1: pay contracted EMIs (interest first, then principal)
            double monthInterest = 0;
            double monthPrincipal = 0;
            double emiOutflow = 0;
            List<String> closedThisMonth = new ArrayList<>();

            for (Debt d : active) {
                if (d.getOutstanding() <= 0) continue;
                double interest = d.monthlyInterest();
                double payment = Math.min(d.getEmi(), d.getOutstanding() + interest);
                d.setOutstanding(d.getOutstanding() - (payment - interest));
                monthInterest += interest;
                monthPrincipal += (payment - interest);
                emiOutflow += payment;
                if (d.getOutstanding() < 1) {
                    d.setOutstanding(0);
                    closedThisMonth.add(d.getLender());
                }
            }

            // Step 2: compute surplus available for avalanche prepayment
            double availableForDebt = profile.disposableIncome() - emiOutflow;
            double prepayment = 0;

            if (availableForDebt > 0) {
                Debt target = highestRateActive(active);
                if (target != null) {
                    prepayment = Math.min(availableForDebt, target.getOutstanding());
                    target.setOutstanding(target.getOutstanding() - prepayment);
                    if (target.getOutstanding() < 1) {
                        target.setOutstanding(0);
                        closedThisMonth.add(target.getLender() + " (early)");
                    }
                }
            }

            totalInterest += monthInterest;

            timeline.add(MonthlySnapshot.builder()
                    .month(month)
                    .totalDebtRemaining(totalRemaining(active))
                    .monthlyOutflow(emiOutflow + prepayment)
                    .interestPaid(monthInterest)
                    .principalPaid(monthPrincipal + prepayment)
                    .prepayment(prepayment)
                    .focusDebt(focusName(active))
                    .debtsClosed(String.join(", ", closedThisMonth))
                    .build());
        }

        double cashOut = profile.totalDebt() + totalInterest;
        int cibilAfter = estimateCibilImprovement(profile, month);

        return PathResult.builder()
                .pathId(pathId())
                .name("Smart Path (CC→EMI + Avalanche)")
                .totalCashOut(cashOut)
                .totalInterestPaid(totalInterest)
                .monthsToFreedom(month)
                .cibilAfter(cibilAfter)
                .summary("Convert toxic CC debt to 16% EMI, attack highest rate first, preserve CIBIL.")
                .timeline(timeline)
                .build();
    }

    private Debt highestRateActive(List<Debt> debts) {
        return debts.stream()
                .filter(d -> d.getOutstanding() > 0)
                .max(Comparator.comparingDouble(Debt::getInterestRate))
                .orElse(null);
    }

    private boolean anyActive(List<Debt> debts) {
        return debts.stream().anyMatch(d -> d.getOutstanding() > 0);
    }

    private double totalRemaining(List<Debt> debts) {
        return debts.stream().mapToDouble(Debt::getOutstanding).sum();
    }

    private String focusName(List<Debt> debts) {
        Debt t = highestRateActive(debts);
        return t == null ? "" : t.getLender();
    }

    /**
     * Rough CIBIL projection: utilization drop (CC→EMI) + on-time history → +80-110 pts over 18-24 mo.
     */
    private int estimateCibilImprovement(Profile p, int monthsToFreedom) {
        int base = p.getCibilScore() == 0 ? 700 : p.getCibilScore();
        int boost = 60;
        if (monthsToFreedom <= 36) boost += 30;
        if (!p.isHasDefault()) boost += 20;
        return Math.min(820, base + boost);
    }
}
