package vn.edu.bkis.dto.admin.course;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * One row in the admin courses table.
 */
@Getter
@AllArgsConstructor
public class AdminCourseListItemDto {
    private final Long id;
    private final String title;
    private final String tag;
    private final Integer year;
    private final String teacherName;
    private final long enrolledStudents;
    private final BigDecimal revenue;
    private final long moduleCount;
    private final long videoCount;
    private final String status;
    private final String statusLabel;
    private final boolean visible;
    private final String updatedAt;

    // Trả về CSS badge phù hợp với trạng thái nghiệp vụ của khóa học trên bảng danh sách.
    public String getStatusBadgeClass() {
        return switch (status) {
            case "DRAFT" -> "badge bg-secondary";
            case "REVIEW" -> "badge bg-warning text-dark";
            case "PUBLISHED" -> "badge bg-success";
            case "ARCHIVED" -> "badge bg-dark";
            default -> "badge bg-light text-dark";
        };
    }
}
