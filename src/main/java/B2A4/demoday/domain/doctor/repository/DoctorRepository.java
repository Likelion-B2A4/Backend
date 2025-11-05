// DoctorRepository.java
package B2A4.demoday.domain.doctor.repository;

import B2A4.demoday.domain.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByQrCode(String qrCode);
    Optional<Doctor> findByNameAndHospital_Id(String name, Long hospitalId);
}