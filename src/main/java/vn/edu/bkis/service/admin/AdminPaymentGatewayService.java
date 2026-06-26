package vn.edu.bkis.service.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayPageDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewaySummaryDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayTestResultDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayUpsertRequest;
import vn.edu.bkis.model.PaymentGatewayConfigEntity;
import vn.edu.bkis.model.PaymentTransactionEntity;
import vn.edu.bkis.repository.PaymentGatewayConfigRepository;
import vn.edu.bkis.repository.PaymentTransactionRepository;
import vn.edu.bkis.util.DateUtil;
import vn.edu.bkis.util.VietnameseNameUtil;

@Service
public class AdminPaymentGatewayService {
    private static final tools.jackson.core.type.TypeReference<Map<String, Object>>
        CONFIG_MAP_TYPE = new tools.jackson.core.type.TypeReference<>() {
    };
    private static final List<String> SUCCESS_STATUSES = List.of("SUCCESS", "COMPLETED", "PAID");
    private static final List<String> ERROR_STATUSES =
        List.of("FAILED", "ERROR", "CANCELLED", "EXPIRED");
    private static final DateTimeFormatter EVENT_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final PaymentGatewayConfigRepository paymentGatewayConfigRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final tools.jackson.databind.ObjectMapper objectMapper;

