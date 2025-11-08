// DoctorRepository.java
package B2A4.demoday.domain.doctor.repository;

import B2A4.demoday.domain.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    // 병원별 의사 목록 조회 (fetch join)
    @Query("SELECT d FROM Doctor d JOIN FETCH d.hospital WHERE d.hospital.id = :hospitalId")
    List<Doctor> findAllByHospital_Id(@Param("hospitalId") Long hospitalId);

    Optional<Doctor> findByQrCode(String qrCode);
    Optional<Doctor> findByNameAndHospital_Id(String name, Long hospitalId);
}