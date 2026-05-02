package vn.edu.bkis.service.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayPageDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewaySummaryDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayTestResultDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayUpsertRequest;
import vn.edu.bkis.model.PaymentGateway;
import vn.edu.bkis.repository.PaymentGatewayRepository;

@Service
public class AdminPaymentGatewayService {
    private final PaymentGatewayRepository paymentGatewayRepository;

    // Khoi tao service quan ly cau hinh cong thanh toan.
    public AdminPaymentGatewayService(PaymentGatewayRepository paymentGatewayRepository) {
        this.paymentGatewayRepository = paymentGatewayRepository;
    }

    // Lay toan bo du lieu cho page cau hinh thanh toan qua API.
    @Transactional
    public PaymentGatewayPageDto getPage() {
        seedDefaultGatewaysIfMissing();
        List<PaymentGateway> gateways = paymentGatewayRepository.findAllByOrderByRoutingPriorityAscIdAsc();
        return new PaymentGatewayPageDto(
                buildSummary(gateways),
                gateways.stream().map(this::toDto).toList(),
                buildRoutingRules(),
                buildHealthEvents()
        );
    }

    // Lay chi tiet mot gateway theo code.
    @Transactional(readOnly = true)
    public PaymentGatewayDto getGateway(String code) {
        return paymentGatewayRepository.findByCode(normalizeCode(code))
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Payment gateway not found: " + code));
    }

    // Tao moi gateway tu modal them gateway.
    @Transactional
    public PaymentGatewayDto createGateway(PaymentGatewayUpsertRequest request) {
        String code = normalizeCode(required(request.getCode(), "Gateway code is required"));
        if (paymentGatewayRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Gateway code already exists: " + code);
        }
        PaymentGateway gateway = new PaymentGateway();
        gateway.setCode(code);
        applyRequest(gateway, request, true);
        gateway.setCreatedBy("admin-payment-gateway");
        gateway.setUpdatedBy("admin-payment-gateway");
        return toDto(paymentGatewayRepository.save(gateway));
    }

