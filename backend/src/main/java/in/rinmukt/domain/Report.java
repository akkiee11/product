package in.rinmukt.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID id;                     // set after persistence; null in dev/non-persisted

    private int healthScore;             // 0-100
    private String healthLabel;          // CRITICAL / RISKY / MANAGEABLE / HEALTHY
    private String primaryConcern;       // The biggest issue named
    private double totalDebt;
    private double monthlyOutflow;
    private double debtToIncomePercent;
    private String recommendedPathId;

    @Builder.Default
    private List<PathResult> paths = new ArrayList<>();

    // Filled by AI service
    private String diagnosisSummary;
    private String goodNews;
    private String reasoningForRecommendation;

    @Builder.Default
    private List<String> firstThreeActions = new ArrayList<>();

    @Builder.Default
    private List<String> phoneScripts = new ArrayList<>();

    private String motivationalClose;
}
