package vn.edu.bkis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Generic API response wrapper.
 *
 * @param <T> response payload type
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final T data;

    /**
     * Build a successful response wrapper.
     *
     * @param data response payload
     * @return wrapped payload
     * @param <T> payload type
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }
}
