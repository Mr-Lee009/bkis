package vn.edu.bkis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.bkis.model.UploadSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class UploadService {

  private static final String BASE_DIR = "uploads/";
  private static final String TMP_BASE_DIR = "upload-tmp/";
  private static final String DEFAULT_TARGET_FOLDER = "/source";
  private static final Pattern SAFE_FILE_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

  private final Map<String, UploadSession> sessionMap = new ConcurrentHashMap<>();
  private final long tempTtlMinutes;

  // Nap thoi gian song cua upload tam de job cleanup biet khi nao duoc xoa.
  public UploadService(@Value("${app.upload.temp-ttl-minutes:120}") long tempTtlMinutes) {
    this.tempTtlMinutes = tempTtlMinutes;
  }

  // Khoi tao upload mac dinh vao thu muc /source de giu tuong thich API cu.
  public String initUpload(String fileName, long totalSize, int totalChunks) {
    return initUpload(fileName, totalSize, totalChunks, DEFAULT_TARGET_FOLDER);
  }

  // Khoi tao phien upload theo thu muc dich duoc truyen tu UI.
  public String initUpload(String fileName, long totalSize, int totalChunks, String targetFolder) {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("File name is required");
    }

    if (totalSize <= 0) {
      throw new IllegalArgumentException("Total size must be > 0");
    }

    if (totalChunks <= 0) {
      throw new IllegalArgumentException("Total chunks must be > 0");
    }

    String uploadId = UUID.randomUUID().toString();
    String safeFolder = normalizeTargetFolder(targetFolder);
    String storedFileName = buildStoredFileName(fileName);
    Path uploadPath = getTempRootPath().resolve(uploadId).normalize();
    ensureTempPath(uploadPath);

    try {
      // Tao thu muc tam rieng cho tung uploadId.
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

  // Luu tung chunk len dia tam va danh dau trang thai chunk da nhan.
  public void saveChunk(String uploadId, int chunkIndex, MultipartFile file) throws IOException {
    UploadSession session = sessionMap.get(uploadId);

    if (session == null) {
      throw new IllegalArgumentException("Invalid uploadId");
    }

    if (chunkIndex < 0 || chunkIndex >= session.getTotalChunks()) {
      throw new IllegalArgumentException("Invalid chunk index");
    }

    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Chunk file is empty");
    }

    Path dirPath = Paths.get(session.getDirPath()).normalize();
    ensureTempPath(dirPath);

    try {
      // Tao lai thu muc tam neu bi xoa ngoai y muon trong qua trinh upload.
      Files.createDirectories(dirPath);
    } catch (IOException e) {
      throw new IOException("Cannot create directory: " + dirPath, e);
    }

    Path chunkPath = dirPath.resolve("chunk_" + chunkIndex).normalize();
    ensureTempPath(chunkPath);

    try {
      // Ghi chunk theo chi so de buoc complete ghep dung thu tu.
      Files.copy(file.getInputStream(), chunkPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IOException("Failed to save chunk " + chunkIndex, e);
    }

    session.getUploadedChunks().add(chunkIndex);
    session.touch();
  }

  // Ghep cac chunk thanh file hoan chinh va tra ve duong dan public cua file.
  public String completeUpload(String uploadId) throws IOException {
    UploadSession session = sessionMap.get(uploadId);

    if (session == null) {
      throw new IllegalArgumentException("Invalid uploadId");
    }

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

  // Huy phien upload dang do dang va xoa cac chunk tam da nhan.
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

  // Don cac phien upload qua han va cac thu muc tmp mo coi.
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

  // Xoa thu muc va file ben trong bang de quy, chi dung cho thu muc tam da xac thuc.
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

  // Quet uploads/tmp de xoa folder khong con session sau khi app restart.
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
      // Khong de loi cleanup lam anh huong upload chinh.
    }
  }

  // Chuan hoa thu muc dich de chan path traversal va ki tu khong hop le.
  private String normalizeTargetFolder(String folder) {
    String rawFolder = folder == null || folder.isBlank() ? DEFAULT_TARGET_FOLDER : folder.trim();
    String withoutSlashes = rawFolder.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");

    if (withoutSlashes.isBlank()) {
      return DEFAULT_TARGET_FOLDER.substring(1);
    }

    String[] parts = withoutSlashes.split("/");
    StringBuilder safeFolder = new StringBuilder();
    for (String part : parts) {
      String safePart = sanitizePathPart(part);
      if (safePart.isBlank() || ".".equals(safePart) || "..".equals(safePart)) {
        throw new IllegalArgumentException("Invalid upload folder");
      }
      if (!safeFolder.isEmpty()) {
        safeFolder.append('/');
      }
      safeFolder.append(safePart);
    }

    return safeFolder.toString();
  }

  // Tao ten file luu tru rieng de tranh ghi de va lo ki tu nguy hiem.
  private String buildStoredFileName(String fileName) {
    String originalName = Paths.get(fileName).getFileName().toString();
    String normalizedName = Normalizer.normalize(originalName, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");
    String safeName = SAFE_FILE_CHARS.matcher(normalizedName).replaceAll("-");
    if (safeName.isBlank() || ".".equals(safeName) || "..".equals(safeName)) {
      safeName = "upload-file";
    }
    return UUID.randomUUID() + "-" + safeName;
  }

  // Chuan hoa tung phan cua duong dan thu muc dich.
  private String sanitizePathPart(String value) {
    String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");
    return SAFE_FILE_CHARS.matcher(normalized).replaceAll("-");
  }

  // Tao URL public ung voi resource handler /uploads/**.
  private String toPublicUrl(Path finalPath) {
    String relativePath = Paths.get(BASE_DIR).relativize(finalPath).toString().replace('\\', '/');
    return "/uploads/" + relativePath;
  }

  // Lay thu muc goc chua chunk tam, tach khoi /uploads public.
  private Path getTempRootPath() {
    return Paths.get(TMP_BASE_DIR).normalize();
  }

  // Dam bao cac lenh xoa chunk chi tac dong trong thu muc upload-tmp.
  private void ensureTempPath(Path tempPath) {
    Path tempRoot = getTempRootPath().toAbsolutePath().normalize();
    Path absolutePath = tempPath.toAbsolutePath().normalize();
    if (!absolutePath.startsWith(tempRoot)) {
      throw new IllegalArgumentException("Invalid upload temp path");
    }
  }
}
