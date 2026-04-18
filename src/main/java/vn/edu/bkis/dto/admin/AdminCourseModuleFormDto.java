package vn.edu.bkis.dto.admin;

import lombok.Getter;
import lombok.Setter;

/**
 * Form payload for creating or updating a course module.
 */
@Getter
@Setter
public class AdminCourseModuleFormDto {
    private Long id;
    private String title;
    private String description;
    private Integer position;
}
