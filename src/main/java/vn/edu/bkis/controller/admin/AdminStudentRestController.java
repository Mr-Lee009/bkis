package vn.edu.bkis.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.bkis.dto.ApiResponse;
import vn.edu.bkis.dto.admin.AdminStudentListPageDto;
import vn.edu.bkis.dto.admin.AdminStudentSummaryDto;
import vn.edu.bkis.service.admin.AdminStudentQueryService;

/**
 * REST APIs for the admin students page.
 */
@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentRestController {
    private final AdminStudentQueryService adminStudentQueryService;

    /**
     * Create the controller with the admin student query service.
     *
     * @param adminStudentQueryService the query service
     */
    public AdminStudentRestController(AdminStudentQueryService adminStudentQueryService) {
        this.adminStudentQueryService = adminStudentQueryService;
    }

    /**
     * Get the paginated student list for the admin students page.
     *
     * @param keyword free-text keyword
     * @param status requested student status filter
     * @param page requested zero-based page index
     * @param size requested page size
     * @return paginated student list
     */
    @GetMapping
    public ApiResponse<AdminStudentListPageDto> getStudents(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.success(adminStudentQueryService.getStudents(keyword, status, page, size));
    }

    /**
     * Get summary metrics for the admin student page cards.
     *
     * @return summary metrics
     */
    @GetMapping("/summary")
    public ApiResponse<AdminStudentSummaryDto> getStudentSummary() {
        return ApiResponse.success(adminStudentQueryService.getStudentSummary());
    }
}
