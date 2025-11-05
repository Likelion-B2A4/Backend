// PatientRepository.java
package B2A4.demoday.domain.patient.repository;

import B2A4.demoday.domain.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}