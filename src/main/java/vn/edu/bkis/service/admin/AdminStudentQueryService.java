package vn.edu.bkis.service.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.admin.student.AdminStudentDetailDto;
import vn.edu.bkis.dto.admin.student.AdminStudentDetailProjection;
import vn.edu.bkis.dto.admin.student.AdminStudentListItemDto;
import vn.edu.bkis.dto.admin.student.AdminStudentListPageDto;
import vn.edu.bkis.dto.admin.student.AdminStudentListProjection;
import vn.edu.bkis.dto.admin.student.AdminStudentSummaryDto;
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

    /**
     * Load one student profile for the admin detail modal.
     *
     * @param studentId requested student id
     * @return student detail DTO
     */
    @Transactional(readOnly = true)
    public AdminStudentDetailDto getStudentDetail(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Ma hoc vien la bat buoc.");
        }

        AdminStudentDetailProjection projection = userRepository.findAdminStudentDetailById(studentId.trim())
            .orElseThrow(() -> new IllegalArgumentException("Khong tim thay hoc vien."));

        String normalizedStatus = normalizeEnrollmentStatus(projection.getEnrollmentStatus());
        BioInfo bioInfo = parseBio(projection.getBio());
        long completedVideos = safeLong(projection.getCompletedVideos());
        long totalVideos = safeLong(projection.getTotalVideos());
        int progressPercent = calculateProgressPercent(completedVideos, totalVideos);

        return new AdminStudentDetailDto(
            projection.getId(),
            projection.getUsername(),
            projection.getFullName(),
            projection.getEmail(),
            projection.getProfilePictureUrl(),
            projection.getBio(),
            Boolean.TRUE.equals(projection.getLocked()),
            projection.getJoinedAt(),
            projection.getCourseId(),
            isBlank(projection.getCourseName()) ? "Chua ghi danh" : projection.getCourseName(),
            normalizedStatus,
            toStatusLabel(normalizedStatus),
            projection.getEnrolledAt(),
            projection.getExpiresAt(),
            progressPercent,
            completedVideos,
            totalVideos,
            toProgressLabel(completedVideos, totalVideos),
            projection.getLastActivityAt(),
            bioInfo.cohortCode(),
            bioInfo.mentorId(),
            bioInfo.goals(),
            bioInfo.note()
        );
    }

    private AdminStudentListItemDto toItemDto(AdminStudentListProjection projection) {
        String normalizedStatus = normalizeEnrollmentStatus(projection.getEnrollmentStatus());
        String courseName = isBlank(projection.getCourseName()) ? "Chua ghi danh" : projection.getCourseName();
        long completedVideos = safeLong(projection.getCompletedVideos());
        long totalVideos = safeLong(projection.getTotalVideos());

        return new AdminStudentListItemDto(
            projection.getId(),
            projection.getUsername(),
            projection.getFullName(),
            projection.getEmail(),
            courseName,
            calculateProgressPercent(completedVideos, totalVideos),
            toProgressLabel(completedVideos, totalVideos),
            normalizedStatus,
            toStatusLabel(normalizedStatus),
            projection.getJoinedAt(),
            Boolean.TRUE.equals(projection.getLocked())
        );
    }

    private String toStatusLabel(String status) {
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> "Dang hoc";
            case "CANCELLED" -> "Da huy";
            case "EXPIRED" -> "Het han";
            case "NOT_ENROLLED" -> "Chua ghi danh";
            default -> status;
        };
    }

    private String normalizeEnrollmentStatus(String rawStatus) {
        return isBlank(rawStatus) ? "NOT_ENROLLED" : rawStatus;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int calculateProgressPercent(long completedVideos, long totalVideos) {
        if (totalVideos <= 0) {
            return 0;
        }
        return (int) Math.round((completedVideos * 100.0) / totalVideos);
    }

    private String toProgressLabel(long completedVideos, long totalVideos) {
        if (totalVideos <= 0) {
            return "Chua co bai hoc";
        }
        return completedVideos + "/" + totalVideos + " video";
    }

    private BioInfo parseBio(String bio) {
        if (isBlank(bio)) {
            return new BioInfo(null, null, List.of(), null);
        }

        String goalsText = extractBioValue(bio, "Goals:");
        List<String> goals = new ArrayList<>();
        if (!isBlank(goalsText)) {
            for (String goal : goalsText.split(",")) {
                if (!goal.isBlank()) {
                    goals.add(goal.trim());
                }
            }
        }

        return new BioInfo(
            extractBioValue(bio, "Cohort:"),
            extractBioValue(bio, "MentorId:"),
            goals,
            extractBioValue(bio, "Note:")
        );
    }

    private String extractBioValue(String bio, String marker) {
        int start = bio.indexOf(marker);
        if (start < 0) {
            return null;
        }

        int valueStart = start + marker.length();
        int nextMarker = bio.length();
        for (String candidate : List.of(" Cohort:", " MentorId:", " Goals:", " Note:")) {
            int index = bio.indexOf(candidate, valueStart);
            if (index >= 0 && index < nextMarker) {
                nextMarker = index;
            }
        }

        String value = bio.substring(valueStart, nextMarker).trim();
        if (value.endsWith(".")) {
            value = value.substring(0, value.length() - 1).trim();
        }
        return value.isBlank() ? null : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

    private record BioInfo(String cohortCode, String mentorId, List<String> goals, String note) {
    }
}
