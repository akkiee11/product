package in.rinmukt.service;

import in.rinmukt.domain.PathResult;
import in.rinmukt.domain.Profile;
import in.rinmukt.domain.Report;
import in.rinmukt.service.strategy.DebtStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CalculationEngine {

    private final List<DebtStrategy> strategies;

    public CalculationEngine(List<DebtStrategy> strategies) {
        this.strategies = strategies;
    }

    public Report run(Profile profile) {
        List<PathResult> results = new ArrayList<>();
        for (DebtStrategy s : strategies) {
            results.add(s.calculate(profile));
        }

        // Rank by total cash out + soft penalty for cibil damage and tax
        results.sort(Comparator.comparingDouble(this::scoreCost));
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }
        results.get(0).setRecommended(true);
        String recommendedId = results.get(0).getPathId();

        int healthScore = healthScore(profile);

        return Report.builder()
                .healthScore(healthScore)
                .healthLabel(label(healthScore))
                .primaryConcern(primaryConcern(profile))
                .totalDebt(profile.totalDebt())
                .monthlyOutflow(profile.monthlyDebtService())
                .debtToIncomePercent(profile.debtToIncomeRatio() * 100)
                .recommendedPathId(recommendedId)
                .paths(results)
                .build();
    }

    /**
     * Scoring: dominant signal is real cash out, but we penalise paths that
     * destroy CIBIL or trigger tax (because users underestimate those costs).
     */
    private double scoreCost(PathResult p) {
        double cibilPenalty = (750 - p.getCibilAfter()) * 5_000;       // ₹5K per CIBIL point lost
        double taxPenalty = p.getTaxExposure() * 1.0;                  // tax counted at face
        return p.getTotalCashOut() + Math.max(0, cibilPenalty) + taxPenalty;
    }

    private int healthScore(Profile p) {
        int score = 100;
        double dti = p.debtToIncomeRatio();
        if (dti > 0.7) score -= 35;
        else if (dti > 0.5) score -= 20;
        else if (dti > 0.3) score -= 10;

        double toxic = p.totalDebt() == 0 ? 0 : p.toxicDebt() / p.totalDebt();
        score -= (int) (toxic * 30);

        if (p.currentSurplus() < 0) score -= 25;
        else if (p.currentSurplus() < p.getMonthlyIncome() * 0.05) score -= 15;

        if (p.getCibilScore() > 0) {
            if (p.getCibilScore() < 600) score -= 15;
            else if (p.getCibilScore() < 700) score -= 8;
        }
        if (p.isHasDefault()) score -= 20;

        return Math.max(0, score);
    }

    private String label(int s) {
        if (s >= 75) return "HEALTHY";
        if (s >= 50) return "MANAGEABLE";
        if (s >= 30) return "RISKY";
        return "CRITICAL";
    }

    private String primaryConcern(Profile p) {
        if (p.toxicDebt() > p.totalDebt() * 0.15) {
            return String.format("₹%,.0f stuck in high-interest debt (>25%% APR). " +
                    "This is your bleeding wound — fix this first.", p.toxicDebt());
        }
        if (p.debtToIncomeRatio() > 0.6) {
            return "Debt service is eating " +
                    String.format("%.0f%%", p.debtToIncomeRatio() * 100) +
                    " of your income. Cashflow is the immediate threat.";
        }
        if (p.currentSurplus() < 1000) {
            return "Zero financial buffer. One emergency could trigger a default cascade.";
        }
        return "Manageable load — focus on accelerating payoff to free up cashflow.";
    }
}
