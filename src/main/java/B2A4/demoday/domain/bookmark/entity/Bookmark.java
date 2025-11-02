package B2A4.demoday.domain.bookmark.entity;

import B2A4.demoday.domain.common.BaseEntity;
import B2A4.demoday.domain.hospital.entity.Hospital;
import B2A4.demoday.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookmarks")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Bookmark extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
}
