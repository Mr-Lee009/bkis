package vn.edu.bkis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_gateway_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "provider", nullable = false, unique = true, length = 20)
    private String provider;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "environment", nullable = false, length = 20)
    private String environment;

    @Column(name = "merchant_code", length = 100)
    private String merchantCode;

    @Column(name = "endpoint_base_url", nullable = false, length = 255)
    private String endpointBaseUrl;

    @Column(name = "create_api_path", length = 255)
    private String createApiPath;

    @Column(name = "query_api_path", length = 255)
    private String queryApiPath;

    @Column(name = "return_url", nullable = false, length = 255)
    private String returnUrl;

    @Column(name = "callback_url", nullable = false, length = 255)
    private String callbackUrl;

    @Column(name = "secret_ref", nullable = false, length = 255)
    private String secretRef;

    @Column(name = "timeout_seconds", nullable = false)
    private Integer timeoutSeconds = 15;

    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    @Column(name = "config_json", columnDefinition = "json")
    private String configJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
