package vn.edu.bkis.dto.admin;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Paginated response for the admin students REST API.
 */
@Getter
@AllArgsConstructor
public class AdminStudentListPageDto {
    private final List<AdminStudentListItemDto> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;
}
