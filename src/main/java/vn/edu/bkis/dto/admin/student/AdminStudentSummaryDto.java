package vn.edu.bkis.dto.admin.student;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Summary data for the admin student page cards.
 */
@Getter
@AllArgsConstructor
public class AdminStudentSummaryDto {
    private final long activeStudents;
    private final long onboardingStudents;
    private final long openSupportTickets;
}
