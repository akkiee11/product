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
public class Report {
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
