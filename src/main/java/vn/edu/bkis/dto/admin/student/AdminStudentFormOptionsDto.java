package vn.edu.bkis.dto.admin.student;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.edu.bkis.dto.admin.AdminOptionDto;

/**
 * Master data needed by the add-student modal.
 */
@Getter
@AllArgsConstructor
public class AdminStudentFormOptionsDto {
    private final List<AdminOptionDto> courses;
    private final List<AdminOptionDto> mentors;
}
