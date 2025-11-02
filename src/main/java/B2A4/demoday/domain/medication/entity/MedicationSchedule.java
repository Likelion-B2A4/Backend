package B2A4.demoday.domain.medication.entity;

import B2A4.demoday.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "medication_schedules")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MedicationSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicationRecord medicationRecord;

    // 간단하게 string 으로 저장!
    @Column(name = "period")
    private String period;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "enabled")
    private Boolean enabled;
}
