package B2A4.demoday.global.kakao.controller;

import B2A4.demoday.global.kakao.service.KakaoAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AddressController {

    private final KakaoAddressService kakaoAddressService;

    /** 개발 테스트용 컨트롤러 */
    @GetMapping("/api/test/geocode")
    public String getCoordinates(@RequestParam String address) {
        double[] coords = kakaoAddressService.getCoordinate(address);

        if (coords != null) {
            return "주소: " + address + " -> 위도: " + coords[0] + ", 경도: " + coords[1];
        } else {
            return "주소를 찾을 수 없습니다.";
        }
    }
}
