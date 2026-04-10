package vn.edu.bkis.dto.admin;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Master data needed by the add-student modal.
 */
@Getter
@AllArgsConstructor
public class AdminStudentFormOptionsDto {
    private final List<AdminOptionDto> courses;
    private final List<AdminOptionDto> mentors;
}
