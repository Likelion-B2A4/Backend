package B2A4.demoday.domain.bookmark.repository;

import B2A4.demoday.domain.bookmark.entity.Bookmark;
import B2A4.demoday.domain.hospital.entity.Hospital;
import B2A4.demoday.domain.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByPatient(Patient patient);
    Optional<Bookmark> findByPatientAndHospital(Patient patient, Hospital hospital);
}