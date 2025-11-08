package B2A4.demoday.domain.doctor.service;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.doctor.dto.request.DoctorRegisterRequest;
import B2A4.demoday.domain.doctor.dto.request.DoctorSelectRequest;
import B2A4.demoday.domain.doctor.dto.response.*;
import B2A4.demoday.domain.doctor.entity.Doctor;
import B2A4.demoday.domain.doctor.repository.DoctorRepository;
import B2A4.demoday.domain.hospital.entity.Hospital;
import B2A4.demoday.domain.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 공용 QR 코드 생성
    private String generateUniqueQrCode(Long hospitalId) {
        String qrCode;
        do {
            qrCode = "H" + hospitalId + "-D" + UUID.randomUUID().toString().substring(0, 8);
        } while (doctorRepository.findByQrCode(qrCode).isPresent());
        return qrCode;
    }

    // 의사 등록
    public CommonResponse<DoctorRegisterResponse> registerDoctor(Long hospitalId, DoctorRegisterRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new NoSuchElementException("병원을 찾을 수 없습니다."));

        // 동일 이름의 의사가 이미 같은 병원에 존재하는지 검사
        Optional<Doctor> existingDoctor = doctorRepository.findByNameAndHospital_Id(request.getName(), hospitalId);
        if (existingDoctor.isPresent()) {
            throw new IllegalArgumentException("이미 등록된 의사입니다.");
        }

        // QR 코드 생성 (중복 방지)
        String qrCode = generateUniqueQrCode(hospital.getId());

        // 의사 생성 및 저장
        Doctor doctor = Doctor.builder()
                .hospital(hospital)
                .name(request.getName())
                .specialty(request.getSpecialty())
                .password(passwordEncoder.encode(request.getPinCode())) // PIN 암호화
                .imageUrl(request.getImageUrl())
                .qrCode(qrCode)
                .qrGeneratedAt(LocalDateTime.now())
                .build();

        doctorRepository.save(doctor);

        return CommonResponse.success(
                DoctorRegisterResponse.from(doctor),
                "의사가 등록되었습니다."
        );
    }

    // 병원 소속 의사 목록 조회
    public CommonResponse<List<DoctorListResponse>> getDoctorsByHospital(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new NoSuchElementException("병원을 찾을 수 없습니다."));

        List<Doctor> doctors = hospital.getDoctors();
        List<DoctorListResponse> responseList = doctors.stream()
                .map(DoctorListResponse::from)
                .collect(Collectors.toList());

        return CommonResponse.success(responseList, "의사 목록 조회 성공");
    }

    // 의사 선택
    public CommonResponse<DoctorSelectResponse> selectDoctor(DoctorSelectRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("의사를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPinCode(), doctor.getPassword())) {
            throw new IllegalArgumentException("의사 인증에 실패했습니다. 비밀번호가 일치하지 않습니다.");
        }

        return CommonResponse.success(
                DoctorSelectResponse.from(doctor),
                "의사 선택 완료"
        );
    }

    // QR 코드 조회
    public CommonResponse<DoctorQrResponse> getDoctorQr(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("의사를 찾을 수 없습니다."));

        DoctorQrResponse response = DoctorQrResponse.from(doctor);

        return CommonResponse.success(response, "QR 코드 조회 성공");
    }

    // QR 코드로 의사 정보 조회
    public CommonResponse<DoctorInfoResponse> getDoctorByQr(String qrCode) {
        Doctor doctor = doctorRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new NoSuchElementException("해당 QR 코드의 의사를 찾을 수 없습니다."));

        return CommonResponse.success(
                DoctorInfoResponse.from(doctor),
                "QR 코드로 의사 정보 조회 성공"
        );
    }

    // QR 코드 재발급
    public CommonResponse<DoctorQrRegenerateResponse> regenerateDoctorQr(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("의사를 찾을 수 없습니다."));

        String previousQr = doctor.getQrCode();
        String newQrCode = generateUniqueQrCode(doctor.getHospital().getId());

        doctor.updateQrCode(newQrCode, LocalDateTime.now());
        doctorRepository.save(doctor);

        DoctorQrRegenerateResponse response = DoctorQrRegenerateResponse.from(doctor, previousQr);
        return CommonResponse.success(response, "QR 코드 재발급 성공");
    }
}