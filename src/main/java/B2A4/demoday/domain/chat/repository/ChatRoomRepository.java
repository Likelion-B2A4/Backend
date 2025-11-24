package B2A4.demoday.domain.chat.repository;

import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.doctor.entity.Doctor;
import B2A4.demoday.domain.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByPatientIdAndFinishedAtBetween(Long patientId, LocalDateTime start, LocalDateTime end);

    List<ChatRoom> findAllByPatientIdAndFinishedAtBetween(Long patientId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM ChatRoom c JOIN FETCH c.patient WHERE c.doctor.id = :doctorId")
    List<ChatRoom> findAllByDoctorId(Long doctorId);

    List<ChatRoom> findAllByStatusAndStartedAtBefore(String status, LocalDateTime threshold);
}