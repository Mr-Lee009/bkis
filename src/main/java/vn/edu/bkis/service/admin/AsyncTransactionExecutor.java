package vn.edu.bkis.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bộ thực thi tác vụ bất đồng bộ trong ngữ cảnh transaction cho admin payment gateway.
 */
@Service
public class AsyncTransactionExecutor {

    @Autowired
    private AdminPaymentGatewayService paymentGatewayService;

    /**
     * Chạy tác vụ chèn dữ liệu lớn ở chế độ bất đồng bộ trong một transaction riêng.
     *
     * @throws RuntimeException nếu service con phát sinh lỗi trong quá trình chèn dữ liệu
     */
    @Async
    @Transactional
    public void insertOneMillionAsync() {
        // Step 1: Gọi service chuyên trách để thực hiện thao tác chèn dữ liệu lớn.
        paymentGatewayService.insertOneMillionRecords();
    }
}
