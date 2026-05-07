package in.rinmukt.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Debt {
    private String id;
    private DebtType type;
    private String lender;
    private double outstanding;       // Principal owed
    private double interestRate;      // Annual decimal (0.42 for 42%)
    private double emi;               // Monthly EMI or min due
    private int monthsLeft;           // Tenure remaining; 0 = revolving

    public Debt copy() {
        return Debt.builder()
                .id(id).type(type).lender(lender)
                .outstanding(outstanding).interestRate(interestRate)
                .emi(emi).monthsLeft(monthsLeft)
                .build();
    }

    public double monthlyInterest() {
        return outstanding * interestRate / 12.0;
    }
}
