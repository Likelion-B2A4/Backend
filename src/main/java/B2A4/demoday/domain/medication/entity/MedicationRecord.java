package B2A4.demoday.domain.medication.entity;

import B2A4.demoday.domain.common.BaseEntity;
import B2A4.demoday.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medication_records")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MedicationRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "name")
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "alarm_enabled")
    private Boolean alarmEnabled;

    // 이 필드 필요하면 추가하세요!! 어차피 연쇄적으로 Schedule에서 볼 수 있길래 주석처리 했음
    /*@Column(name = "alarm_times", columnDefinition = "JSON")
    private String alarmTimes;*/

    @Column(name = "days_of_week", columnDefinition = "JSON")
    private String daysOfWeek;

    @OneToMany(mappedBy = "medicationRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicationSchedule> schedules = new ArrayList<>();

    public void updateName(String name) { this.name = name; }
    public void updateStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void updateEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void updateAlarmEnabled(Boolean alarmEnabled) { this.alarmEnabled = alarmEnabled; }
    public void updateDaysOfWeek(String daysOfWeekJson) { this.daysOfWeek = daysOfWeekJson; }
}
