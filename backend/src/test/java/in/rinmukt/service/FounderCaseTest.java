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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The founder's actual debt situation as a regression test.
 * If we ever break the engine, this test fails — and we know our user-zero is wrong.
 */
class FounderCaseTest {

    private final SmartPathStrategy smart = new SmartPathStrategy();
    private final CalculationEngine engine = new CalculationEngine(List.of(
            new StatusQuoStrategy(),
            smart,
            new AggressiveSmartPathStrategy(smart),
            new DiySettlementStrategy(),
            new FullSettlementStrategy()
    ));

    private Profile founderProfile() {
        return Profile.builder()
                .monthlyIncome(106_000)
                .monthlyExpenses(20_000)
                .age(26)
                .dependents(0)
                .cityTier(3)
                .cibilScore(651)
                .hasDefault(false)
                .canIncreaseIncome(true)
                .assetsValue(0)
                .debtOrigin("House construction + furniture")
                .debts(List.of(
                        debt("HDFC CC",     DebtType.CREDIT_CARD,   1_96_231, 0.42, 9_800,  0),
                        debt("ICICI CC",    DebtType.CREDIT_CARD,   1_09_902, 0.42, 5_500,  0),
                        debt("Axis CC",     DebtType.CREDIT_CARD,   1_35_218, 0.42, 6_800,  0),
                        debt("SBI CC",     DebtType.CREDIT_CARD,      35_441, 0.42, 1_800,  0),
                        debt("SMFG PL",    DebtType.PERSONAL_LOAN,    29_720, 0.33, 4_796,  6),
                        debt("HDFC PL",    DebtType.PERSONAL_LOAN, 13_06_000, 0.108, 34_200, 47),
                        debt("ICICI PL",   DebtType.PERSONAL_LOAN,  3_42_631, 0.1255, 11_332, 35),
                        debt("Navi PL",    DebtType.PERSONAL_LOAN,  1_06_000, 0.2416, 6_600, 20)
                ))
                .build();
    }

    private static Debt debt(String lender, DebtType t, double bal, double rate, double emi, int months) {
        return Debt.builder()
                .lender(lender).type(t)
                .outstanding(bal).interestRate(rate)
                .emi(emi).monthsLeft(months).build();
    }

    @Test
    void totalDebtMatchesReality() {
        Profile p = founderProfile();
        assertThat(p.totalDebt()).isCloseTo(22_61_143, org.assertj.core.data.Offset.offset(2.0));
    }

    @Test
    void diagnosesToxicDebt() {
        Profile p = founderProfile();
        // Toxic threshold is >25% APR. Qualifying: 4 CCs (42%) + SMFG (33%).
        // Navi at 24.16% is just under the threshold.
        assertThat(p.toxicDebt()).isGreaterThan(5_00_000);
    }

    @Test
    void healthScoreIsRiskyOrCritical() {
        Report r = engine.run(founderProfile());
        assertThat(r.getHealthScore()).isLessThanOrEqualTo(50);
        assertThat(r.getHealthLabel()).isIn("RISKY", "CRITICAL");
    }

    @Test
    void smartPathBeatsStatusQuo() {
        Report r = engine.run(founderProfile());
        PathResult smart = pathBy(r, "SMART_PATH");
        PathResult statusQuo = pathBy(r, "STATUS_QUO");

        assertThat(smart.getTotalCashOut()).isLessThan(statusQuo.getTotalCashOut());
        assertThat(smart.getMonthsToFreedom()).isLessThan(statusQuo.getMonthsToFreedom());
    }

    @Test
    void smartPathPreservesCibil() {
        Report r = engine.run(founderProfile());
        PathResult smart = pathBy(r, "SMART_PATH");
        assertThat(smart.getCibilAfter()).isGreaterThanOrEqualTo(700);
    }

    @Test
    void fullSettlementWarnsAboutCibilCrash() {
        Report r = engine.run(founderProfile());
        PathResult freed = pathBy(r, "FULL_SETTLEMENT");
        assertThat(freed.getCibilAfter()).isLessThanOrEqualTo(450);
        assertThat(freed.getTaxExposure()).isGreaterThan(2_00_000);
        assertThat(freed.getWarning()).contains("CIBIL");
    }

    @Test
    void smartPathIsRecommendedForFounder() {
        Report r = engine.run(founderProfile());
        // For someone CURRENT on payments with CIBIL 651, settlement should NOT be recommended
        assertThat(r.getRecommendedPathId()).isIn("SMART_PATH", "AGGRESSIVE_SMART_PATH");
    }

    @Test
    void smartPathFinishesWithinFourYears() {
        Report r = engine.run(founderProfile());
        PathResult smart = pathBy(r, "SMART_PATH");
        assertThat(smart.getMonthsToFreedom()).isLessThanOrEqualTo(48);
    }

    @Test
    void recommendationOrderIsDeterministic() {
        Report r1 = engine.run(founderProfile());
        Report r2 = engine.run(founderProfile());
        assertThat(r1.getRecommendedPathId()).isEqualTo(r2.getRecommendedPathId());
    }

    private PathResult pathBy(Report r, String id) {
        return r.getPaths().stream()
                .filter(p -> p.getPathId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
