package vn.edu.bkis.dto.admin.student;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload used to create a student from the admin page modal.
 */
@Getter
@Setter
public class AdminStudentCreateRequest {
    private String fullName;
    private String email;
    private Long courseId;
    private String cohortCode;
    private LocalDate startDate;
    private String mentorId;
    private List<String> goals;
    private String note;
}
