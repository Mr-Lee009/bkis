package vn.edu.bkis.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.edu.bkis.service.UploadService;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadFileS3RestController {

    private final UploadService uploadService;

    @PostMapping("/init")
    public ResponseEntity<?> init(@RequestParam String fileName,
                                 @RequestParam long totalSize,
                                 @RequestParam int totalChunks) {
        try {
            String uploadId = uploadService.initUpload(fileName, totalSize, totalChunks);
            return ResponseEntity.ok(uploadId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Init upload failed");
        }
    }

    @PostMapping("/chunk")
    public ResponseEntity<?> uploadChunk(@RequestParam String uploadId,
                                         @RequestParam int chunkIndex,
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

    @PostMapping("/complete")
    public ResponseEntity<?> complete(@RequestParam String uploadId) {
        try {
            String filePath = uploadService.completeUpload(uploadId);
            return ResponseEntity.ok(filePath);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error merging chunks");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Complete upload failed");
        }
    }
}
