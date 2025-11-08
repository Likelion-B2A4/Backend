package B2A4.demoday.domain.hospital.service;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.common.JsonUtil;
import B2A4.demoday.domain.hospital.dto.request.HospitalSignupRequest;
import B2A4.demoday.domain.hospital.dto.request.HospitalLoginRequest;
import B2A4.demoday.domain.hospital.dto.request.HospitalUpdateRequest;
import B2A4.demoday.domain.hospital.dto.response.HospitalSignupResponse;
import B2A4.demoday.domain.hospital.dto.response.HospitalLoginResponse;
import B2A4.demoday.domain.hospital.entity.Hospital;
import B2A4.demoday.domain.hospital.entity.HospitalOperatingHours;
import B2A4.demoday.domain.hospital.repository.HospitalRepository;
import B2A4.demoday.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 병원 회원가입
    // 동일 병원이 존재하는지 검사
    public CommonResponse<HospitalSignupResponse> signup(HospitalSignupRequest request) {
        if (hospitalRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 병원 아이디입니다.");
        }

        // 병원 생성 및 저장
        Hospital hospital = Hospital.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPwd()))
                .name(request.getName())
                .address(request.getAddress())
                .contact(request.getContact())
                .specialties(JsonUtil.toJson(request.getSpecialties())) // JSON 변환
                .imageUrl(request.getImageUrl())
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
    public CommonResponse<HospitalSignupResponse> update(Long hospitalId, HospitalUpdateRequest request) {
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
}