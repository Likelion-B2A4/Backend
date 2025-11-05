package B2A4.demoday.domain.auth.service;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.doctor.entity.Doctor;
import B2A4.demoday.domain.doctor.repository.DoctorRepository;
import B2A4.demoday.domain.hospital.entity.Hospital;
import B2A4.demoday.domain.hospital.repository.HospitalRepository;
import B2A4.demoday.domain.patient.entity.Patient;
import B2A4.demoday.domain.patient.repository.PatientRepository;
import B2A4.demoday.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /** 농인 회원가입 **/
    public CommonResponse<String> registerPatient(String loginId, String password, String name) {
        if (patientRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        Patient patient = Patient.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .name(name)
                .build();

        patientRepository.save(patient);
        return CommonResponse.success("농인 회원가입 성공");
    }

    /** 병원 회원가입 **/
    public CommonResponse<String> registerHospital(String loginId, String password, String name, String address) {
        if (hospitalRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 존재하는 병원 아이디입니다.");
        }

        Hospital hospital = Hospital.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(password))
                .name(name)
                .address(address)
                .build();

        hospitalRepository.save(hospital);
        return CommonResponse.success("병원 회원가입 성공");
    }

    /** 농인 로그인 **/
    public CommonResponse<String> loginPatient(String loginId, String password) {
        Patient patient = patientRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (!passwordEncoder.matches(password, patient.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(patient.getId(), "patient");
        return CommonResponse.success(token, "로그인 성공");
    }

    /** 병원 로그인 **/
    public CommonResponse<String> loginHospital(String loginId, String password) {
        Hospital hospital = hospitalRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 병원 계정입니다."));

        if (!passwordEncoder.matches(password, hospital.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(hospital.getId(), "hospital");
        return CommonResponse.success(token, "로그인 성공");
    }

    /** 의사 로그인 (PIN 입력) **/
    public CommonResponse<String> loginDoctor(Long hospitalId, String doctorName, String password) {
        Doctor doctor = doctorRepository.findByNameAndHospital_Id(doctorName, hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("의사 정보를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, doctor.getPassword())) {
            throw new IllegalArgumentException("PIN 번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(doctor.getId(), "doctor");
        return CommonResponse.success(token, "의사 로그인 성공");
    }
}