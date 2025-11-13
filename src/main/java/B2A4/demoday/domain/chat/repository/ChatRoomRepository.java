package B2A4.demoday.domain.chat.repository;

import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.doctor.entity.Doctor;
import B2A4.demoday.domain.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}