package B2A4.demoday.domain.hospital.entity;

import B2A4.demoday.domain.common.BaseEntity;
import B2A4.demoday.domain.doctor.entity.Doctor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hospitals")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Hospital extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;

    // JSON 배열
    @Column(name = "specialties", columnDefinition = "JSON")
    private String specialties;

    @Column(name = "contact")
    private String contact;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "login_id", nullable = false)
    private String loginId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HospitalOperatingHours> operatingHours = new ArrayList<>();
}
