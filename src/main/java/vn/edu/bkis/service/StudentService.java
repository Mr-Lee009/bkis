package vn.edu.bkis.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import vn.edu.bkis.model.Student;

@Service
public class StudentService {
    private final List<Student> students = new ArrayList<>();

    public StudentService() {
        // Sample data
        students.add(new Student(1L, "Nguyễn Văn A", "a@example.com"));
        students.add(new Student(2L, "Trần Thị B", "b@example.com"));
        students.add(new Student(3L, "Lê Văn C", "c@example.com"));
    }

    public List<Student> findAll() {
        return Collections.unmodifiableList(students);
    }

    public void add(Student student) {
        students.add(student);
    }
}
