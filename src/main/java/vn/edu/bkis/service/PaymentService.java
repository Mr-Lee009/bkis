package vn.edu.bkis.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.CourseSignupFormDto;
import vn.edu.bkis.dto.payment.CreatePaymentRequestDto;
import vn.edu.bkis.dto.payment.PaymentDetailResponseDto;
import vn.edu.bkis.dto.payment.PaymentProvider;
import vn.edu.bkis.dto.payment.ResponseCreatePaymentDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.PaymentGatewayConfigEntity;
import vn.edu.bkis.model.PaymentStatus;
import vn.edu.bkis.model.PaymentTransactionEntity;
import vn.edu.bkis.model.User;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.PaymentGatewayConfigRepository;
import vn.edu.bkis.repository.PaymentTransactionRepository;
import vn.edu.bkis.service.gateway.PaymentGateway;
import vn.edu.bkis.service.gateway.PaymentGatewayResolver;

@Service
public class PaymentService {

    private static final String DEFAULT_CURRENCY = "VND";
    private static final String SUCCESS_STATUS = PaymentStatus.COMPLETED.name();
    private static final String FAILED_STATUS = PaymentStatus.FAILED.name();
    private static final String PENDING_STATUS = PaymentStatus.PENDING.name();

    private final CourseRepository courseRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentGatewayConfigRepository paymentGatewayConfigRepository;
    private final PaymentGatewayResolver paymentGatewayResolver;

    /**
     * Khoi tao PaymentService voi cac repository va resolver can thiet cho payment flow.
     *
     * @param courseRepository repository khoa hoc
     * @param paymentTransactionRepository repository giao dich thanh toan
     * @param paymentGatewayConfigRepository repository cau hinh gateway
     * @param paymentGatewayResolver bo phan giai quyet adapter gateway theo provider
     */
    public PaymentService(CourseRepository courseRepository,
        PaymentTransactionRepository paymentTransactionRepository,
        PaymentGatewayConfigRepository paymentGatewayConfigRepository,
        PaymentGatewayResolver paymentGatewayResolver) {
        this.courseRepository = courseRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentGatewayConfigRepository = paymentGatewayConfigRepository;
        this.paymentGatewayResolver = paymentGatewayResolver;
    }

    /**
     * Tao giao dich thanh toan cho khoa hoc va tra ve thong tin de frontend redirect.
     *
     * @param courseId id khoa hoc can thanh toan
     * @param student hoc vien dang dang nhap
     * @param request du lieu form checkout tu frontend
     * @return thong tin giao dich moi va duong dan thanh toan
     */
    @Transactional
    public ResponseCreatePaymentDto createCoursePayment(Long courseId, User student,
        CourseSignupFormDto request) {
        // Step 1: Validate hoc vien, form va khoa hoc truoc khi tao giao dich PENDING.
        validateStudent(student);
        validateRequest(request);
        Course course = getActiveCourse(courseId);
        PaymentGatewayConfigEntity gatewayConfig = getEnabledGateway(request.getPaymentMethod());

        // Step 2: Tao giao dich PENDING va luu vao payment_transaction.
        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setPaymentCode(generatePaymentCode());
        transaction.setOrderId(generateOrderId(courseId, student.getId()));
        transaction.setStudentId(student.getId());
        transaction.setCourseId(course.getId());
        transaction.setProvider(gatewayConfig.getProvider());
        transaction.setAmount(course.getPrice());
        transaction.setCurrency(DEFAULT_CURRENCY);
        transaction.setStatus(PENDING_STATUS);
        transaction.setPaymentUrl(buildFallbackPaymentUrl(gatewayConfig));
        transaction.setRequestPayload(buildRequestPayload(course, student, request, gatewayConfig));
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        PaymentTransactionEntity savedTransaction = paymentTransactionRepository.save(transaction);

        // Step 3: Chon adapter theo provider qua resolver, neu co adapter thi cho adapter build request thanh toan.
        CreatePaymentRequestDto gatewayRequest = buildGatewayRequest(savedTransaction, request);
        PaymentGateway gateway = paymentGatewayResolver.resolve(gatewayConfig.getProvider())
            .orElse(null);
        if (gateway != null) {
            // Step 3.1: Goi adapter provider va lay ket qua tao payment de cap nhat paymentUrl.
            vn.edu.bkis.dto.payment.GatewayCreatePaymentResult gatewayResult =
                gateway.createPayment(gatewayRequest, gatewayConfig);
            if (gatewayResult == null || gatewayResult.getResultCode() == null
                || gatewayResult.getResultCode() != 0) {
                transaction.setStatus(FAILED_STATUS);
                transaction.setFailReason(gatewayResult == null ? "Không nhận được phản hồi từ cổng thanh toán." : gatewayResult.getMessage());
                transaction.setUpdatedAt(LocalDateTime.now());
                paymentTransactionRepository.save(transaction);
                throw new IllegalStateException(gatewayResult == null ? "Không nhận được phản hồi từ cổng thanh toán." : gatewayResult.getMessage());
            }
            if (gatewayResult.getPayUrl() != null && !gatewayResult.getPayUrl().isBlank()) {
                savedTransaction.setPaymentUrl(gatewayResult.getPayUrl());
                savedTransaction.setResponsePayload(gatewayResult.toString());
                savedTransaction.setUpdatedAt(LocalDateTime.now());
                paymentTransactionRepository.save(savedTransaction);
            }
        }

        // Step 4: Tra ve response cho controller de redirect hoac query trang thai.
        ResponseCreatePaymentDto response = new ResponseCreatePaymentDto();
        response.setPaymentCode(savedTransaction.getPaymentCode());
        response.setOrderId(savedTransaction.getOrderId());
        response.setProvider(parseProvider(savedTransaction.getProvider()));
        response.setStatus(PaymentStatus.PENDING);
        response.setAmount(savedTransaction.getAmount().longValue());
        response.setPaymentUrl(savedTransaction.getPaymentUrl());
        return response;
    }

