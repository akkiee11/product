package in.rinmukt.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MriSessionRepository extends JpaRepository<MriSessionEntity, UUID> {

    /**
     * Sessions ready for the day-7 follow-up: created at least 7 days ago,
     * we successfully sent the welcome (so we know the email is reachable),
     * and we haven't sent the follow-up yet.
     */
    @Query("""
            SELECT s FROM MriSessionEntity s
            WHERE s.welcomeSentAt IS NOT NULL
              AND s.day7SentAt IS NULL
              AND s.email IS NOT NULL
              AND s.createdAt <= :before
            """)
    List<MriSessionEntity> findDay7Due(@Param("before") OffsetDateTime before);
}
