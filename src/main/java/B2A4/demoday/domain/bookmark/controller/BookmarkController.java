package B2A4.demoday.domain.bookmark.controller;

import B2A4.demoday.domain.bookmark.service.BookmarkService;
import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 즐겨찾기 추가
    @PostMapping
    public CommonResponse<?> addBookmark(
            HttpServletRequest request,
            @RequestParam Long hospitalId) {

        Long patientId = (Long) request.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        return bookmarkService.addBookmark(patientId, hospitalId);
    }

    // 즐겨찾기 삭제
    @DeleteMapping
    public CommonResponse<?> removeBookmark(
            HttpServletRequest request,
            @RequestParam Long hospitalId) {

        Long patientId = (Long) request.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        return bookmarkService.removeBookmark(patientId, hospitalId);
    }

    // 즐겨찾기 조회
    @GetMapping
    public CommonResponse<?> getBookmarks(HttpServletRequest request) {
        Long patientId = (Long) request.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        return bookmarkService.getBookmarks(patientId);
    }
}