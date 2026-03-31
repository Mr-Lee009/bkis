package vn.edu.bkis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadFileS3Controller {
  @GetMapping("/")
  public String upload() {
    return "05-upload-file";
  }
}
