package vn.edu.bkis.dto.payment;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreatePaymentRequestDto {

    private Long courseId;

    private String paymentMethod;

    private boolean acceptedTerms;

    private String phone;

    private String learningGoal;

    private String learningMode;

    private String couponCode;

    private String orderId;

    private PaymentProvider provider;

    private BigDecimal amount;

    private String description;

    private String returnUrl;
}
