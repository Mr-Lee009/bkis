package vn.edu.bkis.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.CoursePaymentGatewayDto;
import vn.edu.bkis.dto.CourseSignupFormDto;
import vn.edu.bkis.dto.CourseSignupPageDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.Enrollment;
import vn.edu.bkis.model.EnrollmentStatus;
import vn.edu.bkis.model.PaymentGatewayConfigEntity;
import vn.edu.bkis.model.PaymentTransactionEntity;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.EnrollmentRepository;
import vn.edu.bkis.repository.PaymentGatewayConfigRepository;
import vn.edu.bkis.repository.PaymentTransactionRepository;

@Service
public class CourseSignupService {
    private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";
    private static final String DEFAULT_CURRENCY = "VND";
    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final tools.jackson.core.type.TypeReference<Map<String, Object>>
        CONFIG_MAP_TYPE = new tools.jackson.core.type.TypeReference<>() {
    };

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentGatewayConfigRepository paymentGatewayConfigRepository;
    private final tools.jackson.databind.ObjectMapper objectMapper;

    /**
     * Khoi tao service xu ly trang thanh toan khoa hoc voi schema gateway va transaction moi.
     *
     * @param courseRepository repository khoa hoc
     * @param enrollmentRepository repository ghi danh khoa hoc
     * @param paymentTransactionRepository repository giao dich thanh toan moi
     * @param paymentGatewayConfigRepository repository cau hinh cong thanh toan moi
     * @param objectMapper bo parse config_json cua gateway
     */
    public CourseSignupService(CourseRepository courseRepository,
        EnrollmentRepository enrollmentRepository,
        PaymentTransactionRepository paymentTransactionRepository,
        PaymentGatewayConfigRepository paymentGatewayConfigRepository,
        tools.jackson.databind.ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentGatewayConfigRepository = paymentGatewayConfigRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Lay du lieu khoa hoc va hoc vien cho trang thanh toan.
     *
     * @param courseId id khoa hoc dang duoc thanh toan
     * @param student hoc vien dang dang nhap
     * @return du lieu tong hop cho trang checkout khoa hoc
     */
    public CourseSignupPageDto getSignupPage(Long courseId, User student) {
        // Step 1: kiem tra hoc vien hien tai co duoc phep thanh toan khoa hoc hay khong.
        validateStudent(student);

        // Step 2: tai khoa hoc active, trang thai ghi danh va cac gateway dang bat tu DB.
        Course course = getActiveCourse(courseId);
        boolean alreadyEnrolled =
            enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(student.getId(), courseId,
                EnrollmentStatus.ACTIVE);
        List<CoursePaymentGatewayDto> paymentGateways = getEnabledPaymentGateways();

        // Step 3: dong goi du lieu de template hien thi form thanh toan va tom tat don hang.
        return new CourseSignupPageDto(course.getId(), course.getTitle(), course.getDescription(),
            normalizeImage(course.getImageUrl()), course.getPrice(), student.getFullName(),
            student.getEmail(), alreadyEnrolled, paymentGateways);
    }

    /**
     * Xu ly dang ky khoa hoc va tao giao dich thanh toan mo phong.
     *
     * @param courseId id khoa hoc dang duoc mua
     * @param student hoc vien dang thanh toan
     * @param form form gui len tu trang signup
     * @return khong tra ve gia tri; method chi luu transaction va enrollment
     */
    @Transactional
    public void signup(Long courseId, User student, CourseSignupFormDto form) {
        // Step 1: kiem tra user, form va khoa hoc truoc khi tao giao dich.
        validateStudent(student);
        PaymentGatewayConfigEntity gateway = validateSignupForm(form);
        Course course = getActiveCourse(courseId);

        // Step 2: khong thu tien lai neu hoc vien da co enrollment ACTIVE cho khoa hoc nay.
        enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
            .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
            .ifPresent(enrollment -> {
                throw new IllegalArgumentException(
                    "Ban da thanh toan va co quyen hoc khoa hoc nay.");
            });

        // Step 3: tao payment_transaction COMPLETED theo cong thanh toan da chon va gan vao enrollment.
        PaymentTransactionEntity paymentTransaction =
            createCompletedPaymentTransaction(student, course, gateway);
        enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
            .ifPresentOrElse(
                enrollment -> activateEnrollment(enrollment, paymentTransaction.getPaymentCode(),
                    student.getUsername()),
                () -> createEnrollment(student, course, paymentTransaction.getPaymentCode()));
    }

    /**
     * Kiem tra student co quyen thanh toan khoa hoc hay khong.
     *
     * @param student user hien tai
     * @return khong tra ve gia tri; method nem loi neu user khong hop le
     */
    private void validateStudent(User student) {
        // Step 1: chi role STUDENT moi duoc mua va ghi danh khoa hoc.
        if (student == null || student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException(
                "Chi tai khoan hoc vien moi duoc thanh toan khoa hoc.");
        }

        // Step 2: tai khoan bi khoa khong duoc tao giao dich moi.
        if (Boolean.TRUE.equals(student.getLocked())) {
            throw new IllegalArgumentException(
                "Tai khoan cua ban dang bi khoa nen khong the thanh toan khoa hoc.");
        }
    }

    /**
     * Kiem tra form thanh toan va tra ve gateway cau hinh hop le.
     *
     * @param form du lieu thanh toan gui len tu frontend
     * @return gateway da duoc bat trong payment_gateway_config
     */
    private PaymentGatewayConfigEntity validateSignupForm(CourseSignupFormDto form) {
        // Step 1: yeu cau dong y dieu khoan truoc khi tao giao dich.
        if (form == null || !form.isAcceptedTerms()) {
            throw new IllegalArgumentException(
                "Ban can dong y dieu khoan dich vu truoc khi thanh toan khoa hoc.");
        }

        // Step 2: chuan hoa paymentMethod theo provider trong bang payment_gateway_config.
        String paymentMethod = form.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException(
                "Ban can chon cong thanh toan truoc khi thanh toan khoa hoc.");
        }
        String provider = normalizeGatewayCode(paymentMethod);

        // Step 3: chi chap nhan gateway dang enabled trong cau hinh he thong.
        return paymentGatewayConfigRepository.findByProvider(provider)
            .filter(gateway -> Boolean.TRUE.equals(gateway.getEnabled())).orElseThrow(
                () -> new IllegalArgumentException(
                    "Cong thanh toan da chon chua duoc bat trong cau hinh he thong."));
    }

