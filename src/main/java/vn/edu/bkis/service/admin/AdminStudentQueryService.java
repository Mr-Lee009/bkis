package vn.edu.bkis.service.admin;

import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.admin.AdminStudentListItemDto;
import vn.edu.bkis.dto.admin.AdminStudentListPageDto;
import vn.edu.bkis.dto.admin.AdminStudentListProjection;
import vn.edu.bkis.dto.admin.AdminStudentSummaryDto;
import vn.edu.bkis.repository.UserRepository;

/**
 * Query service for admin student REST APIs.
 */
@Service
public class AdminStudentQueryService {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;

    /**
     * Create the service with the user repository.
     *
     * @param userRepository the user repository
     */
    public AdminStudentQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Search students for the admin students table.
     *
     * @param keyword free-text keyword
     * @param status requested student status filter
     * @param page requested zero-based page index
     * @param size requested page size
     * @return paginated student list
     */
    @Transactional(readOnly = true)
    public AdminStudentListPageDto getStudents(String keyword, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(resolvePage(page), resolveSize(size));
        Page<AdminStudentListProjection> studentPage =
            userRepository.searchAdminStudents(normalize(keyword), normalizeStatus(status), pageable);

        return new AdminStudentListPageDto(
            studentPage.getContent().stream().map(this::toItemDto).toList(),
            studentPage.getNumber(),
            studentPage.getSize(),
            studentPage.getTotalElements(),
            studentPage.getTotalPages(),
            studentPage.hasNext(),
            studentPage.hasPrevious()
        );
    }

    /**
     * Load summary metrics for the admin student page.
     *
     * @return summary DTO
     */
    @Transactional(readOnly = true)
    public AdminStudentSummaryDto getStudentSummary() {
        return new AdminStudentSummaryDto(
            userRepository.countActiveStudentsForAdmin(),
            userRepository.countOnboardingStudentsForAdmin(),
            0L
        );
    }

    private AdminStudentListItemDto toItemDto(AdminStudentListProjection projection) {
        String rawStatus = projection.getEnrollmentStatus();
        String normalizedStatus = rawStatus == null || rawStatus.isBlank() ? "NOT_ENROLLED" : rawStatus;
        String courseName = projection.getCourseName() == null || projection.getCourseName().isBlank()
            ? "Chưa ghi danh"
            : projection.getCourseName();

        return new AdminStudentListItemDto(
            projection.getId(),
            projection.getUsername(),
            projection.getFullName(),
            projection.getEmail(),
            courseName,
            normalizedStatus,
            toStatusLabel(normalizedStatus),
            projection.getJoinedAt(),
            Boolean.TRUE.equals(projection.getLocked())
        );
    }

    private String toStatusLabel(String status) {
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> "Đang học";
            case "CANCELLED" -> "Đã hủy";
            case "EXPIRED" -> "Hết hạn";
            case "NOT_ENROLLED" -> "Chưa ghi danh";
            default -> status;
        };
    }

    private int resolvePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int resolveSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalize(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
    }
}
