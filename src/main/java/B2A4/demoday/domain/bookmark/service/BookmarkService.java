package B2A4.demoday.domain.bookmark.service;

import B2A4.demoday.domain.bookmark.entity.Bookmark;
import B2A4.demoday.domain.bookmark.repository.BookmarkRepository;
import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.hospital.entity.Hospital;
import B2A4.demoday.domain.hospital.repository.HospitalRepository;
import B2A4.demoday.domain.patient.entity.Patient;
import B2A4.demoday.domain.patient.repository.PatientRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;

    // 즐겨찾기 추가
    public CommonResponse<?> addBookmark(Long userId, Long hospitalId) {
        Patient patient = patientRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("환자를 찾을 수 없습니다."));
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new NoSuchElementException("병원을 찾을 수 없습니다."));

        // 이미 존재하면 예외
        bookmarkRepository.findByPatientAndHospital(patient, hospital)
                .ifPresent(b -> { throw new IllegalArgumentException("이미 즐겨찾기한 병원입니다."); });

        Bookmark bookmark = Bookmark.builder()
                .patient(patient)
                .hospital(hospital)
                .build();

        bookmarkRepository.save(bookmark);

        return CommonResponse.success("즐겨찾기에 추가되었습니다.");
    }

    // 즐겨찾기 삭제
    public CommonResponse<?> removeBookmark(Long userId, Long hospitalId) {
        Patient patient = patientRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("환자를 찾을 수 없습니다."));
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new NoSuchElementException("병원을 찾을 수 없습니다."));

        Bookmark bookmark = bookmarkRepository.findByPatientAndHospital(patient, hospital)
                .orElseThrow(() -> new NoSuchElementException("즐겨찾기 내역이 존재하지 않습니다."));

        bookmarkRepository.delete(bookmark);

        return CommonResponse.success("즐겨찾기에서 삭제되었습니다.");
    }

    // 즐겨찾기 조회
    public CommonResponse<?> getBookmarks(Long userId) {
        Patient patient = patientRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("환자를 찾을 수 없습니다."));

        List<Bookmark> bookmarks = bookmarkRepository.findByPatient(patient);

        ObjectMapper objectMapper = new ObjectMapper();

        List<Map<String, Object>> hospitalList = bookmarks.stream()
                .map(b -> {
                    Hospital hospital = b.getHospital();
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("id", hospital.getId());
                    map.put("name", hospital.getName());

                    // specialties JSON 문자열을 List로 변환
                    List<String> specialtiesList = new ArrayList<>();
                    try {
                        if (hospital.getSpecialties() != null) {
                            specialtiesList = objectMapper.readValue(
                                    hospital.getSpecialties(),
                                    new TypeReference<List<String>>() {}
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    map.put("specialties", specialtiesList);

                    map.put("imageUrl", hospital.getImageUrl());
                    map.put("address", hospital.getAddress());

                    // operatingHours가 null일 경우 빈 리스트 반환
                    List<Map<String, Object>> operatingHours = hospital.getOperatingHours() != null
                            ? hospital.getOperatingHours().stream()
                            .map(op -> {
                                Map<String, Object> opMap = new LinkedHashMap<>();
                                opMap.put("dayOfWeek", op.getDayOfWeek());
                                opMap.put("openTime", op.getOpenTime());
                                opMap.put("closeTime", op.getCloseTime());
                                opMap.put("breakStartTime", op.getBreakStartTime());
                                opMap.put("breakEndTime", op.getBreakEndTime());
                                opMap.put("isClosed", op.getIsClosed());
                                return opMap;
                            })
                            .collect(Collectors.toList())
                            : new ArrayList<>();
                    map.put("operatingHours", operatingHours);

                    map.put("bookmark", true);
                    map.put("contact", hospital.getContact());

                    return map;
                })
                .collect(Collectors.toList());

        // data 필드 통합
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalCount", hospitalList.size());
        response.put("hospitals", hospitalList);

        return CommonResponse.success(response, "즐겨찾기한 병원 조회 성공");
    }
}