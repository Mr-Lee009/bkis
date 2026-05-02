package vn.edu.bkis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_gateways")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGateway extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "provider_type", nullable = false, length = 50)
    private String providerType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "merchant_id", length = 150)
    private String merchantId;

    @Column(name = "partner_code", length = 150)
    private String partnerCode;

    @Column(name = "secret_key", length = 500)
    private String secretKey;

    @Column(name = "payment_endpoint", length = 500)
    private String paymentEndpoint;

    @Column(name = "return_url", length = 500)
    private String returnUrl;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "ip_allowlist", columnDefinition = "TEXT")
    private String ipAllowlist;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "sandbox_mode")
    private Boolean sandboxMode = false;

    @Column(name = "routing_priority")
    private Integer routingPriority = 99;

    @Column(name = "transaction_fee_percent", precision = 8, scale = 2)
    private BigDecimal transactionFeePercent = BigDecimal.ZERO;

    @Column(name = "success_rate_percent", precision = 8, scale = 2)
    private BigDecimal successRatePercent = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "LIVE";
}
