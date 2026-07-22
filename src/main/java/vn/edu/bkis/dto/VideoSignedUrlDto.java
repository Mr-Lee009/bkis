package vn.edu.bkis.dto;

/**
 * Phan hoi chua duong dan xem video da ky va thoi diem het han.
 * @param url URL stream da ky
 * @param expiresAt epoch seconds het han cua URL
 */
public record VideoSignedUrlDto(String url, long expiresAt) {
}
