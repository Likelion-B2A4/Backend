package B2A4.demoday.domain.medication.repository;

import B2A4.demoday.domain.medication.entity.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Long> {
    void deleteAllByMedicationRecordId(Long medicationRecordId);
}