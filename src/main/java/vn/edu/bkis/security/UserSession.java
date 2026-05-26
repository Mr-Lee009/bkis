package vn.edu.bkis.security;

/**
 * Đối tượng phiên người dùng rút gọn để service và controller dùng chung thông tin đăng nhập hiện tại.
 *
 * @param userId id định danh local của người dùng trong hệ thống
 * @param username username duy nhất của người dùng để dùng cho audit hoặc log
 * @param fullName tên hiển thị của người dùng trên giao diện
 * @param email email hiện tại của người dùng
 * @param role role local của người dùng trong hệ thống
 * @param loginProvider nguồn đăng nhập hiện tại như LOCAL, GOOGLE hoặc FACEBOOK
 * @param authenticated cờ cho biết principal hiện tại đã xác thực hợp lệ hay chưa
 */
public record UserSession(String userId, String username, String fullName, String email,
                          String role, String loginProvider, boolean authenticated) {
    /**
     * Tạo phiên người dùng ẩn danh để các bean theo request vẫn inject được khi chưa đăng nhập.
     *
     * @return {@link UserSession} mặc định cho ngữ cảnh chưa có người dùng xác thực
     */
    public static UserSession anonymous() {
        return new UserSession(null, null, null, null, null, null, false);
    }

    /**
     * Trả về actor dùng cho các field audit như createdBy và updatedBy.
     *
     * @return username nếu người dùng đã đăng nhập, ngược lại trả về `system`
     */
    public String auditActor() {
        return username != null && !username.isBlank() ? username : "system";
    }
}
