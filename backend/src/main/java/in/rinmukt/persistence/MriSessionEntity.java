package in.rinmukt.persistence;

import in.rinmukt.domain.Report;
import in.rinmukt.dto.MriRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mri_sessions")
@Getter
@Setter
public class MriSessionEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "lead_id")
    private UUID leadId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_json", nullable = false, columnDefinition = "jsonb")
    private MriRequest requestJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_json", columnDefinition = "jsonb")
    private Report reportJson;

    @Column(name = "health_score")
    private Integer healthScore;

    @Column(name = "recommended_path", length = 50)
    private String recommendedPath;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
