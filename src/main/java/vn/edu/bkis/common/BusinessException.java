package vn.edu.bkis.common;

public class BusinessException extends RuntimeException {
    private final String code;
    private final Object[] args;

    // Khởi tạo lỗi nghiệp vụ bằng mã message để controller resolve theo ngôn ngữ hiện tại.
    public BusinessException(String code, Object... args) {
        super(code);
        this.code = code;
        this.args = args == null ? new Object[0] : args;
    }

    // Lấy mã message i18n của lỗi nghiệp vụ.
    public String getCode() {
        return code;
    }

    // Lấy các tham số động dùng để thay vào message i18n.
    public Object[] getArgs() {
        return args;
    }
}
