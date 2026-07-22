package vn.edu.bkis.service.gateway.momo;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import vn.edu.bkis.dto.payment.CreatePaymentRequestDto;
import vn.edu.bkis.dto.payment.GatewayCreatePaymentResult;
import vn.edu.bkis.dto.payment.PaymentProvider;
import vn.edu.bkis.model.PaymentGatewayConfigEntity;
import vn.edu.bkis.service.gateway.PaymentGateway;
import vn.edu.bkis.util.PaymentGatewayUtil;
import vn.edu.bkis.util.RestUtil;
import vn.edu.bkis.util.SecurityUtil;

/**
 * Adapter MoMo tao payment URL va ky request theo contract MoMo.
 */
@Component
public class MomoPaymentGateway implements PaymentGateway {

    /**
     * Tra ve provider MoMo.
     *
     * @return enum provider MoMo
     */
    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MOMO;
    }

    /**
     * Tao request thanh toan MoMo, ky signature va goi API create payment.
     *
     * @param request du lieu thanh toan chung
     * @param config  cau hinh gateway MoMo trong DB
     * @return ket qua tao payment da chuan hoa
     * @throws IllegalArgumentException neu cau hinh thieu truong bat buoc hoac provider tra loi loi
     */
    @Override
    public GatewayCreatePaymentResult createPayment(CreatePaymentRequestDto request,
                                                    PaymentGatewayConfigEntity config) {
        // Step 1: parse config_json va lay cac gia tri can thiet cho request MoMo.
        Map<String, Object> metadata = RestUtil.parseMetadata(config.getConfigJson());
        String partnerCode = PaymentGatewayUtil.firstNonBlank(config.getMerchantCode(),
                PaymentGatewayUtil.stringValue(metadata.get("partnerCode")), "MOMO");
        String partnerName = PaymentGatewayUtil.firstNonBlank(
                PaymentGatewayUtil.stringValue(metadata.get("partnerName")), "Test");
        String storeId = PaymentGatewayUtil.firstNonBlank(
                PaymentGatewayUtil.stringValue(metadata.get("storeId")), "MomoTestStore");
        String accessKey = PaymentGatewayUtil.firstNonBlank(
                PaymentGatewayUtil.stringValue(metadata.get("accessKey")), config.getSecretRef());
        String secretKey = PaymentGatewayUtil.firstNonBlank(
                PaymentGatewayUtil.stringValue(metadata.get("secretKey")), config.getSecretRef());
        String requestType = PaymentGatewayUtil.firstNonBlank(
                PaymentGatewayUtil.stringValue(metadata.get("requestType")), "payWithMethod");
        String lang = PaymentGatewayUtil.firstNonBlank(
                PaymentGatewayUtil.stringValue(metadata.get("lang")), "vi");
        boolean autoCapture = PaymentGatewayUtil.booleanValue(metadata.get("autoCapture"), true);
        String extraData = PaymentGatewayUtil.firstNonBlank(
                PaymentGatewayUtil.stringValue(metadata.get("extraData")), "");
        String orderGroupId = PaymentGatewayUtil.firstNonBlank(
                PaymentGatewayUtil.stringValue(metadata.get("orderGroupId")), "");
        String redirectUrl = PaymentGatewayUtil.firstNonBlank(config.getReturnUrl(),
                PaymentGatewayUtil.stringValue(metadata.get("redirectUrl")));
        String ipnUrl = PaymentGatewayUtil.firstNonBlank(config.getCallbackUrl(),
                PaymentGatewayUtil.stringValue(metadata.get("ipnUrl")));
        String baseUrl = PaymentGatewayUtil.firstNonBlank(config.getEndpointBaseUrl(),
                "https://test-payment.momo.vn/v2/gateway/api/create");
        String createApiPath = PaymentGatewayUtil.blankToNull(config.getCreateApiPath());
        String endpoint = PaymentGatewayUtil.buildEndpoint(baseUrl, createApiPath);

        // Step 2: tao payment code va requestId theo orderId de tranh duplicate request.
        String orderId = PaymentGatewayUtil.firstNonBlank(request.getOrderId(), generateOrderId(partnerCode));
        String requestId = orderId;
        String amount = String.valueOf(request.getAmount().longValue());
        String orderInfo = PaymentGatewayUtil.firstNonBlank(request.getDescription(), "pay with MoMo");

        // Step 3: ky request theo raw signature cua MoMo.
        String rawSignature = buildRawSignature(accessKey, amount, extraData, ipnUrl, orderId, orderInfo,
                partnerCode, redirectUrl, requestId, requestType);
        String signature = calculateSignature(rawSignature, secretKey);

        // Step 4: build payload JSON va goi API provider.
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("partnerName", partnerName);
        payload.put("storeId", storeId);
        payload.put("requestId", requestId);
        payload.put("amount", amount);
        payload.put("orderId", orderId);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipnUrl);
        payload.put("lang", lang);
        payload.put("requestType", requestType);
        payload.put("autoCapture", autoCapture);
        payload.put("extraData", extraData);
        payload.put("orderGroupId", orderGroupId);
        payload.put("signature", signature);

        Map<String, Object> responseBody = RestUtil.postJson(endpoint, payload);

        // Step 5: parse response provider va chuan hoa ve DTO dung chung.
        GatewayCreatePaymentResult result = new GatewayCreatePaymentResult();
        result.setPartnerCode(PaymentGatewayUtil.stringValue(responseBody.get("partnerCode")));
        result.setOrderId(PaymentGatewayUtil.stringValue(responseBody.get("orderId")));
        result.setRequestId(PaymentGatewayUtil.stringValue(responseBody.get("requestId")));
        result.setAmount(PaymentGatewayUtil.longValue(responseBody.get("amount"), request.getAmount().longValue()));
        result.setResponseTime(PaymentGatewayUtil.longValue(responseBody.get("responseTime"),
                Instant.now().toEpochMilli()));
        result.setResultCode(PaymentGatewayUtil.intValue(responseBody.get("resultCode"), -1));
        result.setMessage(PaymentGatewayUtil.stringValue(responseBody.get("message")));
        result.setPayUrl(PaymentGatewayUtil.stringValue(responseBody.get("payUrl")));
        result.setShortLink(PaymentGatewayUtil.stringValue(responseBody.get("shortLink")));

        if (result.getResultCode() != 0 && (result.getMessage() == null || result.getMessage().isBlank())) {
            throw new IllegalStateException("MoMo create payment failed without message.");
        }
        return result;
    }

    /**
     * Tao chuoi raw signature theo dung thu tu field MoMo yeu cau.
     *
     * @param accessKey   access key MoMo
     * @param amount      so tien
     * @param extraData   du lieu mo rong
     * @param ipnUrl      callback url server to server
     * @param orderId     ma don hang
     * @param orderInfo   noi dung don hang
     * @param partnerCode ma doi tac
     * @param redirectUrl url redirect sau thanh toan
     * @param requestId   request id
     * @param requestType kieu request
     * @return chuoi raw signature
     */
    private String buildRawSignature(String accessKey, String amount, String extraData,
                                     String ipnUrl, String orderId, String orderInfo, String partnerCode, String redirectUrl,
                                     String requestId, String requestType) {
        // Step 1: noi chuoi theo dung thu tu MoMo yeu cau.
        return "accessKey=" + accessKey + "&amount=" + amount + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl + "&orderId=" + orderId + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode + "&redirectUrl=" + redirectUrl + "&requestId=" + requestId
                + "&requestType=" + requestType;
    }

    /**
     * Ky raw signature bang HMAC SHA256.
     *
     * @param rawSignature chuoi can ky
     * @param secretKey    secret key cua MoMo
     * @return chu ky hexa
     */
    private String calculateSignature(String rawSignature, String secretKey) {
        // Step 1: su dung helper co san trong repo de ky HMAC.
        try {
            return SecurityUtil.calculateHmac(rawSignature, secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Khong the ky request MoMo.", ex);
        }
    }

    /**
     * Sinh orderId MoMo khi request chua co orderId hop le.
     *
     * @param partnerCode ma doi tac
     * @return orderId duy nhat
     */
    private String generateOrderId(String partnerCode) {
        // Step 1: dung partnerCode + timestamp de tranh trung orderId.
        return partnerCode + System.currentTimeMillis();
    }
}
