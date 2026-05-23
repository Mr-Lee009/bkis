package vn.edu.bkis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.bkis.model.UploadSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class UploadService {

  private static final String BASE_DIR = "uploads/";
  private static final String DEFAULT_TARGET_FOLDER = "/source";
  private static final Pattern SAFE_FILE_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

  private final Map<String, UploadSession> sessionMap = new ConcurrentHashMap<>();

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

    Path basePath = Paths.get(BASE_DIR);
    Path uploadPath = basePath.resolve(uploadId);

    try {
      // Tạo luôn cả folder cha nếu chưa có
      Files.createDirectories(uploadPath);

    } catch (FileAlreadyExistsException e) {
      // edge case: path tồn tại nhưng không phải directory
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

    Path dirPath = Paths.get(session.getDirPath());

    try {
      // ✅ Nếu chưa có thì tạo luôn
      Files.createDirectories(dirPath);
    } catch (IOException e) {
      throw new IOException("Cannot create directory: " + dirPath, e);
    }

    Path chunkPath = dirPath.resolve("chunk_" + chunkIndex);

    try {
      // ✅ ghi file an toàn hơn
      Files.copy(file.getInputStream(), chunkPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IOException("Failed to save chunk " + chunkIndex, e);
    }

    session.getUploadedChunks().add(chunkIndex);
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

    Path finalDir = Paths.get(BASE_DIR).resolve(session.getTargetFolder());
    Files.createDirectories(finalDir);

    Path finalPath = finalDir.resolve(session.getStoredFileName());

    try (FileOutputStream fos = new FileOutputStream(finalPath.toFile())) {
      for (int i = 0; i < session.getTotalChunks(); i++) {
        File chunkFile = new File(session.getDirPath() + "/chunk_" + i);

        if (!chunkFile.exists()) {
          throw new IllegalStateException("Missing chunk: " + i);
        }

        Files.copy(chunkFile.toPath(), fos);
      }
    } catch (IOException | RuntimeException e) {
      Files.deleteIfExists(finalPath);
      throw e;
    }

    // cleanup
    deleteDirectory(new File(session.getDirPath()));
    sessionMap.remove(uploadId);

    return toPublicUrl(finalPath);
  }

  // Xoa thu muc tam sau khi upload hoan tat hoac gap loi can don dep.
  private void deleteDirectory(File dir) {
    if (dir.isDirectory()) {
      for (File file : Objects.requireNonNull(dir.listFiles())) {
        deleteDirectory(file);
      }
    }
    dir.delete();
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
}
