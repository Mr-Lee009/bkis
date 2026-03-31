package vn.edu.bkis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.bkis.model.UploadSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UploadService {

  private static final String BASE_DIR = "uploads/";

  private final Map<String, UploadSession> sessionMap = new ConcurrentHashMap<>();

  public String initUpload(String fileName, long totalSize, int totalChunks) {

    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("File name is required");
    }

    if (totalChunks <= 0) {
      throw new IllegalArgumentException("Total chunks must be > 0");
    }

    String uploadId = UUID.randomUUID().toString();

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
        totalSize,
        totalChunks,
        uploadPath.toString()
    );

    sessionMap.put(uploadId, session);

    return uploadId;
  }

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

  public String completeUpload(String uploadId) throws IOException {
    UploadSession session = sessionMap.get(uploadId);

    if (session == null) {
      throw new IllegalArgumentException("Invalid uploadId");
    }

    if (session.getUploadedChunks().size() != session.getTotalChunks()) {
      throw new IllegalStateException("Not all chunks uploaded");
    }

    String finalPath = BASE_DIR + session.getFileName();
    FileOutputStream fos = new FileOutputStream(finalPath);

    try {
      for (int i = 0; i < session.getTotalChunks(); i++) {
        File chunkFile = new File(session.getDirPath() + "/chunk_" + i);

        if (!chunkFile.exists()) {
          throw new IllegalStateException("Missing chunk: " + i);
        }

        Files.copy(chunkFile.toPath(), fos);
      }
    } finally {
      fos.close();
    }

    // cleanup
    deleteDirectory(new File(session.getDirPath()));
    sessionMap.remove(uploadId);

    return finalPath;
  }

  private void deleteDirectory(File dir) {
    if (dir.isDirectory()) {
      for (File file : Objects.requireNonNull(dir.listFiles())) {
        deleteDirectory(file);
      }
    }
    dir.delete();
  }
}