    // Cap nhat gateway hien co va giu secret cu neu form khong nhap secret moi.
    @Transactional
    public PaymentGatewayDto updateGateway(String code, PaymentGatewayUpsertRequest request) {
        PaymentGateway gateway = paymentGatewayRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new IllegalArgumentException("Payment gateway not found: " + code));
        applyRequest(gateway, request, false);
        gateway.setUpdatedBy("admin-payment-gateway");
        return toDto(paymentGatewayRepository.save(gateway));
    }

    // Test ket noi mock cho mot gateway dua tren cau hinh bat/tat va endpoint.
    @Transactional(readOnly = true)
    public PaymentGatewayTestResultDto testGateway(String code) {
        PaymentGateway gateway = paymentGatewayRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new IllegalArgumentException("Payment gateway not found: " + code));
        boolean healthy = Boolean.TRUE.equals(gateway.getEnabled())
                && !isBlank(gateway.getPaymentEndpoint())
                && !isBlank(gateway.getMerchantId());
        String message = healthy
                ? "Gateway " + gateway.getDisplayName() + " connection test passed."
                : "Gateway " + gateway.getDisplayName() + " is missing required connection settings.";
        return new PaymentGatewayTestResultDto(gateway.getCode(), healthy, message, LocalDateTime.now());
    }

    // Test tat ca gateway dang co trong he thong.
    @Transactional(readOnly = true)
    public List<PaymentGatewayTestResultDto> testAllGateways() {
        return paymentGatewayRepository.findAllByOrderByRoutingPriorityAscIdAsc()
                .stream()
                .map(gateway -> testGateway(gateway.getCode()))
                .toList();
    }

    // Gan du lieu request vao entity va chuan hoa gia tri rong.
    private void applyRequest(PaymentGateway gateway, PaymentGatewayUpsertRequest request, boolean creating) {
        gateway.setDisplayName(required(request.getDisplayName(), "Gateway display name is required"));
        gateway.setProviderType(required(request.getProviderType(), "Gateway provider type is required"));
        gateway.setDescription(blankToNull(request.getDescription()));
        gateway.setMerchantId(blankToNull(request.getMerchantId()));
        gateway.setPartnerCode(blankToNull(request.getPartnerCode()));
        if (creating || !isBlank(request.getSecretKey())) {
            gateway.setSecretKey(blankToNull(request.getSecretKey()));
        }
        gateway.setPaymentEndpoint(blankToNull(request.getPaymentEndpoint()));
        gateway.setReturnUrl(blankToNull(request.getReturnUrl()));
        gateway.setWebhookUrl(blankToNull(request.getWebhookUrl()));
        gateway.setIpAllowlist(blankToNull(request.getIpAllowlist()));
        gateway.setEnabled(request.getEnabled() == null || request.getEnabled());
        gateway.setSandboxMode(Boolean.TRUE.equals(request.getSandboxMode()));
        gateway.setRoutingPriority(request.getRoutingPriority() == null ? 99 : request.getRoutingPriority());
        gateway.setTransactionFeePercent(request.getTransactionFeePercent() == null
                ? BigDecimal.ZERO
                : request.getTransactionFeePercent());
        gateway.setSuccessRatePercent(request.getSuccessRatePercent() == null
                ? BigDecimal.ZERO
                : request.getSuccessRatePercent());
        gateway.setStatus(isBlank(request.getStatus()) ? "LIVE" : request.getStatus().trim().toUpperCase(Locale.ROOT));
    }

    // Tao summary card tu danh sach gateway hien tai.
    private PaymentGatewaySummaryDto buildSummary(List<PaymentGateway> gateways) {
        BigDecimal totalRate = gateways.stream()
                .map(PaymentGateway::getSuccessRatePercent)
                .filter(rate -> rate != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageRate = gateways.isEmpty()
                ? BigDecimal.ZERO
                : totalRate.divide(BigDecimal.valueOf(gateways.size()), 2, RoundingMode.HALF_UP);
        boolean hasSandbox = gateways.stream().anyMatch(gateway -> Boolean.TRUE.equals(gateway.getSandboxMode()));
        return new PaymentGatewaySummaryDto(
                gateways.stream().filter(gateway -> Boolean.TRUE.equals(gateway.getEnabled())).count(),
                gateways.size(),
                averageRate,
                0,
                hasSandbox ? "Mixed" : "Live"
        );
    }

    // Chuyen entity gateway sang DTO an toan, khong tra secret that ve UI.
    private PaymentGatewayDto toDto(PaymentGateway gateway) {
        return new PaymentGatewayDto(
                gateway.getId(),
                gateway.getCode(),
                gateway.getDisplayName(),
                gateway.getProviderType(),
                gateway.getDescription(),
                gateway.getMerchantId(),
                gateway.getPartnerCode(),
                maskSecret(gateway.getSecretKey()),
                gateway.getPaymentEndpoint(),
                gateway.getReturnUrl(),
                gateway.getWebhookUrl(),
                gateway.getIpAllowlist(),
                gateway.getEnabled(),
                gateway.getSandboxMode(),
                gateway.getRoutingPriority(),
                gateway.getTransactionFeePercent(),
                gateway.getSuccessRatePercent(),
                gateway.getStatus(),
                statusBadgeClass(gateway.getStatus())
        );
    }

    // Seed gateway mau theo mock khi DB chua co cau hinh nao.
    private void seedDefaultGatewaysIfMissing() {
        if (paymentGatewayRepository.count() > 0) {
            return;
        }
        paymentGatewayRepository.saveAll(List.of(
                defaultGateway("vnpay", "VNPay", "VNPAY", "ATM, QR, Napas", "BKIS_VNPAY_2026", "BKIS_TERM_01", "https://pay.vnpay.vn/vpcpay.html", "https://elearning.vn/payments/return/vnpay", "https://elearning.vn/api/webhooks/vnpay", 1, "1.10", "99.20", "LIVE"),
                defaultGateway("momo", "MoMo", "MOMO", "Wallet, QR, app switch", "BKIS_MOMO_2026", "BKIS_MOMO_PARTNER", "https://test-payment.momo.vn/v2/gateway/api/create", "https://elearning.vn/payments/return/momo", "https://elearning.vn/api/webhooks/momo", 2, "1.30", "98.40", "LIVE"),
                defaultGateway("stripe", "Stripe", "STRIPE", "Visa, Mastercard, Apple Pay", "acct_bkis_demo", "stripe_live", "https://api.stripe.com/v1/checkout/sessions", "https://elearning.vn/payments/return/stripe", "https://elearning.vn/api/webhooks/stripe", 3, "2.90", "96.80", "REVIEW"),
                defaultGateway("bank", "Chuyen khoan", "BANK_TRANSFER", "Manual verify, QR bank", "BKIS_BANK", "VCB_BKIS", "", "https://elearning.vn/payments/return/bank", "https://elearning.vn/api/webhooks/bank", 99, "0.00", "92.10", "MANUAL")
        ));
    }

    // Tao entity gateway mac dinh theo mock admin payment gateway.
    private PaymentGateway defaultGateway(String code, String displayName, String providerType, String description,
                                          String merchantId, String partnerCode, String endpoint, String returnUrl,
                                          String webhookUrl, int priority, String fee, String successRate, String status) {
        PaymentGateway gateway = new PaymentGateway();
        gateway.setCode(code);
        gateway.setDisplayName(displayName);
        gateway.setProviderType(providerType);
        gateway.setDescription(description);
        gateway.setMerchantId(merchantId);
        gateway.setPartnerCode(partnerCode);
        gateway.setSecretKey("dev-secret-" + code);
        gateway.setPaymentEndpoint(endpoint);
        gateway.setReturnUrl(returnUrl);
        gateway.setWebhookUrl(webhookUrl);
        gateway.setIpAllowlist("203.162.4.190\n203.162.4.191");
        gateway.setEnabled(true);
        gateway.setSandboxMode(false);
        gateway.setRoutingPriority(priority);
        gateway.setTransactionFeePercent(new BigDecimal(fee));
        gateway.setSuccessRatePercent(new BigDecimal(successRate));
        gateway.setStatus(status);
        gateway.setCreatedBy("seed");
        gateway.setUpdatedBy("seed");
        return gateway;
    }

    // Tra class badge theo trang thai gateway.
    private String statusBadgeClass(String status) {
        if ("LIVE".equalsIgnoreCase(status)) {
            return "badge bg-success";
        }
        if ("REVIEW".equalsIgnoreCase(status)) {
            return "badge bg-warning text-dark";
        }
        if ("MANUAL".equalsIgnoreCase(status)) {
            return "badge bg-info text-dark";
        }
        return "badge bg-secondary";
    }

    // Tao rules mock de UI hien thi bang routing bang API.
    private List<String> buildRoutingRules() {
        return List.of(
                "VND public courses: VNPay -> MoMo -> Bank transfer",
                "USD international students: Stripe -> VNPay",
                "Enterprise invoice: Bank transfer only"
        );
    }

    // Tao health events mock de UI hien thi timeline bang API.
    private List<String> buildHealthEvents() {
        return List.of(
                "VNPay test passed recently",
                "MoMo webhook retry passed",
                "Stripe tax setting needs review",
                "Bank QR settlement file synced"
        );
    }

    // An secret truoc khi tra ve frontend.
    private String maskSecret(String secretKey) {
        if (isBlank(secretKey)) {
            return "";
        }
        return "********";
    }

    // Chuan hoa code gateway ve lower-case khong khoang trang.
    private String normalizeCode(String code) {
        return required(code, "Gateway code is required").toLowerCase(Locale.ROOT);
    }

    // Bat buoc chuoi khong duoc rong.
    private String required(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    // Chuyen chuoi rong thanh null truoc khi luu DB.
    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    // Kiem tra chuoi rong hoac null.
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
