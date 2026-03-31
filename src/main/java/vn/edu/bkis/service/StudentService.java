package vn.edu.bkis.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import vn.edu.bkis.dto.StudentDto;

@Service
public class StudentService {
    private final List<StudentDto> studentDtos = new ArrayList<>();

    public StudentService() {
        // Sample data
        studentDtos.add(new StudentDto("1L", "Nguyễn Văn A", "a@example.com"));
        studentDtos.add(new StudentDto("2L", "Trần Thị B", "b@example.com"));
        studentDtos.add(new StudentDto("3L", "Lê Văn C", "c@example.com"));
    }

    public List<StudentDto> findAll() {
        return Collections.unmodifiableList(studentDtos);
    }

    public void add(StudentDto studentDto) {
        studentDtos.add(studentDto);
    }
}
