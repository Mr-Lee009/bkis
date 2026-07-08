package vn.edu.bkis.dto.payment;

import lombok.Data;
import vn.edu.bkis.model.PaymentStatus;

@Data
public class ResponseCreatePaymentDto {

    private String paymentCode;

    private String orderId;

    private PaymentProvider provider;

    private PaymentStatus status;

    private Long amount;

    private String paymentUrl;
}