    /**
     * Lay danh sach gateway dang bat tu payment_gateway_config.
     *
     * @return danh sach gateway de render cho trang checkout
     */
    private List<CoursePaymentGatewayDto> getEnabledPaymentGateways() {
        // Step 1: doc gateway theo thu tu uu tien da cau hinh trong DB.
        List<PaymentGatewayConfigEntity> gateways =
            paymentGatewayConfigRepository.findAllByOrderByPriorityAscIdAsc();

        // Step 2: loc gateway dang bat va map sang DTO gon nhe cho frontend.
        return gateways.stream().filter(gateway -> Boolean.TRUE.equals(gateway.getEnabled()))
            .map(this::toCheckoutGateway).toList();
    }

    /**
     * Chuyen gateway config moi sang DTO hien thi checkout.
     *
     * @param gateway gateway dang duoc bat trong he thong
     * @return DTO thong tin gon nhe cho trang signup
     */
    private CoursePaymentGatewayDto toCheckoutGateway(PaymentGatewayConfigEntity gateway) {
        // Step 1: lay metadata tu config_json de xac dinh displayName, providerType va mo ta.
        Map<String, Object> metadata = parseConfig(gateway.getConfigJson());
        String providerType = normalizeGatewayCode(
            coalesce(stringValue(metadata.get("providerType")), gateway.getProvider()));
        String displayName = coalesce(stringValue(metadata.get("displayName")),
            resolveProviderLabel(gateway.getProvider()));
        String description =
            coalesce(stringValue(metadata.get("description")), "Cong thanh toan " + displayName);

        // Step 2: tao DTO chi gom thong tin an toan cho user chon thanh toan.
        return new CoursePaymentGatewayDto(gateway.getProvider(), displayName, providerType,
            description, gatewayBadgeText(providerType), gatewayBadgeClass(providerType));
    }

