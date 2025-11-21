package B2A4.demoday.domain.medication.repository;

import B2A4.demoday.domain.medication.entity.MedicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicationHistoryRepository extends JpaRepository<MedicationHistory, Long> {
    Optional<MedicationHistory> findByMedicationRecord_IdAndDateAndPeriod(
            Long recordId,
            LocalDate date,
            String period
    );

    List<MedicationHistory> findAllByMedicationRecord_IdAndDate(
            Long recordId,
            LocalDate date
    );
}