    /**
     * Khoi tao service quan ly cau hinh cong thanh toan theo bang payment_gateway_config moi.
     *
     * @param paymentGatewayConfigRepository repository thao tac cau hinh gateway moi
     * @param paymentTransactionRepository repository thao tac lich su giao dich thanh toan moi
     * @param objectMapper bo chuyen doi json cho truong config_json
     * @return khong tra du lieu; constructor dung de gan dependency cho service
     */
    public AdminPaymentGatewayService(PaymentGatewayConfigRepository paymentGatewayConfigRepository,
        PaymentTransactionRepository paymentTransactionRepository,
        tools.jackson.databind.ObjectMapper objectMapper) {
        this.paymentGatewayConfigRepository = paymentGatewayConfigRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Lay toan bo du lieu cho page cau hinh thanh toan tu bang gateway moi va giao dich moi.
     *
     * @return {@link PaymentGatewayPageDto} du lieu tong hop cho man hinh admin payment gateways
     */
    @Transactional
    public PaymentGatewayPageDto getPage() {
        // Step 1: seed du lieu cau hinh mac dinh neu DB chua co gateway moi nao.
        seedDefaultGatewaysIfMissing();

        // Step 2: tai danh sach gateway va giao dich lien quan de dung lai cho summary, list va health panel.
        List<PaymentGatewayConfigEntity> gateways =
            paymentGatewayConfigRepository.findAllByOrderByPriorityAscIdAsc();
        List<PaymentTransactionEntity> transactions = loadTransactionsForGateways(gateways);

        // Step 3: map du lieu ve DTO cho frontend admin.
        return new PaymentGatewayPageDto(buildSummary(gateways, transactions),
            gateways.stream().map(gateway -> toDto(gateway, transactions)).toList(),
            buildRoutingRules(gateways), buildHealthEvents());
    }

    /**
     * Lay chi tiet mot gateway theo provider code trong bang moi.
     *
     * @param code ma provider dang duoc UI chon
     * @return {@link PaymentGatewayDto} thong tin chi tiet gateway
     */
    @Transactional(readOnly = true)
    public PaymentGatewayDto getGateway(String code) {
        // Step 1: chuan hoa provider code tu URL request.
        String provider = normalizeProvider(code);

        // Step 2: tai cau hinh gateway va transaction lien quan de tinh thong so hien thi.
        PaymentGatewayConfigEntity gateway = paymentGatewayConfigRepository.findByProvider(provider)
            .orElseThrow(
                () -> new IllegalArgumentException("Payment gateway config not found: " + code));
        List<PaymentTransactionEntity> transactions = loadTransactionsForGateways(List.of(gateway));

        // Step 3: chuyen entity cau hinh moi sang DTO ma UI hien tai dang dung.
        return toDto(gateway, transactions);
    }

    /**
     * Tao moi cau hinh gateway tren bang payment_gateway_config.
     *
     * @param request du lieu gateway moi gui tu popup admin
     * @return {@link PaymentGatewayDto} gateway vua tao sau khi map lai tu DB
     */
    @Transactional
    public PaymentGatewayDto createGateway(PaymentGatewayUpsertRequest request) {
        // Step 1: chuan hoa provider va chan som truong hop trung cau hinh.
        String provider =
            normalizeProvider(required(request.getCode(), "Gateway code is required"));
        if (paymentGatewayConfigRepository.existsByProvider(provider)) {
            throw new IllegalArgumentException("Gateway code already exists: " + provider);
        }

        // Step 2: tao entity moi tu request hien tai.
        PaymentGatewayConfigEntity gateway = new PaymentGatewayConfigEntity();
        gateway.setProvider(provider);
        applyRequest(gateway, request, true);

        // Step 3: luu cau hinh moi va tra DTO cho frontend.
        PaymentGatewayConfigEntity savedGateway = paymentGatewayConfigRepository.save(gateway);
        return toDto(savedGateway, List.of());
    }

    /**
     * Cap nhat cau hinh gateway dang ton tai tren bang moi.
     *
     * @param code provider code dang duoc chon tren UI
     * @param request du lieu cap nhat gui len tu form chi tiet
     * @return {@link PaymentGatewayDto} gateway sau khi cap nhat
     */
    @Transactional
    public PaymentGatewayDto updateGateway(String code, PaymentGatewayUpsertRequest request) {
        // Step 1: tai cau hinh gateway dang co theo provider.
        PaymentGatewayConfigEntity gateway =
            paymentGatewayConfigRepository.findByProvider(normalizeProvider(code)).orElseThrow(
                () -> new IllegalArgumentException("Payment gateway config not found: " + code));

        // Step 2: dong bo request vao entity bang moi.
        applyRequest(gateway, request, false);

        // Step 3: luu cau hinh va tra DTO cap nhat.
        PaymentGatewayConfigEntity savedGateway = paymentGatewayConfigRepository.save(gateway);
        List<PaymentTransactionEntity> transactions =
            loadTransactionsForGateways(List.of(savedGateway));
        return toDto(savedGateway, transactions);
    }

    /**
     * Test mock ket noi mot gateway dua tren cau hinh moi.
     *
     * @param code provider code cua gateway can test
     * @return {@link PaymentGatewayTestResultDto} ket qua test mock hien thi tren admin page
     */
    @Transactional(readOnly = true)
    public PaymentGatewayTestResultDto testGateway(String code) {
        // Step 1: tai cau hinh gateway can test.
        PaymentGatewayConfigEntity gateway =
            paymentGatewayConfigRepository.findByProvider(normalizeProvider(code)).orElseThrow(
                () -> new IllegalArgumentException("Payment gateway config not found: " + code));

        // Step 2: kiem tra cac truong can co de tao ket qua test mock.
        boolean healthy = Boolean.TRUE.equals(gateway.getEnabled()) && !isBlank(
            gateway.getEndpointBaseUrl()) && !isBlank(gateway.getMerchantCode()) && !isBlank(
            gateway.getSecretRef());

        // Step 3: tra ket qua test ve frontend.
        String message = healthy ?
            "Gateway " + resolveDisplayName(gateway) + " connection test passed." :
            "Gateway " + resolveDisplayName(gateway) + " is missing required connection settings.";
        return new PaymentGatewayTestResultDto(gateway.getProvider(), healthy, message,
            LocalDateTime.now());
    }

    /**
     * Test mock ket noi tat ca gateway dang co trong bang moi.
     *
     * @return {@link List} danh sach ket qua test cho tung gateway
     */
    @Transactional(readOnly = true)
    public List<PaymentGatewayTestResultDto> testAllGateways() {
        // Step 1: tai danh sach gateway theo thu tu uu tien hien tai.
        List<PaymentGatewayConfigEntity> gateways =
            paymentGatewayConfigRepository.findAllByOrderByPriorityAscIdAsc();

        // Step 2: goi lai logic test cho tung gateway de frontend hien thi tong hop.
        return gateways.stream().map(gateway -> testGateway(gateway.getProvider())).toList();
    }

    /**
     * Dong bo du lieu tu request cu vao entity gateway moi va luu metadata vao config_json.
     *
     * @param gateway entity bang payment_gateway_config can cap nhat
     * @param request du lieu gui len tu UI admin
     * @param creating true neu dang tao moi; false neu dang cap nhat
     * @return khong tra du lieu; method cap nhat truc tiep tren entity dau vao
     */
    private void applyRequest(PaymentGatewayConfigEntity gateway,
        PaymentGatewayUpsertRequest request, boolean creating) {
        // Step 1: tai metadata hien tai tu config_json de giu lai cac gia tri khong sua.
        Map<String, Object> metadata = parseConfig(gateway.getConfigJson());

        // Step 2: map cac truong cot chinh cua bang moi tu request cu.
        gateway.setEnabled(request.getEnabled() == null || request.getEnabled());
        gateway.setEnvironment(resolveEnvironment(request.getSandboxMode(), request.getStatus()));
        gateway.setMerchantCode(blankToNull(request.getMerchantId()));
        gateway.setEndpointBaseUrl(
            resolveConfigValue(request.getPaymentEndpoint(), gateway.getEndpointBaseUrl(),
                defaultPaymentEndpoint(gateway.getProvider()), "Payment endpoint is required"));
        gateway.setCreateApiPath(blankToNull(stringValue(metadata.get("createApiPath"))));
        gateway.setQueryApiPath(blankToNull(stringValue(metadata.get("queryApiPath"))));
        gateway.setReturnUrl(resolveConfigValue(request.getReturnUrl(), gateway.getReturnUrl(),
            defaultReturnUrl(gateway.getProvider()), "Return URL is required"));
        gateway.setCallbackUrl(resolveConfigValue(request.getWebhookUrl(), gateway.getCallbackUrl(),
            defaultWebhookUrl(gateway.getProvider()), "Webhook URL is required"));
        if (creating || !isBlank(request.getSecretKey())) {
            gateway.setSecretRef(resolveConfigValue(request.getSecretKey(), gateway.getSecretRef(),
                "dev-secret-" + gateway.getProvider(), "Secret key is required"));
        } else if (isBlank(gateway.getSecretRef())) {
            gateway.setSecretRef(resolveConfigValue(request.getSecretKey(), gateway.getSecretRef(),
                "dev-secret-" + gateway.getProvider(), "Secret key is required"));
        }
        gateway.setTimeoutSeconds(resolveInteger(metadata.get("timeoutSeconds"), 15));
        gateway.setPriority(
            request.getRoutingPriority() == null ? 100 : request.getRoutingPriority());
        gateway.setCreatedAt(
            gateway.getCreatedAt() == null ? LocalDateTime.now() : gateway.getCreatedAt());
        gateway.setUpdatedAt(LocalDateTime.now());

        // Step 3: luu cac truong UI cu vao metadata de frontend hien tai khong can doi contract.
        metadata.put("displayName",
            required(request.getDisplayName(), "Gateway display name is required"));
        metadata.put("providerType",
            required(request.getProviderType(), "Gateway provider type is required"));
        metadata.put("description", blankToNull(request.getDescription()));
        metadata.put("partnerCode", blankToNull(request.getPartnerCode()));
        metadata.put("ipAllowlist", blankToNull(request.getIpAllowlist()));
        metadata.put("transactionFeePercent",
            safeDecimal(request.getTransactionFeePercent()).toPlainString());
        metadata.put("status",
            normalizeStatus(request.getStatus(), request.getEnabled(), request.getSandboxMode()));
        gateway.setConfigJson(writeConfig(metadata));
    }

    /**
     * Tao summary cho admin page tu gateway moi va payment_transaction.
     *
     * @param gateways danh sach gateway hien tai trong bang moi
     * @param transactions danh sach transaction cua cac gateway hien tai
     * @return {@link PaymentGatewaySummaryDto} du lieu card summary tren man hinh
     */
    private PaymentGatewaySummaryDto buildSummary(List<PaymentGatewayConfigEntity> gateways,
        List<PaymentTransactionEntity> transactions) {
        // Step 1: tinh tong so gateway dang bat va thong so thanh cong trung binh theo provider.
        BigDecimal averageRate = gateways.isEmpty() ?
            BigDecimal.ZERO :
            gateways.stream().map(
                    gateway -> calculateSuccessRateForProvider(gateway.getProvider(), transactions))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(gateways.size()), 2, RoundingMode.HALF_UP);

        // Step 2: tinh so transaction loi de hien thi canh bao webhook.
        long webhookErrors = transactions.stream()
            .filter(transaction -> ERROR_STATUSES.contains(normalizeUpper(transaction.getStatus())))
            .count();

        // Step 3: xac dinh nhan moi truong tong hop.
        return new PaymentGatewaySummaryDto(
            gateways.stream().filter(gateway -> Boolean.TRUE.equals(gateway.getEnabled())).count(),
            gateways.size(), averageRate, webhookErrors, resolveEnvironmentLabel(gateways));
    }