    /**
     * Tao nhan ngan cho gateway checkout.
     *
     * @param providerType provider type da chuan hoa
     * @return chuoi hien thi ngan gon tren badge
     */
    private String gatewayBadgeText(String providerType) {
        // Step 1: hien thi nhan theo nhom gateway pho bien.
        return switch (providerType) {
            case "vnpay" -> "VNPay";
            case "momo" -> "MoMo";
            case "stripe" -> "Card";
            case "bank_transfer", "bank" -> "Bank";
            case "paypal" -> "PayPal";
            default -> providerType;
        };
    }

    /**
     * Tao class mau cho badge gateway.
     *
     * @param providerType provider type da chuan hoa
     * @return class CSS dung trong template
     */
    private String gatewayBadgeClass(String providerType) {
        // Step 1: map mau badge theo provider de user nhan dien nhanh hon.
        return switch (providerType) {
            case "vnpay" -> "gateway-vnpay";
            case "momo" -> "gateway-momo";
            case "stripe" -> "gateway-visa";
            case "bank_transfer", "bank" -> "gateway-bank";
            case "paypal" -> "gateway-paypal";
            default -> "";
        };
    }

    /**
     * Chuan hoa code gateway ve lower-case.
     *
     * @param value gia tri gateway/provider dau vao
     * @return code da chuan hoa de so sanh DB
     */
    private String normalizeGatewayCode(String value) {
        // Step 1: cat khoang trang va chuyen ve lower-case de khop voi provider trong DB.
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Lay khoa hoc dang active theo courseId.
     *
     * @param courseId id khoa hoc can thanh toan
     * @return khoa hoc hop le dang mo ban
     */
    private Course getActiveCourse(Long courseId) {
        // Step 1: tim khoa hoc trong database va chi chap nhan khoa hoc dang active.
        return courseRepository.findById(courseId)
            .filter(course -> Boolean.TRUE.equals(course.getActiveFlag())).orElseThrow(
                () -> new IllegalArgumentException("Khong tim thay khoa hoc dang mo thanh toan."));
    }

    /**
     * Tao giao dich thanh cong mo phong trong bang payment_transaction.
     *
     * @param student hoc vien dang thanh toan
     * @param course khoa hoc dang duoc mua
     * @param gateway gateway da duoc validate
     * @return giao dich thanh toan moi da duoc luu
     */
    private PaymentTransactionEntity createCompletedPaymentTransaction(User student, Course course,
        PaymentGatewayConfigEntity gateway) {
        // Step 1: tao payment code va order id duy nhat cho giao dich moi.
        String paymentCode = generatePaymentCode();
        String orderId = generateOrderId(course.getId(), student.getId());
        LocalDateTime now = LocalDateTime.now();

        // Step 2: map giao dich moi theo schema payment_transaction hien tai.
        PaymentTransactionEntity paymentTransaction = new PaymentTransactionEntity();
        paymentTransaction.setPaymentCode(paymentCode);
        paymentTransaction.setOrderId(orderId);
        paymentTransaction.setStudentId(student.getId());
        paymentTransaction.setCourseId(course.getId());
        paymentTransaction.setProvider(gateway.getProvider());
        paymentTransaction.setAmount(course.getPrice());
        paymentTransaction.setCurrency(DEFAULT_CURRENCY);
        paymentTransaction.setStatus(COMPLETED_STATUS);
        paymentTransaction.setPaymentUrl(gateway.getEndpointBaseUrl());
        paymentTransaction.setRequestPayload(
            buildRequestPayload(student, course, gateway, paymentCode, orderId));
        paymentTransaction.setResponsePayload(buildResponsePayload(gateway));
        paymentTransaction.setPaidAt(now);
        paymentTransaction.setCreatedAt(now);
        paymentTransaction.setUpdatedAt(now);

        // Step 3: luu giao dich de enrollment co the tham chieu bang payment_code.
        return paymentTransactionRepository.save(paymentTransaction);
    }

    /**
     * Tao enrollment ACTIVE moi cho student va course.
     *
     * @param student hoc vien vua thanh toan
     * @param course khoa hoc vua duoc mua
     * @param paymentCode ma giao dich thanh toan moi
     * @return khong tra ve gia tri; method chi luu enrollment
     */
    private void createEnrollment(User student, Course course, String paymentCode) {
        // Step 1: khoi tao enrollment tu khoa hoc, hoc vien va payment_code vua hoan tat.
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setPaymentCode(paymentCode);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());

        // Step 2: luu enrollment de hoc vien co quyen truy cap khoa hoc.
        enrollment.setCreatedBy(student.getUsername());
        enrollment.setUpdatedBy(student.getUsername());
        enrollmentRepository.save(enrollment);
    }

