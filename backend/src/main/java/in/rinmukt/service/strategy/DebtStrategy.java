package in.rinmukt.service.strategy;

import in.rinmukt.domain.PathResult;
import in.rinmukt.domain.Profile;

public interface DebtStrategy {
    String pathId();
    PathResult calculate(Profile profile);
}