    /**
     * Chuyen entity gateway moi sang DTO ma frontend admin payment gateways dang su dung.
     *
     * @param gateway entity gateway moi trong bang payment_gateway_config
     * @param transactions danh sach transaction lien quan de tinh ty le thanh cong
     * @return {@link PaymentGatewayDto} DTO tra ve cho frontend
     */
    private PaymentGatewayDto toDto(PaymentGatewayConfigEntity gateway,
        List<PaymentTransactionEntity> transactions) {
        // Step 1: parse metadata tu config_json de map lai cac truong UI cu.
        Map<String, Object> metadata = parseConfig(gateway.getConfigJson());
        String status = stringValue(metadata.get("status"));

        // Step 2: map entity sang DTO va bo sung cac truong tinh toan tu payment_transaction.
        return new PaymentGatewayDto(gateway.getId(), gateway.getProvider(),
            coalesce(stringValue(metadata.get("displayName")), resolveDisplayName(gateway)),
            coalesce(stringValue(metadata.get("providerType")), gateway.getProvider()),
            stringValue(metadata.get("description")), gateway.getMerchantCode(),
            stringValue(metadata.get("partnerCode")), maskSecret(gateway.getSecretRef()),
            buildPaymentEndpoint(gateway), gateway.getReturnUrl(), gateway.getCallbackUrl(),
            stringValue(metadata.get("ipAllowlist")), gateway.getEnabled(),
            isSandboxEnvironment(gateway.getEnvironment()), gateway.getPriority(),
            safeDecimal(stringValue(metadata.get("transactionFeePercent"))),
            calculateSuccessRateForProvider(gateway.getProvider(), transactions), status == null ?
            normalizeStatus(null, gateway.getEnabled(),
                isSandboxEnvironment(gateway.getEnvironment())) :
            status, statusBadgeClass(status == null ?
            normalizeStatus(null, gateway.getEnabled(),
                isSandboxEnvironment(gateway.getEnvironment())) :
            status));
    }