    /**
     * Query trang thai giao dich theo payment code.
     *
     * @param paymentCode ma giao dich noi bo
     * @return chi tiet giao dich da chuan hoa
     */
    public PaymentDetailResponseDto getPaymentStatus(String paymentCode) {
        // Step 1: tim giao dich theo paymentCode trong repository.
        PaymentTransactionEntity transaction = paymentTransactionRepository.findByPaymentCode(paymentCode)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch thanh toán."));

        // Step 2: dong goi trang thai hien tai de frontend poll.
        PaymentDetailResponseDto response = new PaymentDetailResponseDto();
        response.setPaymentCode(transaction.getPaymentCode());
        response.setOrderId(transaction.getOrderId());
        response.setProvider(parseProvider(transaction.getProvider()));
        response.setStatus(parsePaymentStatus(transaction.getStatus()));
        response.setAmount(transaction.getAmount().longValue());
        response.setGatewayTransactionNo(transaction.getGatewayTransactionNo());
        return response;
    }

    /**
     * Xu ly callback tu provider va chuan hoa ket qua ve PaymentDetailResponseDto.
     *
     * @param provider ma cong thanh toan
     * @param queryParams tham so callback tu provider
     * @return ket qua callback da chuan hoa
     */
    @Transactional
    public PaymentDetailResponseDto handleCallback(String provider, Map<String, String> queryParams) {
        // Step 1: TODO hoan thien xac thuc callback va verify signature theo provider.
        String paymentCode = queryParams == null ? null : queryParams.get("paymentCode");
        if (paymentCode == null || paymentCode.isBlank()) {
            throw new IllegalArgumentException("Thiếu paymentCode trong callback.");
        }

        // Step 2: tim giao dich va cap nhat trang thai cuoi cung.
        PaymentTransactionEntity transaction = paymentTransactionRepository.findByPaymentCode(paymentCode)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch thanh toán."));

        boolean success = isSuccessCallback(queryParams);
        transaction.setStatus(success ? SUCCESS_STATUS : FAILED_STATUS);
        transaction.setProvider(normalizeProvider(provider));
        transaction.setCallbackPayload(String.valueOf(queryParams));
        transaction.setUpdatedAt(LocalDateTime.now());
        if (success) {
            transaction.setPaidAt(LocalDateTime.now());
            transaction.setFailReason(null);
        } else {
            transaction.setFailReason(resolveFailReason(queryParams));
        }
        paymentTransactionRepository.save(transaction);

        // Step 3: tra ve response da chuan hoa cho frontend hoac provider.
        PaymentDetailResponseDto response = new PaymentDetailResponseDto();
        response.setPaymentCode(transaction.getPaymentCode());
        response.setOrderId(transaction.getOrderId());
        response.setProvider(parseProvider(transaction.getProvider()));
        response.setStatus(parsePaymentStatus(transaction.getStatus()));
        response.setAmount(transaction.getAmount().longValue());
        response.setGatewayTransactionNo(transaction.getGatewayTransactionNo());
        return response;
    }

