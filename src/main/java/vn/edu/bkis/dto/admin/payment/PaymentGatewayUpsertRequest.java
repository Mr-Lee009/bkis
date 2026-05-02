package vn.edu.bkis.dto.admin.payment;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentGatewayUpsertRequest {
    private String code;
    private String displayName;
    private String providerType;
    private String description;
    private String merchantId;
    private String partnerCode;
    private String secretKey;
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
}