    /**
     * Seed cau hinh gateway mac dinh vao bang moi neu he thong chua co du lieu.
     *
     * @return khong tra du lieu; method chi luu du lieu mau khi bang moi dang rong
     */
    private void seedDefaultGatewaysIfMissing() {
        // Step 1: bo qua seed neu bang moi da co du lieu.
        if (paymentGatewayConfigRepository.count() > 0) {
            return;
        }

        // Step 2: tao danh sach gateway mac dinh theo bo cau hinh admin hien tai.
        List<PaymentGatewayConfigEntity> defaultGateways = List.of(
            defaultGateway("vnpay", "VNPay", "VNPAY", "ATM, QR, Napas", "BKIS_VNPAY_2026",
                "BKIS_TERM_01", "https://pay.vnpay.vn/vpcpay.html",
                "https://elearning.vn/payments/return/vnpay",
                "https://elearning.vn/api/webhooks/vnpay", 1, "1.10", "LIVE"),
            defaultGateway("momo", "MoMo", "MOMO", "Wallet, QR, app switch", "BKIS_MOMO_2026",
                "BKIS_MOMO_PARTNER", "https://test-payment.momo.vn/v2/gateway/api/create",
                "https://elearning.vn/payments/return/momo",
                "https://elearning.vn/api/webhooks/momo", 2, "1.30", "LIVE"),
            defaultGateway("stripe", "Stripe", "STRIPE", "Visa, Mastercard, Apple Pay",
                "acct_bkis_demo", "stripe_live", "https://api.stripe.com/v1/checkout/sessions",
                "https://elearning.vn/payments/return/stripe",
                "https://elearning.vn/api/webhooks/stripe", 3, "2.90", "REVIEW"),
            defaultGateway("bank", "Chuyen khoan", "BANK_TRANSFER", "Manual verify, QR bank",
                "BKIS_BANK", "VCB_BKIS", "https://bank.example.vn/transfer",
                "https://elearning.vn/payments/return/bank",
                "https://elearning.vn/api/webhooks/bank", 99, "0.00", "MANUAL"));

        // Step 3: luu bo cau hinh mau vao bang moi.
        paymentGatewayConfigRepository.saveAll(defaultGateways);
    }

    /**
     * Tao entity gateway mac dinh theo schema payment_gateway_config moi.
     *
     * @param provider ma provider luu trong bang moi
     * @param displayName ten hien thi gateway cho UI
     * @param providerType loai provider ma UI cu dang su dung
     * @param description mo ta hien thi tren danh sach gateway
     * @param merchantCode ma merchant cua provider
     * @param partnerCode ma terminal hoac partner code de luu trong metadata
     * @param endpoint endpoint thanh toan chinh
     * @param returnUrl dia chi return url
     * @param callbackUrl dia chi callback url
     * @param priority thu tu uu tien route
     * @param transactionFeePercent phi giao dich mock hien thi tren UI
     * @param status trang thai hien thi tren UI
     * @return {@link PaymentGatewayConfigEntity} entity mac dinh de seed DB
     */
    private PaymentGatewayConfigEntity defaultGateway(String provider, String displayName,
        String providerType, String description, String merchantCode, String partnerCode,
        String endpoint, String returnUrl, String callbackUrl, int priority,
        String transactionFeePercent, String status) {
        // Step 1: tao metadata giu cac truong UI cu trong config_json.
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("displayName", displayName);
        metadata.put("providerType", providerType);
        metadata.put("description", description);
        metadata.put("partnerCode", partnerCode);
        metadata.put("ipAllowlist", "203.162.4.190\n203.162.4.191");
        metadata.put("transactionFeePercent", transactionFeePercent);
        metadata.put("status", status);

        // Step 2: map du lieu mac dinh sang schema moi.
        PaymentGatewayConfigEntity gateway = new PaymentGatewayConfigEntity();
        gateway.setProvider(provider);
        gateway.setEnabled(true);
        gateway.setEnvironment("LIVE");
        gateway.setMerchantCode(merchantCode);
        gateway.setEndpointBaseUrl(endpoint);
        gateway.setCreateApiPath(null);
        gateway.setQueryApiPath(null);
        gateway.setReturnUrl(returnUrl);
        gateway.setCallbackUrl(callbackUrl);
        gateway.setSecretRef("dev-secret-" + provider);
        gateway.setTimeoutSeconds(15);
        gateway.setPriority(priority);
        gateway.setConfigJson(writeConfig(metadata));
        gateway.setCreatedAt(LocalDateTime.now());
        gateway.setUpdatedAt(LocalDateTime.now());
        return gateway;
    }

