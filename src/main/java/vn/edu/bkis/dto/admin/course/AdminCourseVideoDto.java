package vn.edu.bkis.dto.admin.course;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Video item under a course module on the admin course detail page.
 */
@Getter
@AllArgsConstructor
public class AdminCourseVideoDto {
    private final Long id;
    private final String title;
    private final String videoUrl;
    private final Integer duration;
    private final String durationLabel;
    private final Integer position;
    private final String statusLabel;
}
