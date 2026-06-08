package vn.edu.bkis.dto.payment;

import vn.edu.bkis.model.PaymentStatus;

public class ResponseCreatePaymentDto {

    private String paymentCode;

    private String orderId;

    private PaymentProvider provider;

    private PaymentStatus status;

    private Long amount;

    private String paymentUrl;
}
