package vn.edu.bkis.dto.admin.payment;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayDto {
    private Long id;
    private String code;
    private String displayName;
    private String providerType;
    private String description;
    private String merchantId;
    private String partnerCode;
    private String maskedSecretKey;
    private String paymentEndpoint;
    private String returnUrl;
    private String webhookUrl;
    private String ipAllowlist;
    private Boolean enabled;
    private Boolean sandboxMode;
    private Integer routingPriority;
    private BigDecimal transactionFeePercent;
    private BigDecimal successRatePercent;
    private String status;
    private String statusBadgeClass;
}
