package in.rinmukt.dto;

import in.rinmukt.domain.Debt;
import in.rinmukt.domain.DebtType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DebtRequest(
        @NotNull DebtType type,
        String lender,
        @NotNull @DecimalMin("0") Double outstanding,
        @NotNull @DecimalMin("0") Double interestRate,    // decimal: 0.42 for 42%
        @NotNull @DecimalMin("0") Double emi,
        @NotNull Integer monthsLeft
) {
    public Debt toDomain() {
        return Debt.builder()
                .type(type)
                .lender(lender == null ? "" : lender)
                .outstanding(outstanding)
                .interestRate(interestRate)
                .emi(emi)
                .monthsLeft(monthsLeft)
                .build();
    }
}
