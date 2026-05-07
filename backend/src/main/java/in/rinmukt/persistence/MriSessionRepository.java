package in.rinmukt.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MriSessionRepository extends JpaRepository<MriSessionEntity, UUID> {
}
