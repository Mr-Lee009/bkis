package vn.edu.bkis.service.gateway.zalopay;

import org.springframework.stereotype.Component;
import vn.edu.bkis.dto.payment.CreatePaymentRequestDto;
import vn.edu.bkis.dto.payment.GatewayCreatePaymentResult;
import vn.edu.bkis.dto.payment.PaymentProvider;
import vn.edu.bkis.model.PaymentGatewayConfigEntity;
import vn.edu.bkis.service.gateway.PaymentGateway;

/**
 * Khung adapter ZaloPay. Tam thoi chi dung de resolve provider va se duoc hoan thien sau.
 */
@Component
public class ZaloPayPaymentGateway implements PaymentGateway {

    /**
     * Tra ve provider ZaloPay.
     *
     * @return enum provider ZaloPay
     */
    @Override
    public PaymentProvider provider() {
        return PaymentProvider.ZALO_PAY;
    }

    /**
     * Khung tao payment ZaloPay.
     *
     * @param request request thanh toan chung
     * @param config cau hinh gateway ZaloPay
     * @return ket qua trong trang thai TODO
     * @throws UnsupportedOperationException luon nem ra vi adapter chua hoan thien
     */
    @Override
    public GatewayCreatePaymentResult createPayment(CreatePaymentRequestDto request,
        PaymentGatewayConfigEntity config) {
        // Step 1: ghi ro adapter nay chua hoan thien de tranh hieu nham la da chay production.
        throw new UnsupportedOperationException("ZaloPay adapter is TODO.");
    }
}