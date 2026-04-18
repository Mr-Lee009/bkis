package vn.edu.bkis.dto.admin;

import lombok.Getter;
import lombok.Setter;

/**
 * Filter submitted by the admin courses list page.
 */
@Getter
@Setter
public class AdminCourseFilterDto {
    private String keyword;
    private Integer year;
    private String status;
    private Integer page;
    private Integer size;
}
