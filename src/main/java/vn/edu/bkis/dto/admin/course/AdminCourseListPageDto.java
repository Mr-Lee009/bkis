package vn.edu.bkis.dto.admin.course;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Composite DTO for the admin courses list page.
 */
@Getter
@AllArgsConstructor
public class AdminCourseListPageDto {
    private final AdminCourseSummaryDto summary;
    private final List<AdminCourseListItemDto> courses;
    private final List<Integer> years;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;

    public int getDisplayPage() {
        return currentPage + 1;
    }

    public boolean isHasPrevious() {
        return currentPage > 0;
    }

    public boolean isHasNext() {
        return currentPage + 1 < totalPages;
    }

    public long getStartRow() {
        return totalItems == 0 ? 0 : ((long) currentPage * pageSize) + 1;
    }

    public long getEndRow() {
        return Math.min(totalItems, ((long) currentPage + 1) * pageSize);
    }
}
