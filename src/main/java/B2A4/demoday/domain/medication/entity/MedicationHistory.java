package B2A4.demoday.domain.medication.entity;

import B2A4.demoday.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication_history")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MedicationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicationRecord medicationRecord;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "period", nullable = false)
    private String period;

    @Column(name = "taken", nullable = false)
    private Boolean taken;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    public void updateTaken(Boolean taken) {
        this.taken = taken;
        this.takenAt = taken ? LocalDateTime.now() : null;
    }

    public void updateMedicationRecord(MedicationRecord record) {
        this.medicationRecord = record;
    }
}