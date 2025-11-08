package B2A4.demoday.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class AmazonS3Controller {

    private final AwsS3Service awsS3Service;

    // 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFile(
            @RequestParam(value = "files", required = false) List<MultipartFile> multipartFiles) {

        if (multipartFiles == null || multipartFiles.isEmpty()) {
            System.out.println("파일이 비어있습니다.");
            return ResponseEntity.badRequest().body(List.of("파일이 비어 있습니다."));
        }

        System.out.println("요청 파일 개수: " + multipartFiles.size());
        return ResponseEntity.ok(awsS3Service.uploadFile(multipartFiles));
    }

    // 파일 조회
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        List<String> files = awsS3Service.listFiles();
        return ResponseEntity.ok(files);
    }

    // 특정 파일 조회
    @GetMapping
    public ResponseEntity<String> getFileUrl(@RequestParam String fileName) {
        return ResponseEntity.ok(awsS3Service.getFileUrl(fileName));
    }

    // 파일 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName){
        awsS3Service.deleteFile(fileName);
        return ResponseEntity.ok(fileName);
    }
}