    /**
     * Kich hoat lai enrollment da ton tai va gan payment_code moi nhat.
     *
     * @param enrollment enrollment dang ton tai
     * @param paymentCode ma giao dich vua tao
     * @param username username dung cho audit
     * @return khong tra ve gia tri; method chi cap nhat enrollment
     */
    private void activateEnrollment(Enrollment enrollment, String paymentCode, String username) {
        // Step 1: gan lai trang thai ACTIVE va lien ket payment_code moi nhat.
        enrollment.setPaymentCode(paymentCode);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        if (enrollment.getEnrolledAt() == null) {
            enrollment.setEnrolledAt(LocalDateTime.now());
        }

        // Step 2: cap nhat audit va luu thay doi enrollment.
        enrollment.setUpdatedBy(username);
        enrollmentRepository.save(enrollment);
    }

    /**
     * Tao request payload mock de luu lai trong payment_transaction.
     *
     * @param student hoc vien dang thanh toan
     * @param course khoa hoc dang duoc mua
     * @param gateway gateway duoc chon
     * @param paymentCode ma giao dich noi bo
     * @param orderId ma don hang noi bo
     * @return chuoi json request payload de phuc vu debug
     */
    private String buildRequestPayload(User student, Course course,
        PaymentGatewayConfigEntity gateway, String paymentCode, String orderId) {
        // Step 1: tao payload mock gon nhe de debug checkout flow va doi soat sau nay.
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paymentCode", paymentCode);
        payload.put("orderId", orderId);
        payload.put("courseId", course.getId());
        payload.put("studentId", student.getId());
        payload.put("provider", gateway.getProvider());
        payload.put("amount", course.getPrice());

        // Step 2: convert payload sang json string de luu vao DB.
        return writeJson(payload);
    }

    /**
     * Tao response payload mock cho giao dich thanh cong.
     *
     * @param gateway gateway da xu ly giao dich
     * @return chuoi json response payload mock
     */
    private String buildResponsePayload(PaymentGatewayConfigEntity gateway) {
        // Step 1: tao payload response mock de admin page co the hien thi lich su giao dich.
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("provider", gateway.getProvider());
        payload.put("status", COMPLETED_STATUS);
        payload.put("environment", gateway.getEnvironment());

        // Step 2: convert payload sang json string de luu vao DB.
        return writeJson(payload);
    }

