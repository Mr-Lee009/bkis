package vn.edu.bkis.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/students")
public class StudentsController {
  @GetMapping("/")
  public String students() {
    return "admin/06-student";
  }
}
