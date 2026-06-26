package vn.edu.bkis.util;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VietnameseNameUtil {

    // Danh sách các Họ phổ biến tại Việt Nam
    private static final List<String> LAST_NAMES = Arrays.asList(
            "Nguyễn", "Trần", "Lê", "Phạm", "Huỳnh", "Hoàng", "Vũ", "Võ",
            "Phan", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý"
    );

    // Danh sách Tên đệm phổ biến
    private static final List<String> MIDDLE_NAMES = Arrays.asList(
            "Văn", "Thị", "Đức", "Ngọc", "Minh", "Quốc", "Gia", "Hoàng",
            "Khánh", "Phương", "Thanh", "Đình", "Hải", "Tuấn", "Thành"
    );

    // Danh sách Tên chính phổ biến (Nam và Nữ)
    private static final List<String> FIRST_NAMES = Arrays.asList(
            "Anh", "Bảo", "Chi", "Dũng", "Dương", "Giang", "Hà", "Hải",
            "Hằng", "Hiếu", "Hoa", "Hùng", "Huy", "Hương", "Khánh", "Khoa",
            "Linh", "Long", "Mai", "Minh", "Nam", "Ngọc", "Như", "Phong",
            "Phúc", "Phương", "Quân", "Quang", "Quỳnh", "Sơn", "Tâm", "Thảo",
            "Thiên", "Thu", "Trang", "Trí", "Tuấn", "Tùng", "Uyên", "Việt",
            "Vũ", "Vy"
    );

    private static final Random RANDOM = new Random();

    /**
     * Lấy ngẫu nhiên một phần tử từ danh sách
     */
    private static String getRandomElement(List<String> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

    /**
     * Sinh ngẫu nhiên Họ tên đầy đủ (Họ + Tên đệm + Tên)
     */
    public static String getRandomFullName() {
        String lastName = getRandomElement(LAST_NAMES);
        String middleName = getRandomElement(MIDDLE_NAMES);
        String firstName = getRandomElement(FIRST_NAMES);

        return lastName + " " + middleName + " " + firstName;
    }

    /**
     * Sinh ngẫu nhiên chỉ Tên chính (Dùng khi hệ thống tách cột Họ và Tên)
     */
    public static String getRandomFirstNameOnly() {
        return getRandomElement(FIRST_NAMES);
    }

    // Hàm test thử nghiệm
//    public static void main(String[] args) {
//        System.out.println("Tên ngẫu nhiên 1: " + getRandomFullName());
//        System.out.println("Tên ngẫu nhiên 2: " + getRandomFullName());
//        System.out.println("Tên ngẫu nhiên 3: " + getRandomFullName());
//    }
}