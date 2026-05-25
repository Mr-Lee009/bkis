package vn.edu.bkis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.bkis.model.UploadSession;
import vn.edu.bkis.util.SlugUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UploadService {

    private static final String BASE_DIR = "uploads/";
    private static final String TMP_BASE_DIR = "upload-tmp/";
    private static final String DEFAULT_TARGET_FOLDER = "source";
    private static final String FILE_NAME_FALLBACK = "upload-file";

    private final Map<String, UploadSession> sessionMap = new ConcurrentHashMap<>();
    private final long tempTtlMinutes;

    /**
     * Khoi tao service upload.
     *
     * @param tempTtlMinutes thoi gian song cua session tam (phut)
     */
    public UploadService(@Value("${app.upload.temp-ttl-minutes:120}") long tempTtlMinutes) {
        this.tempTtlMinutes = tempTtlMinutes;
    }

    /**
     * Khoi tao upload voi folder mac dinh.
     *
     * @param fileName ten file goc
     * @param totalSize tong dung luong file (bytes)
     * @param totalChunks tong so chunk du kien
     * @return upload id de upload chunk
     */
    public String initUpload(String fileName, long totalSize, int totalChunks) {
        return initUpload(fileName, totalSize, totalChunks, "/" + DEFAULT_TARGET_FOLDER);
    }

    /**
     * Khoi tao upload voi folder dich truyen tu client.
     *
     * @param fileName ten file goc
     * @param totalSize tong dung luong file (bytes)
     * @param totalChunks tong so chunk du kien
     * @param targetFolder folder dich dang path
     * @return upload id de upload chunk
     */
    public String initUpload(String fileName, long totalSize, int totalChunks, String targetFolder) {
        validateInitRequest(fileName, totalSize, totalChunks);

        String uploadId = UUID.randomUUID().toString();
        String safeFolder = SlugUtil.toSafeFolderPath(targetFolder, DEFAULT_TARGET_FOLDER);
        String storedFileName = buildStoredFileName(fileName);
        Path uploadPath = getTempRootPath().resolve(uploadId).normalize();
        ensureTempPath(uploadPath);

        try {
            Files.createDirectories(uploadPath);
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException("Upload path already exists but is not a directory");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + uploadPath, e);
        }

        UploadSession session = new UploadSession(
            fileName,
            storedFileName,
            safeFolder,
            totalSize,
            totalChunks,
            uploadPath.toString()
        );
        sessionMap.put(uploadId, session);
        return uploadId;
    }

    /**
     * Luu mot chunk vao thu muc tam.
     *
     * @param uploadId ma phien upload
     * @param chunkIndex chi so chunk hien tai
     * @param file noi dung chunk
     * @throws IOException khi ghi file that bai
     */
    public void saveChunk(String uploadId, int chunkIndex, MultipartFile file) throws IOException {
        UploadSession session = requireSession(uploadId);
        validateChunkRequest(session, chunkIndex, file);

        Path dirPath = Paths.get(session.getDirPath()).normalize();
        ensureTempPath(dirPath);

        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new IOException("Cannot create directory: " + dirPath, e);
        }

        Path chunkPath = dirPath.resolve("chunk_" + chunkIndex).normalize();
        ensureTempPath(chunkPath);

        try {
            Files.copy(file.getInputStream(), chunkPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to save chunk " + chunkIndex, e);
        }

        session.getUploadedChunks().add(chunkIndex);
        session.touch();
    }

    /**
     * Ghep cac chunk tam thanh file hoan chinh.
     *
     * @param uploadId ma phien upload
     * @return URL public tuong doi cua file moi
     * @throws IOException khi ghep file that bai
     */
    public String completeUpload(String uploadId) throws IOException {
        UploadSession session = requireSession(uploadId);
        if (session.getUploadedChunks().size() != session.getTotalChunks()) {
            throw new IllegalStateException("Not all chunks uploaded");
        }

        Path finalDir = Paths.get(BASE_DIR).resolve(session.getTargetFolder()).normalize();
        Files.createDirectories(finalDir);
        Path finalPath = finalDir.resolve(session.getStoredFileName()).normalize();

        try (FileOutputStream fos = new FileOutputStream(finalPath.toFile())) {
            for (int i = 0; i < session.getTotalChunks(); i++) {
                Path chunkPath = Paths.get(session.getDirPath()).resolve("chunk_" + i).normalize();
                ensureTempPath(chunkPath);
                if (!Files.exists(chunkPath)) {
                    throw new IllegalStateException("Missing chunk: " + i);
                }
                Files.copy(chunkPath, fos);
            }
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(finalPath);
            throw e;
        }

        deleteDirectory(Paths.get(session.getDirPath()).toFile());
        sessionMap.remove(uploadId);
        return toPublicUrl(finalPath);
    }

    /**
     * Huy phien upload va xoa toan bo chunk tam.
     *
     * @param uploadId ma phien upload can huy
     * @return true neu da xoa duoc session hoac folder tam
     */
    public boolean abortUpload(String uploadId) {
        if (uploadId == null || uploadId.isBlank()) {
            throw new IllegalArgumentException("Upload id is required");
        }

        UploadSession session = sessionMap.remove(uploadId);
        Path tempPath = getTempRootPath().resolve(uploadId).normalize();
        ensureTempPath(tempPath);

        if (session != null) {
            deleteDirectory(Paths.get(session.getDirPath()).toFile());
            return true;
        }
        if (Files.exists(tempPath)) {
            deleteDirectory(tempPath.toFile());
            return true;
        }
        return false;
    }

    /**
     * Xoa file upload cu khi cap nhat de tranh ton file khong con duoc tham chieu.
     *
     * @param fileUrl URL file hien tai trong DB
     * @return true neu URL thuoc he thong upload va da xu ly xoa
     */
    public boolean deleteUploadedFile(String fileUrl) {
        Path managedPath = resolveManagedUploadPath(fileUrl);
        if (managedPath == null) {
            return false;
        }

        try {
            Files.deleteIfExists(managedPath);
            deleteEmptyParentDirectories(managedPath.getParent(), Paths.get(BASE_DIR).toAbsolutePath().normalize());
            return true;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete old uploaded file", ex);
        }
    }

    /**
     * Kiem tra URL co phai file duoc quan ly boi he thong upload hay khong.
     *
     * @param fileUrl URL file can kiem tra
     * @return true neu URL map duoc vao thu muc uploads
     */
    public boolean isManagedUploadUrl(String fileUrl) {
        return resolveManagedUploadPath(fileUrl) != null;
    }

    /**
     * Don session qua han va folder mo coi theo lich dinh ky.
     *
     * @return void
     */
    @Scheduled(fixedDelayString = "${app.upload.cleanup.fixed-delay-ms:1800000}")
    public void cleanupExpiredUploads() {
        Instant expiredBefore = Instant.now().minus(Duration.ofMinutes(tempTtlMinutes));
        sessionMap.entrySet().removeIf((entry) -> {
            UploadSession session = entry.getValue();
            if (session.getUpdatedAt() == null || !session.getUpdatedAt().isBefore(expiredBefore)) {
                return false;
            }
            deleteDirectory(Paths.get(session.getDirPath()).toFile());
            return true;
        });
        cleanupOrphanTempDirectories(expiredBefore);
    }

    /**
     * Chuan hoa ten file luu tru noi bo.
     *
     * @param fileName ten file goc
     * @return ten file da gan UUID
     */
    private String buildStoredFileName(String fileName) {
        String originalName = Paths.get(fileName).getFileName().toString();
        String safeName = SlugUtil.toSafeFileName(originalName, FILE_NAME_FALLBACK);
        return UUID.randomUUID() + "-" + safeName;
    }

    /**
     * Doc session upload theo id.
     *
     * @param uploadId ma phien upload
     * @return session upload ton tai
     */
    private UploadSession requireSession(String uploadId) {
        UploadSession session = sessionMap.get(uploadId);
        if (session == null) {
            throw new IllegalArgumentException("Invalid uploadId");
        }
        return session;
    }

    /**
     * Kiem tra input cho buoc init upload.
     *
     * @param fileName ten file goc
     * @param totalSize tong dung luong file
     * @param totalChunks tong so chunk du kien
     * @return void
     */
    private void validateInitRequest(String fileName, long totalSize, int totalChunks) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }
        if (totalSize <= 0) {
            throw new IllegalArgumentException("Total size must be > 0");
        }
        if (totalChunks <= 0) {
            throw new IllegalArgumentException("Total chunks must be > 0");
        }
    }

    /**
     * Kiem tra input cho tung chunk upload.
     *
     * @param session session upload hien tai
     * @param chunkIndex chi so chunk
     * @param file noi dung chunk
     * @return void
     */
    private void validateChunkRequest(UploadSession session, int chunkIndex, MultipartFile file) {
        if (chunkIndex < 0 || chunkIndex >= session.getTotalChunks()) {
            throw new IllegalArgumentException("Invalid chunk index");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chunk file is empty");
        }
    }

    /**
     * Xoa folder va toan bo file con ben trong.
     *
     * @param dir folder can xoa
     * @return void
     */
    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }

    /**
     * Don folder tmp mo coi khong con session trong memory.
     *
     * @param expiredBefore moc thoi gian qua han
     * @return void
     */
    private void cleanupOrphanTempDirectories(Instant expiredBefore) {
        Path tempRoot = getTempRootPath();
        if (!Files.isDirectory(tempRoot)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempRoot)) {
            for (Path tempPath : stream) {
                if (!Files.isDirectory(tempPath) || sessionMap.containsKey(tempPath.getFileName().toString())) {
                    continue;
                }
                Instant lastModified = Files.getLastModifiedTime(tempPath).toInstant();
                if (lastModified.isBefore(expiredBefore)) {
                    deleteDirectory(tempPath.toFile());
                }
            }
        } catch (IOException ignored) {
            // Bo qua loi cleanup de khong anh huong luong upload chinh.
        }
    }

    /**
     * Chuyen URL file thanh path upload noi bo.
     *
     * @param fileUrl URL file dang luu trong DB
     * @return path tuong ung neu la file quan ly boi he thong, null neu khong
     */
    private Path resolveManagedUploadPath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }

        String pathPart = fileUrl.trim();
        if (pathPart.contains("://")) {
            try {
                pathPart = URI.create(pathPart).getPath();
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        if (pathPart == null || !pathPart.startsWith("/uploads/")) {
            return null;
        }

        String relativePath = pathPart.substring("/uploads/".length());
        if (relativePath.isBlank()) {
            return null;
        }

        Path uploadsRoot = Paths.get(BASE_DIR).toAbsolutePath().normalize();
        Path filePath = uploadsRoot.resolve(relativePath).normalize();
        if (!filePath.startsWith(uploadsRoot) || Files.isDirectory(filePath)) {
            return null;
        }
        return filePath;
    }

    /**
     * Xoa folder cha rong de giam rac sau khi xoa file.
     *
     * @param startPath folder bat dau xet
     * @param stopPath folder goc dung vong lap
     * @return void
     */
    private void deleteEmptyParentDirectories(Path startPath, Path stopPath) {
        Path current = startPath;
        while (current != null && !current.equals(stopPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(current)) {
                if (stream.iterator().hasNext()) {
                    return;
                }
            } catch (IOException ex) {
                return;
            }

            try {
                Files.deleteIfExists(current);
            } catch (IOException ex) {
                return;
            }
            current = current.getParent();
        }
    }

    /**
     * Tao URL public tu path file trong thu muc uploads.
     *
     * @param finalPath path file ket qua
     * @return URL tuong doi de frontend truy cap
     */
    private String toPublicUrl(Path finalPath) {
        String relativePath = Paths.get(BASE_DIR).relativize(finalPath).toString().replace('\\', '/');
        return "/uploads/" + relativePath;
    }

    /**
     * Lay thu muc goc luu chunk tam.
     *
     * @return path thu muc temp
     */
    private Path getTempRootPath() {
        return Paths.get(TMP_BASE_DIR).normalize();
    }

    /**
     * Dam bao path dang thao tac nam trong thu muc temp duoc phep.
     *
     * @param tempPath path can kiem tra
     * @return void
     */
    private void ensureTempPath(Path tempPath) {
        Path tempRoot = getTempRootPath().toAbsolutePath().normalize();
        Path absolutePath = tempPath.toAbsolutePath().normalize();
        if (!absolutePath.startsWith(tempRoot)) {
            throw new IllegalArgumentException("Invalid upload temp path");
        }
    }
}
