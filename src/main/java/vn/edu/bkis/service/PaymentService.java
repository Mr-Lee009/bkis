package vn.edu.bkis.service;

import org.springframework.stereotype.Service;
import vn.edu.bkis.dto.payment.CreatePaymentRequestDto;
import vn.edu.bkis.dto.payment.ResponseCreatePaymentDto;

@Service
public class PaymentService {

    /**
     * Tao lenh thanh toan tam thoi cho controller trong giai doan chua co luong checkout day du.
     *
     * @param request thong tin yeu cau tao giao dich
     * @param provider ma cong thanh toan duoc chon
     * @return ket qua tao giao dich tam thoi; hien tai tra ve null cho phan chua hoan thien
     */
    public ResponseCreatePaymentDto createPayment(CreatePaymentRequestDto request, String provider) {
        // Step 1: giu compile on dinh cho luong payment hien tai trong khi schema moi dang duoc chuan hoa.
        // Step 2: se thay bang implementation day du khi checkout flow duoc hoan thien.
        return null;
    }
}
