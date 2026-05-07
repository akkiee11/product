package in.rinmukt.service.strategy;

import in.rinmukt.domain.Debt;
import in.rinmukt.domain.PathResult;
import in.rinmukt.domain.Profile;
import org.springframework.stereotype.Component;

/**
 * Selectively settle ONLY high-interest unsecured debts (CCs) at 60% haircut,
 * keep paying secured/low-rate loans normally.
 *
 * Risk: temporary CIBIL hit (~580), bank pushback. Lower risk than full Freed-style.
 */
@Component
public class DiySettlementStrategy implements DebtStrategy {

    private static final double SETTLEMENT_RATIO = 0.60;       // pay 60% of CC
    private static final int SETTLEMENT_TIME_MONTHS = 9;        // build pot + negotiate
    private static final double TAX_SLAB = 0.30;

    @Override public String pathId() { return "DIY_SETTLEMENT"; }

    @Override
    public PathResult calculate(Profile profile) {
        double settledPrincipal = 0;
        double settlementCash = 0;

        for (Debt d : profile.getDebts()) {
            if (d.getType().isSettleable() && d.getInterestRate() > 0.30) {
                settledPrincipal += d.getOutstanding();
                settlementCash += d.getOutstanding() * SETTLEMENT_RATIO;
            }
        }

        double waivedDebt = settledPrincipal - settlementCash;
        double tax = waivedDebt * TAX_SLAB;

        // Non-settled debts continue paying interest normally
        double nonSettledRemainingInterest = 0;
        double nonSettledPrincipal = 0;
        for (Debt d : profile.getDebts()) {
            if (!(d.getType().isSettleable() && d.getInterestRate() > 0.30)) {
                int n = Math.max(d.getMonthsLeft(), 12);
                nonSettledPrincipal += d.getOutstanding();
                nonSettledRemainingInterest += d.getEmi() * n - d.getOutstanding();
            }
        }

        double cashOut = settlementCash + tax + nonSettledPrincipal + nonSettledRemainingInterest;

        return PathResult.builder()
                .pathId(pathId())
                .name("DIY Negotiated Settlement (CCs only)")
                .totalCashOut(cashOut)
                .totalInterestPaid(nonSettledRemainingInterest)
                .feesPaid(0)
                .taxExposure(tax)
                .monthsToFreedom(Math.max(SETTLEMENT_TIME_MONTHS,
                        profile.getDebts().stream().mapToInt(Debt::getMonthsLeft).max().orElse(60)))
                .cibilAfter(580)
                .summary("Negotiate settlement directly with CC issuers. Pay ~60%, save 40%.")
                .warning("CIBIL drops to ~580 for 2-3 years. Recovery agents likely. Tax bomb on waived debt.")
                .build();
    }
}
