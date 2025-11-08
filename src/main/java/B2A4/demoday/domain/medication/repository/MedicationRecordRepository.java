package B2A4.demoday.domain.medication.repository;

import B2A4.demoday.domain.medication.entity.MedicationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRecordRepository extends JpaRepository<MedicationRecord, Long> {
    List<MedicationRecord> findByPatient_Id(Long patientId);
}