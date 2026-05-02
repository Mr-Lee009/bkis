package vn.edu.bkis.dto.admin.payment;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewaySummaryDto {
    private long enabledGateways;
    private long totalGateways;
    private BigDecimal averageSuccessRatePercent;
    private long webhookErrors;
    private String environmentLabel;
}
