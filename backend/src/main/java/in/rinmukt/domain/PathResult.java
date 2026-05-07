package in.rinmukt.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathResult {
    private String pathId;             // SMART_PATH, STATUS_QUO, etc.
    private String name;               // Display name
    private double totalCashOut;       // Cash user pays (interest + principal + fees + tax)
    private double totalInterestPaid;
    private double feesPaid;
    private double taxExposure;
    private int monthsToFreedom;
    private int cibilAfter;
    private int rank;                  // 1 = best for this user
    private boolean recommended;
    private String summary;            // 1-line description
    private String warning;            // Optional risk warning

    @Builder.Default
    private List<MonthlySnapshot> timeline = new ArrayList<>();

    public double yearsToFreedom() {
        return monthsToFreedom / 12.0;
    }
}
