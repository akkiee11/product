package in.rinmukt.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySnapshot {
    private int month;
    private double totalDebtRemaining;
    private double monthlyOutflow;
    private double interestPaid;
    private double principalPaid;
    private double prepayment;
    private String debtsClosed;       // Comma-separated lender names that closed this month
    private String focusDebt;         // Which debt is being attacked
}
