package vn.edu.bkis.util;

/**
 * Tap helper dung chung cho cac payment gateway adapter.
 */
public final class PaymentGatewayUtil {

    private PaymentGatewayUtil() {
    }

    /**
     * Xay dung endpoint day du tu baseUrl va path cau hinh.
     *
     * @param baseUrl base url cua gateway
     * @param path path api can noi them
     * @return endpoint day du sau khi chuan hoa dau gach cheo
     */
    public static String buildEndpoint(String baseUrl, String path) {
        // Step 1: neu path rong thi tra ve nguyen baseUrl de giu cau hinh don gian.
        if (path == null || path.isBlank()) {
            return baseUrl;
        }

        // Step 2: noi baseUrl va path theo dung so dau gach cheo.
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl + path.substring(1);
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    /**
     * Lay gia tri string dau tien khong rong trong danh sach ung vien.
     *
     * @param values cac gia tri ung vien
     * @return gia tri hop le dau tien; null neu khong tim thay
     */
    public static String firstNonBlank(String... values) {
        // Step 1: chan som truong hop danh sach null.
        if (values == null) {
            return null;
        }

        // Step 2: duyet tu trai sang phai va tra ve gia tri hop le dau tien.
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Chuyen object ve string an toan.
     *
     * @param value gia tri dau vao
     * @return chuoi string hoac null neu value null
     */
    public static String stringValue(Object value) {
        // Step 1: tra ve null neu gia tri khong ton tai.
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Chuyen object ve long voi fallback an toan.
     *
     * @param value gia tri dau vao
     * @param fallback gia tri mac dinh neu parse that bai
     * @return gia tri long hop le
     */
    public static Long longValue(Object value, Long fallback) {
        // Step 1: tra ve fallback neu khong co du lieu.
        if (value == null) {
            return fallback;
        }

        // Step 2: parse sang long va fallback neu du lieu khong dung dinh dang so.
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    /**
     * Chuyen object ve int voi fallback an toan.
     *
     * @param value gia tri dau vao
     * @param fallback gia tri mac dinh neu parse that bai
     * @return gia tri int hop le
     */
    public static int intValue(Object value, int fallback) {
        // Step 1: tra ve fallback neu khong co du lieu.
        if (value == null) {
            return fallback;
        }

        // Step 2: parse sang int va fallback neu du lieu khong dung dinh dang so.
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    /**
     * Chuyen object ve boolean voi fallback an toan.
     *
     * @param value gia tri dau vao
     * @param fallback gia tri mac dinh neu value null
     * @return gia tri boolean hop le
     */
    public static boolean booleanValue(Object value, boolean fallback) {
        // Step 1: tra ve fallback neu khong co du lieu.
        if (value == null) {
            return fallback;
        }

        // Step 2: parse sang boolean theo quy tac mac dinh cua Java.
        return Boolean.parseBoolean(String.valueOf(value));
    }

    /**
     * Chuan hoa chuoi rong thanh null.
     *
     * @param value gia tri can xu ly
     * @return null neu rong; nguoc lai tra ve chuoi da trim
     */
    public static String blankToNull(String value) {
        // Step 1: tra ve null neu gia tri rong hoac chi co khoang trang.
        if (value == null || value.isBlank()) {
            return null;
        }

        // Step 2: trim chuoi truoc khi tra ve de dong bo du lieu.
        return value.trim();
    }
}
