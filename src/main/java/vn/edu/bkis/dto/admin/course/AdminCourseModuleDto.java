package vn.edu.bkis.dto.admin.course;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Module summary in the admin course detail page.
 */
@Getter
@AllArgsConstructor
public class AdminCourseModuleDto {
    private final Long id;
    private final String title;
    private final String description;
    private final Integer position;
    private final long videoCount;
    private final List<AdminCourseVideoDto> videos;
}
