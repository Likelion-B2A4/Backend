package B2A4.demoday.domain.doctor.entity;

import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.common.BaseEntity;
import B2A4.demoday.domain.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Doctor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "specialty", nullable = false)
    private String specialty;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "last_treatment")
    private String lastTreatment;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "qr_generated_at")
    private LocalDateTime qrGeneratedAt;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoom> chatRooms = new ArrayList<>();
}
