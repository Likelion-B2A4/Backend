package B2A4.demoday.domain.chat.entity;

import B2A4.demoday.domain.common.BaseEntity;
import B2A4.demoday.domain.doctor.entity.Doctor;
import B2A4.demoday.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // waiting / active / closed
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "qr_code", nullable = false)
    private String qrCode;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    // ai가 요약한 내용
    @Column(name = "diagnosis_summary", columnDefinition = "TEXT")
    private String diagnosisSummary;

    // ai가 요약한 내용
    @Column(name = "symptom_summary", columnDefinition = "TEXT")
    private String symptomSummary;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    // 상태 및 시간 업데이트 메서드
    public void updateStatus(String newStatus) {
        this.status = newStatus.toLowerCase();
    }

    public void updateFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
