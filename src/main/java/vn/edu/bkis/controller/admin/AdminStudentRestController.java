package vn.edu.bkis.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.bkis.dto.ApiResponse;
import vn.edu.bkis.dto.admin.student.AdminStudentCreateRequest;
import vn.edu.bkis.dto.admin.student.AdminStudentCreateResponseDto;
import vn.edu.bkis.dto.admin.student.AdminStudentDetailDto;
import vn.edu.bkis.dto.admin.student.AdminStudentFormOptionsDto;
import vn.edu.bkis.dto.admin.student.AdminStudentListPageDto;
import vn.edu.bkis.dto.admin.student.AdminStudentSummaryDto;
import vn.edu.bkis.service.admin.AdminStudentCommandService;
import vn.edu.bkis.service.admin.AdminStudentQueryService;

/**
 * REST APIs for the admin students page.
 */
@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentRestController {
    private final AdminStudentCommandService adminStudentCommandService;
    private final AdminStudentQueryService adminStudentQueryService;

    /**
     * Create the controller with the admin student query service.
     *
     * @param adminStudentQueryService the query service
     */
    public AdminStudentRestController(
        AdminStudentCommandService adminStudentCommandService,
        AdminStudentQueryService adminStudentQueryService
    ) {
        this.adminStudentCommandService = adminStudentCommandService;
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

    /**
     * Get one student profile for the detail modal.
     *
     * @param studentId requested student id
     * @return student detail profile
     */
    @GetMapping("/{studentId}")
    public ApiResponse<AdminStudentDetailDto> getStudentDetail(@PathVariable String studentId) {
        return ApiResponse.success(adminStudentQueryService.getStudentDetail(studentId));
    }

    /**
     * Get course and mentor options for the add-student modal.
     *
     * @return form options
     */
    @GetMapping("/form-options")
    public ApiResponse<AdminStudentFormOptionsDto> getFormOptions() {
        return ApiResponse.success(adminStudentCommandService.getFormOptions());
    }

    /**
     * Create a new student from the admin modal.
     *
     * @param request submitted payload
     * @return created student response
     */
    @PostMapping
    public ApiResponse<AdminStudentCreateResponseDto> createStudent(@RequestBody AdminStudentCreateRequest request) {
        return ApiResponse.success(adminStudentCommandService.createStudent(request));
    }

    /**
     * Convert simple validation errors to readable 400 responses for AJAX calls.
     *
     * @param ex raised exception
     * @return response body with the error text
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
