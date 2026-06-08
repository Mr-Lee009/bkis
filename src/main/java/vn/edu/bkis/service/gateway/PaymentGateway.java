package vn.edu.bkis.service.gateway;

import vn.edu.bkis.dto.payment.CreatePaymentRequestDto;
import vn.edu.bkis.dto.payment.GatewayCreatePaymentResult;
import vn.edu.bkis.dto.payment.PaymentProvider;
import vn.edu.bkis.model.PaymentGatewayConfigEntity;
import vn.edu.bkis.model.PaymentTransactionEntity;

public interface PaymentGateway {
    PaymentProvider provider();

    GatewayCreatePaymentResult createPayment(CreatePaymentRequestDto request,
        PaymentGatewayConfigEntity config);

//    GatewayCallbackResult handleCallback(PaymentGatewayCallbackCommand command,
//        PaymentGatewayConfigEntity config);
//
//    GatewayQueryResult queryPayment(PaymentTransactionEntity transaction,
//        PaymentGatewayConfigEntity config);
}
