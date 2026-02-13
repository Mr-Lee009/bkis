package vn.edu.bkis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.edu.bkis.service.StudentService;

@Controller
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/students")
    public String list(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "students";
    }
}
