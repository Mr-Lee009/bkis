package vn.edu.bkis.dto.admin;

import lombok.Getter;
import lombok.Setter;

/**
 * Form payload for creating or updating a lesson video.
 */
@Getter
@Setter
public class AdminCourseVideoFormDto {
    private Long id;
    private Long lessonId;
    private String title;
    private String videoUrl;
    private Integer duration;
    private Integer position;
}
