package vn.edu.bkis.service.gateway.vnpay;

import org.springframework.stereotype.Component;
import vn.edu.bkis.dto.payment.CreatePaymentRequestDto;
import vn.edu.bkis.dto.payment.GatewayCreatePaymentResult;
import vn.edu.bkis.dto.payment.PaymentProvider;
import vn.edu.bkis.model.PaymentGatewayConfigEntity;
import vn.edu.bkis.service.gateway.PaymentGateway;

/**
 * Khung adapter VNPay. Tam thoi chi dung de resolve provider va se duoc hoan thien sau.
 */
@Component
public class VnPayPaymentGateway implements PaymentGateway {

    /**
     * Tra ve provider VNPay.
     *
     * @return enum provider VNPay
     */
    @Override
    public PaymentProvider provider() {
        return PaymentProvider.VN_PAY;
    }

    /**
     * Khung tao payment VNPay.
     *
     * @param request request thanh toan chung
     * @param config cau hinh gateway VNPay
     * @return ket qua trong trang thai TODO
     * @throws UnsupportedOperationException luon nem ra vi adapter chua hoan thien
     */
    @Override
    public GatewayCreatePaymentResult createPayment(CreatePaymentRequestDto request,
        PaymentGatewayConfigEntity config) {
        // Step 1: ghi ro adapter nay chua hoan thien de tranh hieu nham la da chay production.
        throw new UnsupportedOperationException("VNPay adapter is TODO.");
    }
}