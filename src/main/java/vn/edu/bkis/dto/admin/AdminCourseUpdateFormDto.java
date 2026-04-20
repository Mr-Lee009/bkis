package vn.edu.bkis.dto.admin;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Form payload for updating a course from the admin detail page.
 */
@Getter
@Setter
public class AdminCourseUpdateFormDto {
    private Long id;
    private String title;
    private String description;
    private String highlights;
    private String teacherId;
    private BigDecimal price;
    private String tag;
    private String imageUrl;
    private String status;
    private Boolean visible;
}
