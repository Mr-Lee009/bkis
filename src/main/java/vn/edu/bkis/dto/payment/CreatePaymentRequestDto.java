package vn.edu.bkis.dto.payment;

public class CreatePaymentRequestDto {

    private String orderId;

    private PaymentProvider provider;

    private Long amount;

    private String description;

    private String returnUr;
}