    /**
     * Sinh payment code noi bo duy nhat.
     *
     * @return ma giao dich noi bo dung de lien ket enrollment va transaction
     */
    private String generatePaymentCode() {
        // Step 1: dung UUID rut gon de tao ma giao dich an toan va de doc.
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Sinh order id noi bo cho giao dich mua khoa hoc.
     *
     * @param courseId id khoa hoc
     * @param studentId id hoc vien
     * @return ma don hang noi bo duy nhat
     */
    private String generateOrderId(Long courseId, String studentId) {
        // Step 1: ghep du lieu nghiep vu va timestamp de tao order id de truy vet.
        String studentToken = studentId == null ? "student" : studentId.replace("-", "");
        String shortStudentToken =
            studentToken.length() > 8 ? studentToken.substring(0, 8) : studentToken;
        return "ORD-" + courseId + "-" + shortStudentToken + "-" + System.currentTimeMillis();
    }

    /**
     * Parse config_json thanh metadata map de lay display name va provider type.
     *
     * @param configJson json metadata luu trong payment_gateway_config
     * @return map metadata; tra ve rong neu json khong hop le
     */
    private Map<String, Object> parseConfig(String configJson) {
        // Step 1: chan som chuoi rong de tranh parse khong can thiet.
        if (configJson == null || configJson.isBlank()) {
            return Map.of();
        }

        // Step 2: parse json sang map va fallback ve map rong neu du lieu khong hop le.
        try {
            return objectMapper.readValue(configJson, CONFIG_MAP_TYPE);
        } catch (Exception ex) {
            return Map.of();
        }
    }

    /**
     * Convert payload map thanh json string de luu vao DB.
     *
     * @param payload du lieu can ghi thanh json
     * @return chuoi json da duoc serialize
     */
    private String writeJson(Map<String, Object> payload) {
        // Step 1: serialize payload thanh json string de luu vao request_payload hoac response_payload.
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Khong the tao du lieu thanh toan mo phong.");
        }
    }

    /**
     * Chuyen gia tri metadata sang string.
     *
     * @param value gia tri metadata can doc
     * @return chuoi string hoac null neu gia tri rong
     */
    private String stringValue(Object value) {
        // Step 1: tra ve null neu metadata khong ton tai.
        if (value == null) {
            return null;
        }

        // Step 2: convert object sang string de xu ly thong nhat.
        return String.valueOf(value);
    }

    /**
     * Chuyen provider code thanh nhan de doc cho UI.
     *
     * @param provider ma provider luu trong he thong
     * @return ten hien thi don gian cho user
     */
    private String resolveProviderLabel(String provider) {
        // Step 1: chuan hoa provider code de map nhat quan.
        String normalizedProvider = normalizeGatewayCode(provider);

        // Step 2: tra nhan hien thi tuong ung cho provider pho bien.
        return switch (normalizedProvider) {
            case "vnpay" -> "VNPay";
            case "momo" -> "MoMo";
            case "stripe" -> "Stripe";
            case "bank", "bank_transfer" -> "Chuyen khoan";
            default -> normalizedProvider.toUpperCase(Locale.ROOT);
        };
    }

    /**
     * Lay gia tri dau tien khong rong trong hai chuoi.
     *
     * @param first gia tri uu tien truoc
     * @param second gia tri fallback sau
     * @return gia tri hop le dau tien tim duoc
     */
    private String coalesce(String first, String second) {
        // Step 1: fallback ve gia tri thu hai neu gia tri dau rong.
        return first == null || first.isBlank() ? second : first;
    }

    /**
     * Chuan hoa imageUrl cua khoa hoc de template luon render duoc.
     *
     * @param imageUrl duong dan anh khoa hoc
     * @return duong dan anh hop le de frontend hien thi
     */
    private String normalizeImage(String imageUrl) {
        // Step 1: dung anh mac dinh neu khoa hoc chua co anh.
        if (imageUrl == null || imageUrl.isBlank()) {
            return DEFAULT_COURSE_IMAGE;
        }

        // Step 2: giu nguyen URL tuyet doi hoac duong dan da bat dau bang slash.
        if (imageUrl.startsWith("http://") || imageUrl.startsWith(
            "https://") || imageUrl.startsWith("/")) {
            return imageUrl;
        }

        // Step 3: them slash dau duong dan tuong doi de template truy cap qua static resource.
        return "/" + imageUrl;
    }
}
