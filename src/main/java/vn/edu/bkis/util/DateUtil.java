package vn.edu.bkis.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

/**
 * Tiện ích tạo ngày và thời gian ngẫu nhiên phục vụ dữ liệu mẫu hoặc test.
 */
public class DateUtil {
    /**
     * Sinh một ngày ngẫu nhiên trong năm hiện tại.
     *
     * @return {@link LocalDate} nằm trong khoảng từ ngày đầu năm đến ngày cuối năm hiện tại
     */
    public static LocalDate getRandomDateInCurrentYear() {
        // Step 1: Lấy năm hiện tại và xác định ngày đầu tiên của năm.
        int currentYear = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);

        // Step 2: Xác định tổng số ngày hợp lệ trong năm để tránh sinh ngày vượt phạm vi.
        int lengthOfYear = startOfYear.lengthOfYear();

        // Step 3: Sinh số ngày ngẫu nhiên và cộng vào ngày đầu năm để tạo kết quả.
        Random random = new Random();
        int randomDays = random.nextInt(lengthOfYear);
        return startOfYear.plusDays(randomDays);
    }

    /**
     * Sinh một thời điểm ngẫu nhiên trong năm hiện tại.
     *
     * @return {@link LocalDateTime} gồm ngày ngẫu nhiên trong năm hiện tại và giờ phút giây ngẫu nhiên
     */
    public static LocalDateTime getRandomDateTimeInCurrentYear() {
        // Step 1: Lấy năm hiện tại và xác định ngày đầu tiên của năm.
        int currentYear = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);

        // Step 2: Xác định số ngày hợp lệ trong năm để sinh ngày ngẫu nhiên an toàn.
        int lengthOfYear = startOfYear.lengthOfYear();

        // Step 3: Sinh ngày ngẫu nhiên trong phạm vi năm hiện tại.
        Random random = new Random();
        int randomDays = random.nextInt(lengthOfYear);
        LocalDate randomDate = startOfYear.plusDays(randomDays);

        // Step 4: Sinh giờ, phút, giây ngẫu nhiên trong ngày.
        LocalTime randomTime = LocalTime.of(
                random.nextInt(24),
                random.nextInt(60),
                random.nextInt(60)
        );

        // Step 5: Ghép ngày và thời gian để trả về một LocalDateTime hoàn chỉnh.
        return LocalDateTime.of(randomDate, randomTime);
    }
}