    /**
     * Kiem tra hoc vien hop le truoc khi tao payment.
     *
     * @param student hoc vien dang dang nhap
     * @throws IllegalArgumentException neu hoc vien khong hop le
     */
    private void validateStudent(User student) {
        // Step 1: chi chap nhan hoc vien hop le.
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException("Bạn cần đăng nhập bằng tài khoản học viên để thanh toán khóa học.");
        }
    }

    /**
     * Kiem tra du lieu checkout truoc khi xu ly payment.
     *
     * @param request du lieu form checkout
     * @throws IllegalArgumentException neu request thieu dieu khoan hoac gateway
     */
    private void validateRequest(CourseSignupFormDto request) {
        // Step 1: kiem tra dieu khoan va cong thanh toan.
        if (request == null || !request.isAcceptedTerms()) {
            throw new IllegalArgumentException("Bạn cần đồng ý điều khoản dịch vụ trước khi thanh toán khóa học.");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new IllegalArgumentException("Bạn cần chọn cổng thanh toán trước khi thanh toán khóa học.");
        }
    }

    /**
     * Lay khoa hoc dang active de thanh toan.
     *
     * @param courseId id khoa hoc
     * @return khoa hoc dang mo ban
     * @throws IllegalArgumentException neu khong tim thay khoa hoc hop le
     */
    private Course getActiveCourse(Long courseId) {
        // Step 1: chi chap nhan khoa hoc dang active.
        return courseRepository.findById(courseId)
            .filter(course -> Boolean.TRUE.equals(course.getActiveFlag()))
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học đang mở thanh toán."));
    }

    /**
     * Lay cau hinh gateway da duoc bat tu DB.
     *
     * @param provider ma cong thanh toan
     * @return cau hinh gateway hop le
     * @throws IllegalArgumentException neu gateway khong ton tai hoac dang tat
     */
    private PaymentGatewayConfigEntity getEnabledGateway(String provider) {
        // Step 1: doc cau hinh gateway tu DB va loc gateway dang bat.
        return paymentGatewayConfigRepository.findByProvider(normalizeProvider(provider))
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .orElseThrow(() -> new IllegalArgumentException("Cổng thanh toán đã chọn chưa được bật trong hệ thống."));
    }

    /**
     * Tao paymentUrl tam thoi khi adapter provider chua hoan thien.
     *
     * @param gatewayConfig cau hinh gateway
     * @return duong dan thanh toan tam thoi
     */
    private String buildFallbackPaymentUrl(PaymentGatewayConfigEntity gatewayConfig) {
        // Step 1: giu url thanh toan tam thoi de flow khong bi dut trong khi adapter dang duoc hoan thien.
        return gatewayConfig.getEndpointBaseUrl();
    }

    /**
     * Dong goi request payload de doi soat va debug giao dich.
     *
     * @param course khoa hoc dang duoc mua
     * @param student hoc vien thanh toan
     * @param request du lieu checkout
     * @param gatewayConfig cau hinh gateway
     * @return chuoi payload dang text de luu DB
     */
    private String buildRequestPayload(Course course, User student, CourseSignupFormDto request,
        PaymentGatewayConfigEntity gatewayConfig) {
        // Step 1: dong goi thong tin can thiet de doi soat callback ve sau.
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("courseId", course.getId());
        payload.put("studentId", student.getId());
        payload.put("paymentMethod", request.getPaymentMethod());
        payload.put("provider", gatewayConfig.getProvider());
        payload.put("phone", request.getPhone());
        payload.put("learningGoal", request.getLearningGoal());
        payload.put("learningMode", request.getLearningMode());
        payload.put("couponCode", request.getCouponCode());
        return payload.toString();
    }

    /**
     * Chuyen doi transaction sang request chung cho adapter gateway.
     *
     * @param transaction giao dich dang xu ly
     * @param request du lieu checkout ban dau
     * @return request dung chung cho cac adapter gateway
     */
    private CreatePaymentRequestDto buildGatewayRequest(PaymentTransactionEntity transaction,
        CourseSignupFormDto request) {
        // Step 1: chuan bi request data chung cho adapter provider.
        CreatePaymentRequestDto gatewayRequest = new CreatePaymentRequestDto();
        gatewayRequest.setCourseId(transaction.getCourseId());
        gatewayRequest.setPaymentMethod(request.getPaymentMethod());
        gatewayRequest.setAcceptedTerms(request.isAcceptedTerms());
        gatewayRequest.setPhone(request.getPhone());
        gatewayRequest.setLearningGoal(request.getLearningGoal());
        gatewayRequest.setLearningMode(request.getLearningMode());
        gatewayRequest.setCouponCode(request.getCouponCode());
        gatewayRequest.setOrderId(transaction.getOrderId());
        gatewayRequest.setProvider(parseProvider(transaction.getProvider()));
        gatewayRequest.setAmount(transaction.getAmount());
        gatewayRequest.setDescription("Thanh toán khóa học " + transaction.getCourseId());
        gatewayRequest.setReturnUrl(null);
        return gatewayRequest;
    }

    /**
     * Tao payment code noi bo cho giao dich moi.
     *
     * @return payment code duy nhat
     */
    private String generatePaymentCode() {
        // Step 1: tao payment code ngan gon va duy nhat.
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 16);
    }

    /**
     * Tao order id noi bo de doi soat giao dich.
     *
     * @param courseId id khoa hoc
     * @param studentId id hoc vien
     * @return order id duy nhat
     */
    private String generateOrderId(Long courseId, String studentId) {
        // Step 1: tao order id de doi soat theo course va student.
        String studentToken = studentId == null ? "STU" : studentId.replaceAll("[^A-Za-z0-9]", "");
        if (studentToken.length() > 8) {
            studentToken = studentToken.substring(0, 8);
        }
        return "ORD-" + courseId + "-" + studentToken + "-" + System.currentTimeMillis();
    }

    /**
     * Kiem tra callback co dau hieu thanh cong hay khong.
     *
     * @param queryParams tham so callback tu provider
     * @return true neu callback duoc xem la thanh cong
     */
    private boolean isSuccessCallback(Map<String, String> queryParams) {
        // Step 1: todo map quy tac success theo provider thuc te.
        String status = queryParams == null ? null : queryParams.get("status");
        return status != null && ("SUCCESS".equalsIgnoreCase(status) || "00".equals(status));
    }

    /**
     * Lay ly do that bai tu callback provider.
     *
     * @param queryParams tham so callback tu provider
     * @return message loi neu co
     */
    private String resolveFailReason(Map<String, String> queryParams) {
        // Step 1: lay ly do loi tu callback neu co.
        if (queryParams == null) {
            return "Callback khong hop le.";
        }
        return queryParams.getOrDefault("message", "Thanh toan that bai.");
    }

    /**
     * Chuyen provider string ve enum de tra response.
     *
     * @param value chuoi provider can parse
     * @return provider enum neu khop
     */
    private PaymentProvider parseProvider(String value) {
        // Step 1: chuyen provider string ve enum de tra response.
        if (value == null) {
            return null;
        }
        return switch (normalizeProvider(value)) {
            case "momo" -> PaymentProvider.MOMO;
            case "vnpay" -> PaymentProvider.VN_PAY;
            case "zalo_pay", "zalopay" -> PaymentProvider.ZALO_PAY;
            default -> null;
        };
    }

    /**
     * Chuyen trang thai string ve enum PaymentStatus.
     *
     * @param value trang thai luu trong DB
     * @return enum trang thai neu hop le
     */
    private PaymentStatus parsePaymentStatus(String value) {
        // Step 1: chuyen status string ve enum.
        if (value == null) {
            return null;
        }
        try {
            return PaymentStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Chuan hoa provider ve lower-case de khop cau hinh gateway.
     *
     * @param provider ten provider can chuan hoa
     * @return provider da chuan hoa
     */
    private String normalizeProvider(String provider) {
        // Step 1: chuan hoa provider de khop voi payment_gateway_config.
        return provider == null ? "" : provider.trim().toLowerCase();
    }
}
