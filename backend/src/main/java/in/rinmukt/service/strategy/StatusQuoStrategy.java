package in.rinmukt.service.strategy;

import in.rinmukt.domain.Debt;
import in.rinmukt.domain.MonthlySnapshot;
import in.rinmukt.domain.PathResult;
import in.rinmukt.domain.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * "Do nothing different" — credit cards keep revolving at 42%, EMIs continue.
 * Models worst-case: only minimum due on cards (which we approximate at 5% of balance).
 */
@Component
public class StatusQuoStrategy implements DebtStrategy {

    private static final int MAX_MONTHS = 240; // 20 yr cap
    private static final double CC_MIN_DUE_RATIO = 0.05;

    @Override public String pathId() { return "STATUS_QUO"; }

    @Override
    public PathResult calculate(Profile profile) {
        List<Debt> active = profile.getDebts().stream().map(Debt::copy).toList();
        List<MonthlySnapshot> timeline = new ArrayList<>();

        double totalInterest = 0;
        int month = 0;

        while (hasOpenDebt(active) && month < MAX_MONTHS) {
            month++;
            double monthInterest = 0;
            double monthPrincipal = 0;
            double monthOutflow = 0;

            for (Debt d : active) {
                if (d.getOutstanding() <= 0) continue;

                double interest = d.monthlyInterest();
                double payment = paymentFor(d);
                payment = Math.min(payment, d.getOutstanding() + interest);

                d.setOutstanding(d.getOutstanding() - (payment - interest));
                if (d.getOutstanding() < 1) d.setOutstanding(0);

                monthInterest += interest;
                monthPrincipal += (payment - interest);
                monthOutflow += payment;
            }

            totalInterest += monthInterest;

            if (month <= 60 || month % 6 == 0) {
                timeline.add(MonthlySnapshot.builder()
                        .month(month)
                        .totalDebtRemaining(totalRemaining(active))
                        .monthlyOutflow(monthOutflow)
                        .interestPaid(monthInterest)
                        .principalPaid(monthPrincipal)
                        .build());
            }
        }

        double cashOut = profile.totalDebt() + totalInterest;

        return PathResult.builder()
                .pathId(pathId())
                .name("Status Quo (keep paying minimums)")
                .totalCashOut(cashOut)
                .totalInterestPaid(totalInterest)
                .monthsToFreedom(month)
                .cibilAfter(Math.min(profile.getCibilScore(), 670))
                .summary("Pay only minimums on cards. Slow financial bleed.")
                .warning(month >= MAX_MONTHS ? "At minimum payments you may never clear this debt." : null)
                .timeline(timeline)
                .build();
    }

    private double paymentFor(Debt d) {
        return switch (d.getType()) {
            case CREDIT_CARD -> Math.max(d.getOutstanding() * CC_MIN_DUE_RATIO, 200);
            default -> d.getEmi();
        };
    }

    private boolean hasOpenDebt(List<Debt> debts) {
        return debts.stream().anyMatch(d -> d.getOutstanding() > 0);
    }

    private double totalRemaining(List<Debt> debts) {
        return debts.stream().mapToDouble(Debt::getOutstanding).sum();
    }
}
