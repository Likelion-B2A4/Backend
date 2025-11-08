package B2A4.demoday.domain.patient.service;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.patient.dto.request.PatientLoginRequest;
import B2A4.demoday.domain.patient.dto.request.PatientUpdateRequest;
import B2A4.demoday.domain.patient.dto.response.PatientLoginResponse;
import B2A4.demoday.domain.patient.dto.request.PatientSignupRequest;
import B2A4.demoday.domain.patient.dto.response.PatientSignupResponse;
import B2A4.demoday.domain.patient.entity.Patient;
import B2A4.demoday.domain.patient.repository.PatientRepository;
import B2A4.demoday.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    // 환자 회원가입
    // 동일 환자가 존재하는지 검사
    public CommonResponse<PatientSignupResponse> signup(PatientSignupRequest request) {
        if (patientRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // 병원 생성 및 저장
        Patient patient = Patient.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPwd()))
                .name(request.getName())
                .build();

        patientRepository.save(patient);

        return CommonResponse.success(
                PatientSignupResponse.from(patient),
                "회원가입이 완료되었습니다."
        );
    }

    // 환자 로그인
    public CommonResponse<PatientLoginResponse> login(PatientLoginRequest request) {
        Patient patient = patientRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(request.getPwd(), patient.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 발급 (userType = "patient")
        String accessToken = jwtTokenProvider.createToken(patient.getId(), "patient");

        return CommonResponse.success(
                PatientLoginResponse.from(patient, accessToken),
                "로그인 성공"
        );
    }

    // 환자 로그아웃
    public CommonResponse<Void> logout(Long patientId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        return CommonResponse.success("로그아웃 완료");
    }

    // 환자 정보 수정
    @Transactional
    public CommonResponse<PatientSignupResponse> update(Long patientId, PatientUpdateRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        // 이름 변경 (선택적)
        if (request.getName() != null && !request.getName().isBlank()) {
            patient.updateName(request.getName());
        }

        // 비밀번호 변경 (선택적)
        if (request.getPwd() != null && !request.getPwd().isBlank()) {
            patient.updatePassword(passwordEncoder.encode(request.getPwd()));
        }

        // JPA의 변경감지(Dirty Checking)로 자동 update 됨
        return CommonResponse.success(
                PatientSignupResponse.from(patient),
                "회원 정보가 수정되었습니다."
        );
    }
}