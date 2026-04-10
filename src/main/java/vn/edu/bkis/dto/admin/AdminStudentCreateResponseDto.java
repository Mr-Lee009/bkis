package vn.edu.bkis.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response returned after creating a student from the admin page.
 */
@Getter
@AllArgsConstructor
public class AdminStudentCreateResponseDto {
    private final String id;
    private final String username;
    private final String fullName;
    private final String email;
    private final Long courseId;
    private final String courseName;
}
