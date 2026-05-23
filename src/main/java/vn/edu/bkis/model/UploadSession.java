package vn.edu.bkis.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
public class UploadSession {
  private String fileName;
  private String storedFileName;
  private String targetFolder;
  private long totalSize;
  private int totalChunks;
  private String dirPath;
  private Set<Integer> uploadedChunks = ConcurrentHashMap.newKeySet();

  public UploadSession(String fileName, long totalSize, int totalChunks, String dirPath) {
    this.fileName = fileName;
    this.storedFileName = fileName;
    this.targetFolder = "";
    this.totalSize = totalSize;
    this.totalChunks = totalChunks;
    this.dirPath = dirPath;
    this.uploadedChunks = ConcurrentHashMap.newKeySet();
  }

  // Luu thong tin phien upload co kem thu muc dich va ten file da chuan hoa.
  public UploadSession(String fileName, String storedFileName, String targetFolder, long totalSize, int totalChunks,
      String dirPath) {
    this.fileName = fileName;
    this.storedFileName = storedFileName;
    this.targetFolder = targetFolder;
    this.totalSize = totalSize;
    this.totalChunks = totalChunks;
    this.dirPath = dirPath;
    this.uploadedChunks = ConcurrentHashMap.newKeySet();
  }
}
