package in.rinmukt.service.strategy;

import in.rinmukt.domain.PathResult;
import in.rinmukt.domain.Profile;
import org.springframework.stereotype.Component;

/**
 * Smart Path + assumed income increase of ₹20,000/month after 6 months
 * (job switch, side income). Realistic for any senior salaried professional.
 */
@Component
public class AggressiveSmartPathStrategy implements DebtStrategy {

    private static final double INCOME_BUMP = 20_000;
    private static final int BUMP_AFTER_MONTHS = 6;

    private final SmartPathStrategy smart;

    public AggressiveSmartPathStrategy(SmartPathStrategy smart) {
        this.smart = smart;
    }

    @Override public String pathId() { return "AGGRESSIVE_SMART_PATH"; }

    @Override
    public PathResult calculate(Profile profile) {
        // Approximation: simulate by raising disposable income across the run.
        // (For V0, simple weighted bump suffices; V1 can do period-aware modelling.)
        Profile bumped = Profile.builder()
                .monthlyIncome(profile.getMonthlyIncome() + INCOME_BUMP)
                .monthlyExpenses(profile.getMonthlyExpenses())
                .age(profile.getAge())
                .dependents(profile.getDependents())
                .cityTier(profile.getCityTier())
                .cibilScore(profile.getCibilScore())
                .hasDefault(profile.isHasDefault())
                .canIncreaseIncome(true)
                .assetsValue(profile.getAssetsValue())
                .debtOrigin(profile.getDebtOrigin())
                .debts(profile.getDebts())
                .build();

        PathResult base = smart.calculate(bumped);

        return PathResult.builder()
                .pathId(pathId())
                .name("Aggressive Smart Path (with ₹20K income boost)")
                .totalCashOut(base.getTotalCashOut())
                .totalInterestPaid(base.getTotalInterestPaid())
                .monthsToFreedom(base.getMonthsToFreedom() + BUMP_AFTER_MONTHS / 2) // small adjustment
                .cibilAfter(Math.min(820, base.getCibilAfter() + 10))
                .summary("Same plan, accelerated by ₹20K/mo income increase (job switch / freelance).")
                .timeline(base.getTimeline())
                .build();
    }
}
