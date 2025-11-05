package B2A4.demoday.domain.auth.controller;

import B2A4.demoday.domain.auth.service.AuthService;
import B2A4.demoday.domain.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 농인 회원가입
    @PostMapping("/patient/signup")
    public ResponseEntity<CommonResponse<String>> registerPatient(@RequestParam String loginId,
                                                                  @RequestParam String password,
                                                                  @RequestParam String name) {
        return ResponseEntity.ok(authService.registerPatient(loginId, password, name));
    }

    // 병원 회원가입
    @PostMapping("/hospital/signup")
    public ResponseEntity<CommonResponse<String>> registerHospital(@RequestParam String loginId,
                                                                   @RequestParam String password,
                                                                   @RequestParam String name,
                                                                   @RequestParam String address) {
        return ResponseEntity.ok(authService.registerHospital(loginId, password, name, address));
    }

    // 농인 로그인
    @PostMapping("/patient/login")
    public ResponseEntity<CommonResponse<String>> loginPatient(@RequestParam String loginId,
                                                               @RequestParam String password) {
        return ResponseEntity.ok(authService.loginPatient(loginId, password));
    }

    // 병원 로그인
    @PostMapping("/hospital/login")
    public ResponseEntity<CommonResponse<String>> loginHospital(@RequestParam String loginId,
                                                                @RequestParam String password) {
        return ResponseEntity.ok(authService.loginHospital(loginId, password));
    }

    // 의사 로그인 (PIN)
    @PostMapping("/doctor/login")
    public ResponseEntity<CommonResponse<String>> loginDoctor(@RequestParam Long hospitalId,
                                                              @RequestParam String name,
                                                              @RequestParam String password) {
        return ResponseEntity.ok(authService.loginDoctor(hospitalId, name, password));
    }
}