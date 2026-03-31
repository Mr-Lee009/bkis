package vn.edu.bkis.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
public class UploadSession {
  private String fileName;
  private long totalSize;
  private int totalChunks;
  private String dirPath;
  private Set<Integer> uploadedChunks = ConcurrentHashMap.newKeySet();

  public UploadSession(String fileName, long totalSize, int totalChunks, String dirPath) {
    this.fileName = fileName;
    this.totalSize = totalSize;
    this.totalChunks = totalChunks;
    this.dirPath = dirPath;
    this.uploadedChunks = ConcurrentHashMap.newKeySet();
  }
}
