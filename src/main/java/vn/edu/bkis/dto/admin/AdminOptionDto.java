package vn.edu.bkis.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Generic option DTO for select inputs.
 */
@Getter
@AllArgsConstructor
public class AdminOptionDto {
    private final String value;
    private final String label;
}
