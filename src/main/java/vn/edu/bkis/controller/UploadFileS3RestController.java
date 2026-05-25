package vn.edu.bkis.controller;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.edu.bkis.service.UploadService;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadFileS3RestController {

  private final UploadService uploadService;

  @PostMapping("/init")
  public ResponseEntity<?> init(@RequestParam String fileName, @RequestParam long totalSize,
      @RequestParam int totalChunks, @RequestParam(required = false) String folder) {
    try {
      String uploadId = uploadService.initUpload(fileName, totalSize, totalChunks, folder);
      return ResponseEntity.ok(uploadId);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Init upload failed");
    }
  }

  // Tao phien upload tra ve JSON de cac man hinh co the tai su dung.
  @PostMapping("/api/init")
  public ResponseEntity<?> initApi(@RequestParam String fileName, @RequestParam long totalSize,
      @RequestParam int totalChunks, @RequestParam(required = false) String folder) {
    try {
      String uploadId = uploadService.initUpload(fileName, totalSize, totalChunks, folder);
      return ResponseEntity.ok(Map.of("uploadId", uploadId));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("message", "Init upload failed"));
    }
  }

  @PostMapping("/chunk")
  public ResponseEntity<?> uploadChunk(@RequestParam String uploadId, @RequestParam int chunkIndex,
      @RequestParam MultipartFile file) {
    try {
      uploadService.saveChunk(uploadId, chunkIndex, file);
      return ResponseEntity.ok("Chunk uploaded");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IOException e) {
      return ResponseEntity.internalServerError().body("IO error while saving chunk");
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Upload chunk failed");
    }
  }

  // Nhan chunk upload va tra ve JSON de UI hien thi loi ro rang.
  @PostMapping("/api/chunk")
  public ResponseEntity<?> uploadChunkApi(@RequestParam String uploadId, @RequestParam int chunkIndex,
      @RequestParam MultipartFile file) {
    try {
      uploadService.saveChunk(uploadId, chunkIndex, file);
      return ResponseEntity.ok(Map.of("message", "Chunk uploaded"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (IOException e) {
      return ResponseEntity.internalServerError().body(Map.of("message", "IO error while saving chunk"));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("message", "Upload chunk failed"));
    }
  }

  @PostMapping("/complete")
  public ResponseEntity<?> complete(@RequestParam String uploadId, HttpServletRequest request) {
    try {
      String filePath = uploadService.completeUpload(uploadId);
      return ResponseEntity.ok(toAbsoluteFileUrl(filePath, request));
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IOException e) {
      return ResponseEntity.internalServerError().body("Error merging chunks");
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Complete upload failed");
    }
  }

  // Huy upload dang do dang va xoa chunk tam tren server.
  @PostMapping("/abort")
  public ResponseEntity<?> abort(@RequestParam String uploadId) {
    try {
      boolean deleted = uploadService.abortUpload(uploadId);
      return ResponseEntity.ok(deleted ? "Upload aborted" : "Upload session not found");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Abort upload failed");
    }
  }

  // Ghep file va tra ve URL public sau khi upload hoan tat.
  @PostMapping("/api/complete")
  public ResponseEntity<?> completeApi(@RequestParam String uploadId, HttpServletRequest request) {
    try {
      String fileUrl = uploadService.completeUpload(uploadId);
      return ResponseEntity.ok(Map.of("url", toAbsoluteFileUrl(fileUrl, request)));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (IOException e) {
      return ResponseEntity.internalServerError().body(Map.of("message", "Error merging chunks"));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("message", "Complete upload failed"));
    }
  }

  // Huy upload dang do dang va tra ve JSON cho cac widget upload.
  @PostMapping("/api/abort")
  public ResponseEntity<?> abortApi(@RequestParam String uploadId) {
    try {
      boolean deleted = uploadService.abortUpload(uploadId);
      return ResponseEntity.ok(Map.of("aborted", deleted));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("message", "Abort upload failed"));
    }
  }

  // Tao URL day du domain theo request hien tai de client co the xem file truc tiep.
  private String toAbsoluteFileUrl(String filePath, HttpServletRequest request) {
    String normalizedPath = filePath.startsWith("/") ? filePath : "/" + filePath;
    return ServletUriComponentsBuilder.fromRequestUri(request)
        .replacePath(request.getContextPath() + normalizedPath)
        .replaceQuery(null)
        .build()
        .toUriString();
  }
}
