package vn.edu.bkis.dto.admin.course;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data rendered on the admin course detail page.
 */
@Getter
@AllArgsConstructor
public class AdminCourseDetailDto {
    private final Long id;
    private final String title;
    private final String description;
    private final String highlights;
    private final String teacherId;
    private final String teacherName;
    private final BigDecimal price;
    private final String tag;
    private final String imageUrl;
    private final Integer rating;
    private final Integer year;
    private final String status;
    private final String statusLabel;
    private final boolean visible;
    private final long enrolledStudents;
    private final BigDecimal revenue;
    private final long moduleCount;
    private final long videoCount;
    private final long paymentCount;
    private final String createdAt;
    private final String updatedAt;
    private final int completionPercent;
    private final List<AdminCourseModuleDto> modules;

    // Trả về CSS badge phù hợp với trạng thái nghiệp vụ của khóa học trên trang chi tiết.
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
