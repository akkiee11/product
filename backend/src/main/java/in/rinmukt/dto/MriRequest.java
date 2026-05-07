package in.rinmukt.dto;

import in.rinmukt.domain.Profile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MriRequest(
        @NotNull @DecimalMin("0") Double monthlyIncome,
        @NotNull @DecimalMin("0") Double monthlyExpenses,
        Integer age,
        Integer dependents,
        Integer cityTier,
        Integer cibilScore,
        Boolean hasDefault,
        Boolean canIncreaseIncome,
        Double assetsValue,
        String debtOrigin,
        String email,
        String phone,
        @NotEmpty @Valid List<DebtRequest> debts
) {
    public Profile toProfile() {
        return Profile.builder()
                .monthlyIncome(monthlyIncome)
                .monthlyExpenses(monthlyExpenses)
                .age(age == null ? 0 : age)
                .dependents(dependents == null ? 0 : dependents)
                .cityTier(cityTier == null ? 1 : cityTier)
                .cibilScore(cibilScore == null ? 0 : cibilScore)
                .hasDefault(Boolean.TRUE.equals(hasDefault))
                .canIncreaseIncome(Boolean.TRUE.equals(canIncreaseIncome))
                .assetsValue(assetsValue == null ? 0 : assetsValue)
                .debtOrigin(debtOrigin)
                .debts(debts.stream().map(DebtRequest::toDomain).toList())
                .build();
    }
}
