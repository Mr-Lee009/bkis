package vn.edu.bkis.dto.payment;

import lombok.Data;
import vn.edu.bkis.model.PaymentStatus;

/**
 * DTO trả về thông tin chi tiết payment hiện tại.
 * Dùng cho frontend poll trạng thái hoặc hiển thị kết quả thanh toán.
 */
@Data
public class PaymentDetailResponseDto {
    // Mã payment nội bộ.
    private String paymentCode;

    // Mã đơn hàng nội bộ.
    private String orderId;

    // Provider xử lý giao dịch.
    private PaymentProvider provider;

    // Trạng thái hiện tại của payment.
    private PaymentStatus status;

    // Số tiền của giao dịch.
    private Long amount;

    // Mã giao dịch thật phía gateway nếu đã có.
    private String gatewayTransactionNo;
}
