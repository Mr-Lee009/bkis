package vn.edu.bkis.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.bkis.dto.VideoSignedUrlDto;
import vn.edu.bkis.model.User;
import vn.edu.bkis.security.CustomOAuth2User;
import vn.edu.bkis.security.CustomUserDetails;
import vn.edu.bkis.service.VideoAccessService;

/** API cap signed URL va stream video khoa hoc. */
@RestController
public class VideoController {
    private final VideoAccessService videoAccessService;

    /**
     * Khoi tao controller video voi service kiem soat quyen truy cap.
     * @param videoAccessService service kiem soat quyen xem va stream video
     */
    public VideoController(VideoAccessService videoAccessService) {
        this.videoAccessService = videoAccessService;
    }

    /**
     * Cap link xem video co hieu luc mot gio cho user hien tai.
     * @param videoId id video can xem
     * @param authentication thong tin dang nhap hien tai, co the null voi video preview
     * @return DTO chua signed URL va thoi diem het han
     * @throws IllegalArgumentException neu video khong ton tai hoac khong phai video noi bo
     * @throws SecurityException neu user khong co quyen xem video
     */
    @GetMapping("/api/videos/{videoId}/signed-url")
    public VideoSignedUrlDto signedUrl(@PathVariable Long videoId, Authentication authentication) {
        return videoAccessService.createSignedUrl(videoId, getCurrentUser(authentication));
    }

    /**
     * Stream video theo signed URL va ho tro HTTP Range de the video tua/phat on dinh.
     * @param key key video tuong doi trong URL da ky
     * @param expires epoch seconds het han cua URL
     * @param signature chu ky HMAC cua key va expires
     * @param headers HTTP headers, co the chua Range
     * @return response chua file hoac ResourceRegion
     * @throws IOException neu khong doc duoc file video
     * @throws IllegalArgumentException neu URL sai, het han hoac file khong ton tai
     */
    @GetMapping(value = "/secure/videos/stream", produces = "video/*")
    public ResponseEntity<?> stream(@RequestParam String key, @RequestParam long expires,
            @RequestParam String signature, @RequestHeader HttpHeaders headers) throws IOException {
        // Step 1: xac thuc signed URL va lay file video nam trong storage root.
        Path videoPath = videoAccessService.resolveSignedVideo(key, expires, signature);
        Resource resource = new FileSystemResource(videoPath);
        MediaType contentType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        // Step 2: chon response day du hoac response theo Range cua trinh duyet.
        List<org.springframework.http.HttpRange> ranges = headers.getRange();
        if (ranges.isEmpty()) {
            return ResponseEntity.ok().contentType(contentType).contentLength(resource.contentLength())
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes").body(resource);
        }
        // Step 3: tra mot region de video co the tua va tai theo tung phan.
        ResourceRegion region = ranges.get(0).toResourceRegion(resource);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).contentType(contentType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes").body(region);
    }

    /**
     * Lay user local tu principal de service quyet dinh quyen xem video tra phi.
     * @param authentication principal dang dang nhap
     * @return user local hoac null neu chua dang nhap
     */
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getUser();
        }
        if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getUser();
        }
        return null;
    }
}
