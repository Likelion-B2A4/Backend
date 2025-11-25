// HospitalRepository.java
package B2A4.demoday.domain.hospital.repository;

import B2A4.demoday.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);


    // POINT(경도,위도) 순서 주의
    @Query(value = "SELECT h.id, h.name, h.latitude, h.longitude, " +
            "ST_Distance_Sphere(POINT(h.longitude, h.latitude), POINT(:myLng, :myLat)) AS distance " +
            "FROM hospitals h " +
            "WHERE h.latitude IS NOT NULL AND h.longitude IS NOT NULL " +
            "AND ST_Distance_Sphere(POINT(h.longitude, h.latitude), POINT(:myLng, :myLat)) <= :radius " +
            "ORDER BY distance ASC",
            nativeQuery = true)
    List<Object[]> findNearbyHospitals(
            @Param("myLat") double myLat,
            @Param("myLng") double myLng,
            @Param("radius") double radius
    );
}