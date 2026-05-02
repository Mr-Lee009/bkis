package vn.edu.bkis.dto.admin.payment;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayPageDto {
    private PaymentGatewaySummaryDto summary;
    private List<PaymentGatewayDto> gateways;
    private List<String> routingRules;
    private List<String> healthEvents;
}
