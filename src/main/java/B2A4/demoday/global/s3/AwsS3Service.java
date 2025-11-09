package B2A4.demoday.global.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    // 파일 업로드
    public List<String> uploadFile(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            try {
                s3Template.upload(bucket, fileName, file.getInputStream());
                String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
                urls.add(url);
                System.out.println("업로드 성공: " + url);
            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 실패", e);
            }
        }

        return urls;
    }

    // 파일 URL 조회
    public String getFileUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
    }
    
    // 파일 조회
    public List<String> listFiles() {
        System.out.println("S3 파일 목록 조회 시도 / bucket = " + bucket);

        var s3ClientField = s3Template.getClass().getDeclaredFields();
        List<String> urls = new ArrayList<>();

        try {
            var field = s3Template.getClass().getDeclaredField("s3Client");
            field.setAccessible(true);
            var s3Client = (software.amazon.awssdk.services.s3.S3Client) field.get(s3Template);

            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .build();

            ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

            for (S3Object obj : listRes.contents()) {
                String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, obj.key());
                urls.add(url);
            }

            System.out.println("총 " + urls.size() + "개 파일 조회 완료");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("파일 목록 조회 실패", e);
        }

        return urls;
    }

    // 파일 삭제 (URL 또는 파일명 모두 허용)
    public void deleteFile(String filePathOrUrl) {
        try {
            // 파일 키 추출
            String fileKey = extractKey(filePathOrUrl);

            // S3 객체 삭제
            s3Template.deleteObject(bucket, fileKey);
            log.info("S3 파일 삭제 완료: {}", fileKey);
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패 ({}): {}", filePathOrUrl, e.getMessage());
        }
    }

    // URL → 파일 키 변환
    private String extractKey(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("파일 경로가 비어 있습니다.");
        }

        // 만약 전체 URL이라면 맨 뒤 슬래시 이후 문자열만 추출
        if (input.startsWith("http")) {
            return input.substring(input.lastIndexOf("/") + 1);
        }

        // 이미 파일명(Key)만 들어온 경우 그대로 리턴
        return input;
    }
}