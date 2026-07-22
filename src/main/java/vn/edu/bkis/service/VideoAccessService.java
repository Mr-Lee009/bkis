package vn.edu.bkis.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import vn.edu.bkis.dto.VideoSignedUrlDto;
import vn.edu.bkis.model.EnrollmentStatus;
import vn.edu.bkis.model.Lesson;
import vn.edu.bkis.model.LessonVideo;
import vn.edu.bkis.model.User;
import vn.edu.bkis.repository.EnrollmentRepository;
import vn.edu.bkis.repository.LessonRepository;
import vn.edu.bkis.repository.LessonVideoRepository;

/** Xu ly cap va xac thuc link stream video noi bo co thoi han. */
@Service
public class VideoAccessService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final LessonVideoRepository lessonVideoRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final Path storageRoot;
    private final String signingSecret;
    private final long validMinutes;

    /** Khoi tao service ky URL va kiem tra file video trong thu muc luu tru. */
    public VideoAccessService(LessonVideoRepository lessonVideoRepository, LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository, @Value("${app.video.storage-root:uploads}") String storageRoot,
            @Value("${app.video.signing-secret:bkis-video-secret-key-change-me}") String signingSecret,
            @Value("${app.video.signed-url-valid-minutes:60}") long validMinutes) {
        this.lessonVideoRepository = lessonVideoRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
        this.signingSecret = signingSecret;
        this.validMinutes = validMinutes > 0 ? validMinutes : 60;
    }

    /**
     * Cap URL stream cho video neu user duoc xem video.
     * @param videoId id video can xem
     * @param currentUser user hien tai, co the null voi video preview
     * @return DTO chua signed URL va thoi diem het han
     * @throws IllegalArgumentException neu video khong ton tai hoac key khong hop le
     * @throws SecurityException neu user khong co quyen xem video
     */
    public VideoSignedUrlDto createSignedUrl(Long videoId, User currentUser) {
        // Step 1: tai video, lesson va kiem tra quyen xem theo preview hoac enrollment ACTIVE.
        LessonVideo video = lessonVideoRepository.findById(videoId).orElseThrow(() -> new IllegalArgumentException("Video not found"));
        Lesson lesson = lessonRepository.findById(video.getLessonId()).orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        boolean enrolled = currentUser != null && currentUser.getId() != null
                && enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(currentUser.getId(), lesson.getCourseId(), EnrollmentStatus.ACTIVE);
        if (!Boolean.TRUE.equals(video.getPreview()) && !enrolled) {
            throw new SecurityException("You do not have permission to watch this video");
        }

        // Step 2: chuan hoa key noi bo va tao chu ky HMAC gan voi thoi diem het han.
        String videoKey = normalizeVideoKey(video.getVideoUrl());
        long expiresAt = Instant.now().plusSeconds(validMinutes * 60).getEpochSecond();
        String signature = sign(videoKey, expiresAt);

        // Step 3: tra URL tuong doi de browser goi endpoint stream ma khong lo duong dan luu tru that.
        String url = UriComponentsBuilder.fromPath("/secure/videos/stream").queryParam("key", videoKey)
                .queryParam("expires", expiresAt).queryParam("signature", signature).build().encode().toUriString();
        return new VideoSignedUrlDto(url, expiresAt);
    }

    /**
     * Xac thuc signed URL va tra path file nam trong storage root.
     * @param videoKey key video tuong doi
     * @param expiresAt epoch seconds het han
     * @param signature chu ky HMAC cua key va expires
     * @return path file hop le trong storage root
     * @throws IllegalArgumentException neu chu ky sai, URL het han hoac file khong ton tai
     */
    public Path resolveSignedVideo(String videoKey, long expiresAt, String signature) {
        // Step 1: kiem tra thoi han va doi chieu chu ky bang so sanh constant-time.
        if (expiresAt <= Instant.now().getEpochSecond() || videoKey == null || signature == null
                || !MessageDigest.isEqual(sign(videoKey, expiresAt).getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Video link is invalid or expired");
        }

        // Step 2: chan URL ngoai, path traversal va moi file nam ngoai storage root.
        String normalizedKey = normalizeVideoKey(videoKey);
        Path videoPath = storageRoot.resolve(normalizedKey).normalize();
        if (!videoPath.startsWith(storageRoot) || !Files.isRegularFile(videoPath)) {
            throw new IllegalArgumentException("Video file not found");
        }
        return videoPath;
    }

    /**
     * Chuan hoa du lieu cu dang URL `/uploads/...` ve key tuong doi an toan.
     * @param rawVideoKey key video hoac URL cu luu trong database
     * @return key tuong doi an toan
     * @throws IllegalArgumentException neu key rong, co path traversal hoac la URL khong hop le
     */
    private String normalizeVideoKey(String rawVideoKey) {
        if (rawVideoKey == null || rawVideoKey.isBlank()) {
            throw new IllegalArgumentException("Video key is required");
        }
        String key = rawVideoKey.trim();
        if (key.contains("://")) {
            key = URI.create(key).getPath();
        }
        if (key == null || key.startsWith("/secure/") || key.contains("..")) {
            throw new IllegalArgumentException("Invalid video key");
        }
        if (key.startsWith("/uploads/")) {
            key = key.substring("/uploads/".length());
        } else if (key.startsWith("uploads/")) {
            key = key.substring("uploads/".length());
        }
        if (key.isBlank() || key.startsWith("/") || key.contains("\\")) {
            throw new IllegalArgumentException("Invalid video key");
        }
        return key;
    }

    /**
     * Tao chu ky HMAC-SHA256 on dinh cho key va thoi diem het han.
     * @param videoKey key video tuong doi
     * @param expiresAt epoch seconds het han
     * @return chuoi chu ky HMAC dang hexadecimal
     * @throws IllegalStateException neu khong khoi tao duoc bo may HMAC
     */
    private String sign(String videoKey, long expiresAt) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal((videoKey + ":" + expiresAt).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException exception) {
            throw new IllegalStateException("Cannot sign video URL", exception);
        }
    }
}