    /**
     * Tao danh sach routing rule mock dua tren gateway moi de frontend hien thi.
     *
     * @param gateways danh sach gateway moi theo thu tu uu tien
     * @return {@link List} routing rule dang hien thi tren panel admin
     */
    private List<String> buildRoutingRules(List<PaymentGatewayConfigEntity> gateways) {
        // Step 1: tai danh sach provider dang bat theo thu tu route.
        List<String> enabledProviders =
            gateways.stream().filter(gateway -> Boolean.TRUE.equals(gateway.getEnabled()))
                .map(this::resolveDisplayName).toList();

        // Step 2: tra danh sach rule mock co dinh neu he thong chua co rule phuc tap.
        if (enabledProviders.isEmpty()) {
            return List.of("Chua co gateway dang bat de dinh tuyen thanh toan.");
        }
        return List.of("Mac dinh VND: " + String.join(" -> ", enabledProviders),
            "Gateway co priority nho hon duoc uu tien route truoc.",
            "Gateway tat se bi bo qua trong luong thanh toan moi.");
    }

    /**
     * Tao timeline health event dua tren payment_transaction moi.
     *
     * @return {@link List} danh sach event hien thi ben panel health check
     */
    private List<String> buildHealthEvents() {
        // Step 1: tai toi da 10 transaction moi nhat de dung lam event mock.
        List<PaymentTransactionEntity> transactions =
            paymentTransactionRepository.findTop10ByOrderByUpdatedAtDesc();
        if (transactions.isEmpty()) {
            return List.of("Chua co giao dich moi de tao health event.");
        }

        // Step 2: map transaction thanh dong event ngan gon cho UI.
        List<String> events = new ArrayList<>();
        for (PaymentTransactionEntity transaction : transactions) {
            String eventTime = transaction.getUpdatedAt() == null ?
                "--/-- --:--" :
                transaction.getUpdatedAt().format(EVENT_TIME_FORMATTER);
            String status = normalizeUpper(transaction.getStatus());
            if (SUCCESS_STATUSES.contains(status)) {
                events.add(resolveProviderLabel(
                    transaction.getProvider()) + " thanh cong luc " + eventTime);
                continue;
            }
            if (ERROR_STATUSES.contains(status)) {
                events.add(resolveProviderLabel(
                    transaction.getProvider()) + " loi " + status + " luc " + eventTime);
                continue;
            }
            events.add(resolveProviderLabel(
                transaction.getProvider()) + " dang o trang thai " + status + " luc " + eventTime);
        }
        return events;
    }

