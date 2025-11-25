package B2A4.demoday.domain.hospital.service;

import B2A4.demoday.domain.bookmark.repository.BookmarkRepository;
import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.common.JsonUtil;
import B2A4.demoday.domain.hospital.dto.request.HospitalSignupRequest;
import B2A4.demoday.domain.hospital.dto.request.HospitalLoginRequest;
import B2A4.demoday.domain.hospital.dto.request.HospitalUpdateRequest;
import B2A4.demoday.domain.hospital.dto.response.HospitalDetailResponse;
import B2A4.demoday.domain.hospital.dto.response.HospitalNearbyResponse;
import B2A4.demoday.domain.hospital.dto.response.HospitalSignupResponse;
import B2A4.demoday.domain.hospital.dto.response.HospitalLoginResponse;
import B2A4.demoday.domain.hospital.entity.Hospital;
import B2A4.demoday.domain.hospital.entity.HospitalOperatingHours;
import B2A4.demoday.domain.hospital.repository.HospitalRepository;
import B2A4.demoday.domain.patient.entity.Patient;
import B2A4.demoday.domain.patient.repository.PatientRepository;
import B2A4.demoday.global.jwt.JwtTokenProvider;
import B2A4.demoday.global.kakao.service.KakaoAddressService;
import B2A4.demoday.global.s3.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AwsS3Service awsS3Service;
    private final KakaoAddressService kakaoAddressService;
    private final PatientRepository patientRepository;
    private final BookmarkRepository bookmarkRepository;

    // 병원 회원가입
    // 동일 병원이 존재하는지 검사
    @Transactional
    public CommonResponse<HospitalSignupResponse> signup(HospitalSignupRequest request, MultipartFile image) {
        if (hospitalRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 병원 아이디입니다.");
        }

        double[] coordinate;
        try {
            coordinate = kakaoAddressService.getCoordinate(request.getAddress());

            if (coordinate == null) {
                throw new IllegalArgumentException("입력하신 주소를 찾을 수 없습니다. 도로명 주소를 정확히 입력해주세요.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            // 카카오 API 호출 중 네트워크 에러, 키 만료 등 시스템 에러
            log.error("주소 변환 중 시스템 오류 발생: address={}", request.getAddress(), e);
            throw new RuntimeException("주소 변환 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }

        // 이미지 업로드
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = awsS3Service.uploadFile(List.of(image)).get(0);
        }

        // 병원 생성 및 저장
        Hospital hospital = Hospital.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPwd()))
                .name(request.getName())
                .address(request.getAddress())
                .latitude(coordinate[0])
                .longitude(coordinate[1])
                .contact(request.getContact())
                .specialties(JsonUtil.toJson(request.getSpecialties())) // JSON 변환
                .imageUrl(imageUrl != null ? imageUrl : request.getImageUrl()) // 파일 or 기존 URL 중 하나 사용
                .build();

        hospitalRepository.save(hospital);

        // 운영시간 저장
        if (request.getOperatingHours() != null) {
            saveOperatingHoursForSignup(hospital, request.getOperatingHours(), request.getBreakTimes());
        }

        return CommonResponse.success(
                HospitalSignupResponse.from(hospital),
                "회원가입이 완료되었습니다."
        );
    }

    // 병원 로그인
    public CommonResponse<HospitalLoginResponse> login(HospitalLoginRequest request) {
        Hospital hospital = hospitalRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 병원입니다."));

        if (!passwordEncoder.matches(request.getPwd(), hospital.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 발급
        String accessToken = jwtTokenProvider.createToken(hospital.getId(), "hospital");

        return CommonResponse.success(
                HospitalLoginResponse.from(hospital, accessToken),
                "로그인 성공"
        );
    }

    // 병원 로그아웃
    public CommonResponse<Void> logout(Long hospitalId) {
        hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new NoSuchElementException("병원을 찾을 수 없습니다."));
        return CommonResponse.success("로그아웃 완료");
    }

    // 병원 정보 수정
    @Transactional
    public CommonResponse<HospitalSignupResponse> update(Long hospitalId, HospitalUpdateRequest request, MultipartFile image) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new NoSuchElementException("병원을 찾을 수 없습니다."));

        if (request.getName() != null && !request.getName().isBlank()) {
            hospital.updateName(request.getName());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            hospital.updateAddress(request.getAddress());
        }
        if (request.getContact() != null && !request.getContact().isBlank()) {
            hospital.updateContact(request.getContact());
        }
        if (request.getSpecialties() != null && !request.getSpecialties().isEmpty()) {
            String specialtiesJson = JsonUtil.toJson(request.getSpecialties());
            hospital.updateSpecialties(specialtiesJson);
        }

        // 이미지 업로드
        if (image != null && !image.isEmpty()) {
            // 기존 이미지가 있다면 S3에서 삭제
            if (hospital.getImageUrl() != null && !hospital.getImageUrl().isBlank()) {
                awsS3Service.deleteFile(hospital.getImageUrl());
            }

            // 새 이미지 업로드
            String imageUrl = awsS3Service.uploadFile(List.of(image)).get(0);
            hospital.updateImageUrl(imageUrl);
        }

        // 비밀번호 변경
        if (request.getPwd() != null && !request.getPwd().isBlank()) {
            if (!passwordEncoder.matches(request.getCurrentPwd(), hospital.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            hospital.updatePassword(passwordEncoder.encode(request.getPwd()));
        }

        // 운영시간 변경
        if (request.getOperatingHours() != null) {
            hospital.getOperatingHours().clear();
            saveOperatingHoursForUpdate(hospital, request.getOperatingHours(), request.getBreakTimes());
        }

        hospitalRepository.save(hospital);

        return CommonResponse.success(
                HospitalSignupResponse.from(hospital),
                "병원 정보가 수정되었습니다."
        );
    }

    // 회원가입시 운영시간 및 휴게시간 저장
    private void saveOperatingHoursForSignup(
            Hospital hospital,
            Map<String, HospitalSignupRequest.OperatingHourDto> operatingHours,
            Map<String, HospitalSignupRequest.BreakTimeDto> breakTimes) {

        operatingHours.forEach((day, dto) -> {
            HospitalOperatingHours hours = HospitalOperatingHours.builder()
                    .hospital(hospital)
                    .dayOfWeek(day.toUpperCase())
                    .openTime(dto.getOpenTime() != null ? LocalTime.parse(dto.getOpenTime()) : null)
                    .closeTime(dto.getCloseTime() != null ? LocalTime.parse(dto.getCloseTime()) : null)
                    .isClosed(dto.isClosed())
                    .build();

            if (breakTimes != null && breakTimes.containsKey(day)) {
                HospitalSignupRequest.BreakTimeDto breakDto = breakTimes.get(day);
                hours = HospitalOperatingHours.builder()
                        .hospital(hospital)
                        .dayOfWeek(day.toUpperCase())
                        .openTime(dto.getOpenTime() != null ? LocalTime.parse(dto.getOpenTime()) : null)
                        .closeTime(dto.getCloseTime() != null ? LocalTime.parse(dto.getCloseTime()) : null)
                        .breakStartTime(breakDto.getBreakStartTime() != null ? LocalTime.parse(breakDto.getBreakStartTime()) : null)
                        .breakEndTime(breakDto.getBreakEndTime() != null ? LocalTime.parse(breakDto.getBreakEndTime()) : null)
                        .isClosed(dto.isClosed())
                        .build();
            }

            hospital.getOperatingHours().add(hours);
        });
    }

    // 정보수정시 운영시간 및 휴게시간 저장
    private void saveOperatingHoursForUpdate(
            Hospital hospital,
            Map<String, HospitalUpdateRequest.OperatingHourDto> operatingHours,
            Map<String, HospitalUpdateRequest.BreakTimeDto> breakTimes) {

        operatingHours.forEach((day, dto) -> {
            HospitalOperatingHours hours = HospitalOperatingHours.builder()
                    .hospital(hospital)
                    .dayOfWeek(day.toUpperCase())
                    .openTime(dto.getOpenTime() != null ? LocalTime.parse(dto.getOpenTime()) : null)
                    .closeTime(dto.getCloseTime() != null ? LocalTime.parse(dto.getCloseTime()) : null)
                    .isClosed(dto.isClosed())
                    .build();

            if (breakTimes != null && breakTimes.containsKey(day)) {
                HospitalUpdateRequest.BreakTimeDto breakDto = breakTimes.get(day);
                hours = HospitalOperatingHours.builder()
                        .hospital(hospital)
                        .dayOfWeek(day.toUpperCase())
                        .openTime(dto.getOpenTime() != null ? LocalTime.parse(dto.getOpenTime()) : null)
                        .closeTime(dto.getCloseTime() != null ? LocalTime.parse(dto.getCloseTime()) : null)
                        .breakStartTime(breakDto.getBreakStartTime() != null ? LocalTime.parse(breakDto.getBreakStartTime()) : null)
                        .breakEndTime(breakDto.getBreakEndTime() != null ? LocalTime.parse(breakDto.getBreakEndTime()) : null)
                        .isClosed(dto.isClosed())
                        .build();
            }

            hospital.getOperatingHours().add(hours);
        });
    }

    // 미터 기준
    private static final double DEFAULT_RADIUS = 3000.0;

    public List<HospitalNearbyResponse> getNearbyHospitals(Double lat, Double lng, Double radius, Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 환자입니다."));

        if(lat == null || lng == null || !patient.isLocationPermission()) {
            // default 주소는 홍대
            double[] coordinate = kakaoAddressService.getCoordinate("서울특별시 마포구 와우산로 94");
            if (coordinate != null) {
                lat = coordinate[0]; // 위도
                lng = coordinate[1]; // 경도
            } else {
                // 카카오 API 실패 시 하드코딩 값 사용
                lat = 37.5509;
                lng = 126.9255;
            }
        }

        if(radius == null) {
            radius = DEFAULT_RADIUS;
        }

        log.info("현재 검색 좌표: lat={}, lng={}, radius={}", lat, lng, radius);
        List<Object[]> results = hospitalRepository.findNearbyHospitals(lat, lng, radius);

        return results.stream()
                .map(row -> HospitalNearbyResponse.builder()
                        .hospitalId(((Number) row[0]).longValue())
                        .hospitalName((String) row[1])
                        .latitude((Double) row[2])
                        .longitude((Double) row[3])
                        .distance((Double) row[4]) // 미터
                        .build())
                .collect(Collectors.toList());

    }


    public HospitalDetailResponse getHospitalDetail(Long hospitalId, Long patientId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new NoSuchElementException("병원을 찾을 수 없습니다."));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        // 해당 환자의 즐겨찾기 리스트에 hospital 이 있는지 확인
        boolean exists = bookmarkRepository.existsByPatientAndHospital(patient, hospital);

        return HospitalDetailResponse.from(hospital, exists);
    }

    public List<HospitalNearbyResponse> getAllHospitals() {
        List<Hospital> hospitals = hospitalRepository.findAll();

        return hospitals.stream()
                .map(hospital -> HospitalNearbyResponse.builder()
                        .hospitalId(hospital.getId())
                        .hospitalName(hospital.getName())
                        .latitude(hospital.getLatitude())
                        .longitude(hospital.getLongitude())
                        .distance(null)
                        .build())
                .collect(Collectors.toList());

    }
}