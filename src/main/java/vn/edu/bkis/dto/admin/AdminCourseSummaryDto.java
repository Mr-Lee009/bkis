package vn.edu.bkis.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Summary cards for the admin courses page.
 */
@Getter
@AllArgsConstructor
public class AdminCourseSummaryDto {
    private final long currentYearCourses;
    private final long draftCourses;
    private final long publishedCourses;
    private final long hiddenCourses;
    private final long totalCourses;
}
