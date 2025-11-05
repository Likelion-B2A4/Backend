// HospitalRepository.java
package B2A4.demoday.domain.hospital.repository;

import B2A4.demoday.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}