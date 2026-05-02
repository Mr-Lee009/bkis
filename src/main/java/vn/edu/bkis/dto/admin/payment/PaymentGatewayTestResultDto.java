package vn.edu.bkis.dto.admin.payment;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayTestResultDto {
    private String code;
    private boolean healthy;
    private String message;
    private LocalDateTime checkedAt;
}
