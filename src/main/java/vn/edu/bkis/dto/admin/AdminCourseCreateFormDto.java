package vn.edu.bkis.dto.admin;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Form payload for creating a draft course from the admin courses list page.
 */
@Getter
@Setter
public class AdminCourseCreateFormDto {
    private String title;
    private String description;
    private String highlights;
    private String teacherId;
    private BigDecimal price;
    private String tag;
    private String imageUrl;
}