    /**
     * Tinh ty le thanh cong cua mot provider dua tren payment_transaction moi.
     *
     * @param provider ma provider can tinh ty le thanh cong
     * @param transactions danh sach transaction cua cac provider dang hien thi
     * @return {@link BigDecimal} ty le thanh cong theo phan tram
     */
    private BigDecimal calculateSuccessRateForProvider(String provider,
        List<PaymentTransactionEntity> transactions) {
        // Step 1: loc transaction cua provider hien tai.
        List<PaymentTransactionEntity> providerTransactions = transactions.stream().filter(
            transaction -> normalizeProvider(transaction.getProvider()).equals(
                normalizeProvider(provider))).toList();
        if (providerTransactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Step 2: dem giao dich thanh cong va tinh phan tram.
        long successCount = providerTransactions.stream().filter(
                transaction -> SUCCESS_STATUSES.contains(normalizeUpper(transaction.getStatus())))
            .count();
        return BigDecimal.valueOf(successCount * 100.0 / providerTransactions.size())
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tai transaction lien quan den danh sach gateway hien tai.
     *
     * @param gateways danh sach gateway dang can tinh summary hoac health
     * @return {@link List} transaction lien quan den cac provider dang quan ly
     */
    private List<PaymentTransactionEntity> loadTransactionsForGateways(
        List<PaymentGatewayConfigEntity> gateways) {
        // Step 1: trich xuat danh sach provider can tim giao dich.
        List<String> providers =
            gateways.stream().map(PaymentGatewayConfigEntity::getProvider).toList();
        if (providers.isEmpty()) {
            return List.of();
        }

        // Step 2: tai transaction theo provider tu bang payment_transaction.
        return paymentTransactionRepository.findByProviderIn(providers);
    }

    /**
     * Parse truong config_json thanh map de service thao tac.
     *
     * @param configJson json metadata luu kem trong payment_gateway_config
     * @return {@link Map} metadata da parse; tra ve map rong neu json khong hop le
     */
    private Map<String, Object> parseConfig(String configJson) {
        // Step 1: chan som chuoi rong de tranh parse khong can thiet.
        if (isBlank(configJson)) {
            return new LinkedHashMap<>();
        }

        // Step 2: parse json sang map de map lai DTO cho UI cu.
        try {
            return objectMapper.readValue(configJson, CONFIG_MAP_TYPE);
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    /**
     * Ghi metadata map ve json de luu vao truong config_json.
     *
     * @param metadata du lieu mo rong can luu kem theo gateway config
     * @return {@link String} chuoi json de luu DB
     */
    private String writeConfig(Map<String, Object> metadata) {
        // Step 1: bo cac key co gia tri null de json gon hon.
        metadata.entrySet().removeIf(entry -> entry.getValue() == null);

        // Step 2: convert map sang json string de luu.
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot serialize payment gateway config metadata.");
        }
    }

    /**
     * Tao display name tu metadata hoac provider de hien thi tren UI.
     *
     * @param gateway gateway config moi
     * @return {@link String} ten hien thi phu hop cho UI
     */
    private String resolveDisplayName(PaymentGatewayConfigEntity gateway) {
        // Step 1: uu tien displayName trong config_json neu co.
        Map<String, Object> metadata = parseConfig(gateway.getConfigJson());
        String displayName = stringValue(metadata.get("displayName"));
        if (!isBlank(displayName)) {
            return displayName;
        }

        // Step 2: fallback ve provider da format de tranh null tren giao dien.
        return resolveProviderLabel(gateway.getProvider());
    }

    /**
     * Ghep endpoint base va create path thanh chuoi endpoint hien thi cho UI.
     *
     * @param gateway gateway config moi
     * @return {@link String} endpoint thanh toan day du
     */
    private String buildPaymentEndpoint(PaymentGatewayConfigEntity gateway) {
        // Step 1: lay base endpoint va path cau hinh hien tai.
        String baseUrl = blankToNull(gateway.getEndpointBaseUrl());
        String path = blankToNull(gateway.getCreateApiPath());
        if (baseUrl == null) {
            return null;
        }

        // Step 2: neu khong co path thi tra ve base; neu co thi ghep lai thanh URL day du.
        if (path == null) {
            return baseUrl;
        }
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl + path.substring(1);
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    /**
     * Tao endpoint mac dinh theo provider de giu tuong thich voi popup tao gateway hien tai.
     *
     * @param provider ma provider cua gateway dang duoc tao hoac cap nhat
     * @return {@link String} endpoint mac dinh phu hop voi provider
     */
    private String defaultPaymentEndpoint(String provider) {
        return switch (normalizeProvider(provider)) {
            case "vnpay" -> "https://pay.vnpay.vn/vpcpay.html";
            case "momo" -> "https://test-payment.momo.vn/v2/gateway/api/create";
            case "stripe" -> "https://api.stripe.com/v1/checkout/sessions";
            case "bank" -> "https://bank.example.vn/transfer";
            default -> "https://api.example.com/payments/" + normalizeProvider(provider);
        };
    }

    /**
     * Tao return url mac dinh theo provider de giam so field bat buoc khi tao moi.
     *
     * @param provider ma provider cua gateway dang duoc tao hoac cap nhat
     * @return {@link String} return url mac dinh
     */
    private String defaultReturnUrl(String provider) {
        return "https://elearning.vn/payments/return/" + normalizeProvider(provider);
    }

    /**
     * Tao webhook url mac dinh theo provider de giam so field bat buoc khi tao moi.
     *
     * @param provider ma provider cua gateway dang duoc tao hoac cap nhat
     * @return {@link String} webhook url mac dinh
     */
    private String defaultWebhookUrl(String provider) {
        return "https://elearning.vn/api/webhooks/" + normalizeProvider(provider);
    }

    /**
     * Lay gia tri cau hinh uu tien theo request, du lieu DB hien co va fallback mac dinh.
     *
     * @param requestValue gia tri gui len tu frontend
     * @param currentValue gia tri dang luu trong DB
     * @param defaultValue gia tri mac dinh khi hai nguon tren deu rong
     * @param message thong diep loi neu khong tim thay gia tri hop le
     * @return {@link String} gia tri cau hinh cuoi cung sau khi chuan hoa
     */
    private String resolveConfigValue(String requestValue, String currentValue, String defaultValue,
        String message) {
        // Step 1: uu tien gia tri request neu frontend co gui len.
        String normalizedRequestValue = blankToNull(requestValue);
        if (normalizedRequestValue != null) {
            return normalizedRequestValue;
        }

        // Step 2: fallback ve gia tri dang luu trong DB neu da ton tai.
        String normalizedCurrentValue = blankToNull(currentValue);
        if (normalizedCurrentValue != null) {
            return normalizedCurrentValue;
        }

        // Step 3: dung gia tri mac dinh de giu luong tao gateway cu van chay duoc.
        if (!isBlank(defaultValue)) {
            return defaultValue;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Xac dinh nhan moi truong tong hop tu danh sach gateway moi.
     *
     * @param gateways danh sach gateway hien tai
     * @return {@link String} nhan moi truong de hien thi tren summary card
     */
    private String resolveEnvironmentLabel(List<PaymentGatewayConfigEntity> gateways) {
        // Step 1: loc danh sach moi truong khac nhau cua cac gateway.
        List<String> environments =
            gateways.stream().map(PaymentGatewayConfigEntity::getEnvironment)
                .filter(value -> !isBlank(value)).map(this::normalizeUpper).distinct().toList();
        if (environments.isEmpty()) {
            return "--";
        }

        // Step 2: tra nhan tong hop don gian cho UI.
        return environments.size() == 1 ? environments.get(0) : "Mixed";
    }

    /**
     * Chuyen trang thai gateway thanh class badge hien thi tren UI.
     *
     * @param status trang thai hien tai cua gateway
     * @return {@link String} class badge bootstrap cho frontend
     */
    private String statusBadgeClass(String status) {
        // Step 1: chuan hoa status de switch theo gia tri thong nhat.
        String normalizedStatus = normalizeUpper(status);

        // Step 2: tra class badge tuong ung.
        return switch (normalizedStatus) {
            case "LIVE" -> "badge bg-success";
            case "REVIEW" -> "badge bg-warning text-dark";
            case "MANUAL" -> "badge bg-info text-dark";
            case "DISABLED" -> "badge bg-secondary";
            case "SANDBOX" -> "badge bg-primary";
            default -> "badge bg-secondary";
        };
    }

    /**
     * An secret truoc khi tra ve frontend.
     *
     * @param secretRef secret dang duoc luu trong bang moi
     * @return {@link String} chuoi da an de hien thi an toan tren UI
     */
    private String maskSecret(String secretRef) {
        // Step 1: chan som truong hop secret rong.
        if (isBlank(secretRef)) {
            return "";
        }

        // Step 2: luon tra ve masked text co dinh.
        return "********";
    }

    /**
     * Chuan hoa provider code ve lower-case khong khoang trang.
     *
     * @param provider ma provider dau vao
     * @return {@link String} provider da chuan hoa
     */
    private String normalizeProvider(String provider) {
        // Step 1: bat buoc provider khong duoc rong.
        String requiredProvider = required(provider, "Gateway code is required");

        // Step 2: chuan hoa lower-case de tim kiem nhat quan trong DB.
        return requiredProvider.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Chuan hoa trang thai gateway dua tren request cu va schema moi.
     *
     * @param status status gui len tu frontend cu
     * @param enabled co bat gateway hay khong
     * @param sandboxMode co dang o che do sandbox hay khong
     * @return {@link String} status chuan hoa de hien thi tren UI
     */
    private String normalizeStatus(String status, Boolean enabled, Boolean sandboxMode) {
        // Step 1: uu tien status do frontend gui len neu hop le.
        String normalizedStatus = normalizeUpper(status);
        if (!isBlank(normalizedStatus)) {
            return normalizedStatus;
        }

        // Step 2: fallback tu enabled va sandbox mode neu frontend khong gui status.
        if (!Boolean.TRUE.equals(enabled)) {
            return "DISABLED";
        }
        return Boolean.TRUE.equals(sandboxMode) ? "SANDBOX" : "LIVE";
    }

    /**
     * Xac dinh environment luu trong bang moi dua tren sandbox mode va status hien tai.
     *
     * @param sandboxMode co bat sandbox mode hay khong
     * @param status status gui len tu frontend
     * @return {@link String} environment luu vao bang payment_gateway_config
     */
    private String resolveEnvironment(Boolean sandboxMode, String status) {
        // Step 1: uu tien environment sandbox neu frontend bat co nay.
        if (Boolean.TRUE.equals(sandboxMode)) {
            return "SANDBOX";
        }

        // Step 2: fallback ve status neu frontend gui environment-like status.
        String normalizedStatus = normalizeUpper(status);
        if ("REVIEW".equals(normalizedStatus) || "MANUAL".equals(normalizedStatus)) {
            return normalizedStatus;
        }
        return "LIVE";
    }

    /**
     * Kiem tra environment hien tai co phai sandbox hay khong.
     *
     * @param environment environment dang luu trong bang moi
     * @return true neu environment la sandbox; nguoc lai tra ve false
     */
    private boolean isSandboxEnvironment(String environment) {
        // Step 1: chuan hoa environment sang upper-case de so sanh.
        return "SANDBOX".equals(normalizeUpper(environment));
    }

    /**
     * Chuyen provider code thanh nhan de doc cho UI.
     *
     * @param provider ma provider dang luu trong DB
     * @return {@link String} nhan de doc cho nguoi dung admin
     */
    private String resolveProviderLabel(String provider) {
        // Step 1: chuan hoa provider code.
        String normalizedProvider = normalizeProvider(provider);

        // Step 2: map cac provider pho bien sang ten hien thi.
        return switch (normalizedProvider) {
            case "vnpay" -> "VNPay";
            case "momo" -> "MoMo";
            case "stripe" -> "Stripe";
            case "bank" -> "Chuyen khoan";
            default -> normalizedProvider.toUpperCase(Locale.ROOT);
        };
    }

    /**
     * Chuyen object ve string sau khi parse metadata json.
     *
     * @param value gia tri metadata dang can ep ve string
     * @return {@link String} gia tri string hoac null neu khong co
     */
    private String stringValue(Object value) {
        // Step 1: tra ve null neu metadata khong ton tai.
        if (value == null) {
            return null;
        }

        // Step 2: chuyen doi object sang string de service dung thong nhat.
        return String.valueOf(value);
    }

    /**
     * Chuyen object metadata ve integer va fallback ve gia tri mac dinh neu can.
     *
     * @param value gia tri metadata can convert
     * @param defaultValue gia tri mac dinh khi convert that bai
     * @return gia tri integer hop le de luu vao entity
     */
    private Integer resolveInteger(Object value, int defaultValue) {
        // Step 1: tra ve mac dinh neu metadata rong.
        if (value == null) {
            return defaultValue;
        }

        // Step 2: convert ve integer voi fallback an toan.
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Chuyen gia tri thanh BigDecimal an toan cho cac truong phi phan tram.
     *
     * @param value gia tri dau vao dang string hoac BigDecimal
     * @return {@link BigDecimal} gia tri da chuan hoa; fallback ve 0 neu khong hop le
     */
    private BigDecimal safeDecimal(Object value) {
        // Step 1: fallback ve 0 neu gia tri rong.
        if (value == null || isBlank(String.valueOf(value))) {
            return BigDecimal.ZERO;
        }

        // Step 2: convert ve BigDecimal voi fallback an toan.
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Chuyen chuoi rong thanh null truoc khi luu vao DB.
     *
     * @param value gia tri string dau vao
     * @return {@link String} chuoi da trim hoac null neu rong
     */
    private String blankToNull(String value) {
        // Step 1: tra ve null neu chuoi rong hoac null.
        if (isBlank(value)) {
            return null;
        }

        // Step 2: trim chuoi truoc khi luu.
        return value.trim();
    }

    /**
     * Bat buoc chuoi khong duoc rong.
     *
     * @param value gia tri can validate
     * @param message thong diep loi khi du lieu rong
     * @return {@link String} chuoi da trim neu hop le
     */
    private String required(String value, String message) {
        // Step 1: chan som gia tri rong de tra loi nghiep vu ro rang.
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }

        // Step 2: trim chuoi truoc khi su dung tiep.
        return value.trim();
    }

    /**
     * Kiem tra chuoi rong hoac null.
     *
     * @param value gia tri string dau vao
     * @return true neu rong hoac null; nguoc lai tra ve false
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Chuan hoa chuoi ve upper-case an toan.
     *
     * @param value gia tri string dau vao
     * @return {@link String} chuoi upper-case; tra ve rong neu dau vao null
     */
    private String normalizeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Lay gia tri dau tien khong rong trong hai chuoi.
     *
     * @param first gia tri uu tien truoc
     * @param second gia tri fallback sau
     * @return {@link String} gia tri hop le dau tien tim duoc
     */
    private String coalesce(String first, String second) {
        return isBlank(first) ? second : first;
    }


    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Chen 1 trieu ban ghi vao bang payment_transaction de test batch insert.
     */
    void insertOneMillionRecords() {
        int totalRecords = 1000000;
        int batchSize = 5000; // Khớp với batch_size đã cấu hình

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalRecords; i++) {
            PaymentTransactionEntity transaction = new PaymentTransactionEntity();

            LocalDateTime now = DateUtil.getRandomDateTimeInCurrentYear();

            // Không set ID vì dùng GenerationType.IDENTITY (để DB tự tăng)
            transaction.setPaymentCode("PAY-" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16) + i);
            transaction.setOrderId("ORD-" + i + "-" + UUID.randomUUID().toString().substring(0, 8));
            transaction.setStudentId("28f4f5e7-0e08-48af-8a02-ed6c2126d465");
            transaction.setCourseId(2L);
            transaction.setProvider("vnpay");
            transaction.setAmount(new BigDecimal("129.00"));
            transaction.setCurrency("VND");
            transaction.setStatus("COMPLETED");
            transaction.setPaymentUrl("https://pay.vnpay.vn/vpcpay.html");
            transaction.setRequestPayload("{\"amount\": 129.0, \"orderId\": \"ORD-" + i + VietnameseNameUtil.getRandomFullName() + "\"}");
            transaction.setResponsePayload("{\"status\": \"COMPLETED\", \"provider\": \"vnpay\"}");
            transaction.setCreatedAt(now);
            transaction.setUpdatedAt(now);

            // Lưu vào Persistence Context
            entityManager.persist(transaction);

            // Cứ đủ 5000 bản ghi thì đẩy (flush) xuống DB và xóa (clear) bộ nhớ RAM của JPA
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
                System.out.println("Đã xử lý xong " + i + " bản ghi...");
            }
        }

        // Xử lý nốt phần dư còn lại
        entityManager.flush();
        entityManager.clear();

        long endTime = System.currentTimeMillis();
        System.out.println("Hoàn thành chèn 1 triệu bản ghi bằng JPA mất: " + (endTime - startTime) / 1000.0 + " giây.");
    }

}
