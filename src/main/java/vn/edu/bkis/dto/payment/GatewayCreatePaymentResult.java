package vn.edu.bkis.dto.payment;

import lombok.Data;

/**
 * Ket qua tra ve chung sau khi adapter gateway tao giao dich thanh toan.
 */
@Data
public class GatewayCreatePaymentResult {

	private String partnerCode;

	private String orderId;

	private String requestId;

	private Long amount;

	private Long responseTime;

	private Integer resultCode;

	private String message;

	private String payUrl;

	private String shortLink;
}
