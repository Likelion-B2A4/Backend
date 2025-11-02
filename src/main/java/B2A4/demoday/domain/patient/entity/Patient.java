package B2A4.demoday.domain.patient.entity;

import B2A4.demoday.domain.bookmark.entity.Bookmark;
import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.common.BaseEntity;
import B2A4.demoday.domain.medication.entity.MedicationRecord;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Patient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "login_id", nullable = false)
    private String loginId;

    // 암호화 후 저장 필요 !
    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicationRecord> medicationRecords = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoom> chatRooms = new ArrayList<>();
}
