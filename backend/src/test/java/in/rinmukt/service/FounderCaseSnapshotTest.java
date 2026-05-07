package in.rinmukt.service;

import in.rinmukt.domain.Debt;
import in.rinmukt.domain.DebtType;
import in.rinmukt.domain.PathResult;
import in.rinmukt.domain.Profile;
import in.rinmukt.domain.Report;
import in.rinmukt.service.strategy.AggressiveSmartPathStrategy;
import in.rinmukt.service.strategy.DiySettlementStrategy;
import in.rinmukt.service.strategy.FullSettlementStrategy;
import in.rinmukt.service.strategy.SmartPathStrategy;
import in.rinmukt.service.strategy.StatusQuoStrategy;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Not assertive — prints engine output for the founder's case.
 * Useful when iterating on tuning parameters. Run with: mvn test -Dtest=FounderCaseSnapshotTest
 */
class FounderCaseSnapshotTest {

    @Test
    void printReport() {
        SmartPathStrategy smart = new SmartPathStrategy();
        CalculationEngine engine = new CalculationEngine(List.of(
                new StatusQuoStrategy(),
                smart,
                new AggressiveSmartPathStrategy(smart),
                new DiySettlementStrategy(),
                new FullSettlementStrategy()
        ));

        Profile p = Profile.builder()
                .monthlyIncome(106_000).monthlyExpenses(20_000)
                .age(26).cibilScore(651)
                .debts(List.of(
                        d("HDFC CC",  DebtType.CREDIT_CARD,  1_96_231, 0.42, 9_800,  0),
                        d("ICICI CC", DebtType.CREDIT_CARD,  1_09_902, 0.42, 5_500,  0),
                        d("Axis CC",  DebtType.CREDIT_CARD,  1_35_218, 0.42, 6_800,  0),
                        d("SBI CC",   DebtType.CREDIT_CARD,    35_441, 0.42, 1_800,  0),
                        d("SMFG PL",  DebtType.PERSONAL_LOAN,  29_720, 0.33,  4_796, 6),
                        d("HDFC PL",  DebtType.PERSONAL_LOAN, 13_06_000, 0.108, 34_200, 47),
                        d("ICICI PL", DebtType.PERSONAL_LOAN, 3_42_631, 0.1255, 11_332, 35),
                        d("Navi PL",  DebtType.PERSONAL_LOAN, 1_06_000, 0.2416, 6_600, 20)))
                .build();

        Report r = engine.run(p);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("en", "IN"));

        System.out.println("================================================================");
        System.out.printf("HEALTH SCORE: %d / 100  (%s)%n", r.getHealthScore(), r.getHealthLabel());
        System.out.printf("Total debt:       ₹%s%n", fmt.format((long) r.getTotalDebt()));
        System.out.printf("Monthly outflow:  ₹%s%n", fmt.format((long) r.getMonthlyOutflow()));
        System.out.printf("DTI:              %.0f%%%n", r.getDebtToIncomePercent());
        System.out.println("Concern: " + r.getPrimaryConcern());
        System.out.println();
        System.out.println("Recommended path: " + r.getRecommendedPathId());
        System.out.println();
        System.out.println("------------------ ALL PATHS (ranked) ------------------");
        for (PathResult path : r.getPaths()) {
            System.out.printf("#%d %s%n", path.getRank(), path.getName());
            System.out.printf("    cash out:   ₹%s%n", fmt.format((long) path.getTotalCashOut()));
            System.out.printf("    interest:   ₹%s%n", fmt.format((long) path.getTotalInterestPaid()));
            System.out.printf("    fees:       ₹%s%n", fmt.format((long) path.getFeesPaid()));
            System.out.printf("    tax:        ₹%s%n", fmt.format((long) path.getTaxExposure()));
            System.out.printf("    months:     %d  (%.1f yrs)%n", path.getMonthsToFreedom(), path.yearsToFreedom());
            System.out.printf("    cibil:      %d%n", path.getCibilAfter());
            if (path.getWarning() != null) System.out.println("    ⚠ " + path.getWarning());
            System.out.println();
        }
        System.out.println("================================================================");
    }

    private static Debt d(String lender, DebtType t, double bal, double rate, double emi, int months) {
        return Debt.builder()
                .lender(lender).type(t).outstanding(bal)
                .interestRate(rate).emi(emi).monthsLeft(months).build();
    }
}
