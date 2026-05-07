package in.rinmukt.service.strategy;

import in.rinmukt.domain.Debt;
import in.rinmukt.domain.PathResult;
import in.rinmukt.domain.Profile;
import org.springframework.stereotype.Component;

/**
 * "Freed-style" full settlement of ALL unsecured debt.
 * We model this honestly — including the costs Freed downplays:
 *   - 18% platform fee
 *   - ₹3,999 evaluation fee
 *   - 30% income tax on waived debt (Sec 56(2)(x))
 *   - CIBIL crashes to 350-450 for 7 years
 *   - Non-settleable debts (Navi etc) still run full term
 */
@Component
public class FullSettlementStrategy implements DebtStrategy {

    private static final double SETTLEMENT_RATIO = 0.45;      // 45% of principal goes to creditor
    private static final double PLATFORM_FEE_RATIO = 0.18;    // 18% of debt to platform
    private static final double EVALUATION_FEE = 3_999;
    private static final double TAX_SLAB = 0.30;
    private static final int PROGRAM_MONTHS = 84;

    @Override public String pathId() { return "FULL_SETTLEMENT"; }

    @Override
    public PathResult calculate(Profile profile) {
        double settleableDebt = 0;
        double nonSettleableDebt = 0;
        double nonSettleableInterest = 0;

        for (Debt d : profile.getDebts()) {
            if (d.getType().isSettleable()) {
                settleableDebt += d.getOutstanding();
            } else {
                nonSettleableDebt += d.getOutstanding();
                int n = Math.max(d.getMonthsLeft(), 12);
                nonSettleableInterest += Math.max(0, d.getEmi() * n - d.getOutstanding());
            }
        }

        double settlementToCreditors = settleableDebt * SETTLEMENT_RATIO;
        double platformFee = settleableDebt * PLATFORM_FEE_RATIO;
        double waivedDebt = settleableDebt - settlementToCreditors;
        double tax = waivedDebt * TAX_SLAB;

        double cashOut = settlementToCreditors
                + platformFee
                + EVALUATION_FEE
                + tax
                + nonSettleableDebt
                + nonSettleableInterest;

        return PathResult.builder()
                .pathId(pathId())
                .name("Full Settlement (Freed-style)")
                .totalCashOut(cashOut)
                .totalInterestPaid(nonSettleableInterest)
                .feesPaid(platformFee + EVALUATION_FEE)
                .taxExposure(tax)
                .monthsToFreedom(PROGRAM_MONTHS)
                .cibilAfter(400)
                .summary("Stop paying creditors, save into program, settle in lump sum after 6-9 months.")
                .warning("CIBIL crashes to ~400 for 5-7 years. Tax bomb on waived debt. " +
                        "Recovery agent harassment. Possible Section 138 / civil suits. " +
                        "No new credit, home loan, or rental approvals till ~2033.")
                .build();
    }
}
