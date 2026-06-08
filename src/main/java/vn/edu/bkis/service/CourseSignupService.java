package vn.edu.bkis.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.CoursePaymentGatewayDto;
import vn.edu.bkis.dto.CourseSignupFormDto;
import vn.edu.bkis.dto.CourseSignupPageDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.Enrollment;
import vn.edu.bkis.model.EnrollmentStatus;
import vn.edu.bkis.model.Payment;
import vn.edu.bkis.model.PaymentGateway;
import vn.edu.bkis.model.PaymentStatus;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.EnrollmentRepository;
import vn.edu.bkis.repository.PaymentGatewayRepository;
import vn.edu.bkis.repository.PaymentsRepository;

@Service
public class CourseSignupService {
    private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentsRepository paymentsRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;

    // Khoi tao service xu ly trang thanh toan khoa hoc, can repository khoa hoc, ghi danh va thanh toan, khong tra ve gia tri.
    public CourseSignupService(CourseRepository courseRepository,
                               EnrollmentRepository enrollmentRepository,
                               PaymentsRepository paymentsRepository,
                               PaymentGatewayRepository paymentGatewayRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.paymentsRepository = paymentsRepository;
        this.paymentGatewayRepository = paymentGatewayRepository;
    }

    // Lay du lieu khoa hoc va hoc vien cho trang thanh toan; tham so courseId va student bat buoc, tra ve CourseSignupPageDto, nem loi neu user/khoa hoc khong hop le.
    public CourseSignupPageDto getSignupPage(Long courseId, User student) {
        // Step 1: Kiem tra hoc vien hien tai co duoc phep thanh toan khoa hoc hay khong.
        validateStudent(student);

        // Step 2: Tai khoa hoc active, trang thai ghi danh va cac gateway dang bat tu DB.
        Course course = getActiveCourse(courseId);
        boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), courseId, EnrollmentStatus.ACTIVE);
        List<CoursePaymentGatewayDto> paymentGateways = getEnabledPaymentGateways();

        // Step 3: Dong goi du lieu de template hien thi form thanh toan va tom tat don hang.
        return new CourseSignupPageDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                normalizeImage(course.getImageUrl()),
                course.getPrice(),
                student.getFullName(),
                student.getEmail(),
                alreadyEnrolled,
                paymentGateways
        );
    }

    // Xu ly thanh toan mo phong cho courseId va student; form chua paymentMethod/terms, khong tra ve gia tri, nem loi neu du lieu khong hop le.
    @Transactional
    public void signup(Long courseId, User student, CourseSignupFormDto form) {
        // Step 1: Kiem tra user, form va khoa hoc truoc khi tao giao dich.
        validateStudent(student);
        String paymentMethod = validateSignupForm(form);
        Course course = getActiveCourse(courseId);

        // Step 2: Khong thu tien lai neu hoc vien da co enrollment ACTIVE cho khoa hoc nay.
        enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .ifPresent(enrollment -> {
                    throw new IllegalArgumentException("Ban da thanh toan va co quyen hoc khoa hoc nay.");
                });

        // Step 3: Tao payment COMPLETED theo cong thanh toan da chon va gan vao enrollment.
        Payment payment = createCompletedPayment(student, course, paymentMethod);
        enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .ifPresentOrElse(
                        enrollment -> activateEnrollment(enrollment, payment.getId(), student.getUsername()),
                        () -> createEnrollment(student, course, payment.getId())
                );
    }

    // Kiem tra student co quyen thanh toan; tham so student la user hien tai, khong tra ve gia tri, nem IllegalArgumentException khi sai role/bi khoa.
    private void validateStudent(User student) {
        // Step 1: Chi role STUDENT moi duoc mua va ghi danh khoa hoc.
        if (student == null || student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Chi tai khoan hoc vien moi duoc thanh toan khoa hoc.");
        }

        // Step 2: Tai khoan bi khoa khong duoc tao giao dich moi.
        if (Boolean.TRUE.equals(student.getLocked())) {
            throw new IllegalArgumentException("Tai khoan cua ban dang bi khoa nen khong the thanh toan khoa hoc.");
        }
    }

    // Kiem tra form thanh toan; tham so form la CourseSignupFormDto, tra ve ma paymentMethod hop le, nem loi neu chua dong y dieu khoan/cong khong ho tro.
    private String validateSignupForm(CourseSignupFormDto form) {
        // Step 1: Yeu cau dong y dieu khoan truoc khi tao payment mo phong.
        if (form == null || !form.isAcceptedTerms()) {
            throw new IllegalArgumentException("Ban can dong y dieu khoan dich vu truoc khi thanh toan khoa hoc.");
        }

        // Step 2: Chuan hoa paymentMethod theo code gateway trong bang payment_gateways.
        String paymentMethod = form.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Ban can chon cong thanh toan truoc khi thanh toan khoa hoc.");
        }
        paymentMethod = normalizeGatewayCode(paymentMethod);

        // Step 3: Chi chap nhan gateway dang enabled trong cau hinh PaymentGateway.
        boolean enabled = paymentGatewayRepository.findByCode(paymentMethod)
                .filter(gateway -> Boolean.TRUE.equals(gateway.getEnabled()))
                .isPresent();
        if (!enabled) {
            throw new IllegalArgumentException("Cong thanh toan da chon chua duoc bat trong cau hinh he thong.");
        }
        return paymentMethod;
    }

    // Lay danh sach gateway dang bat tu PaymentGateway; khong co tham so, tra ve DTO checkout theo thu tu uu tien.
    private List<CoursePaymentGatewayDto> getEnabledPaymentGateways() {
        // Step 1: Doc gateway enabled theo routingPriority cau hinh trong DB.
        List<PaymentGateway> gateways = paymentGatewayRepository.findByEnabledTrueOrderByRoutingPriorityAscIdAsc();

        // Step 2: Map entity sang DTO gon nhe de template khong phu thuoc truc tiep vao secret/config nhay cam.
        return gateways.stream()
                .map(this::toCheckoutGateway)
                .toList();
    }

    // Chuyen PaymentGateway sang DTO hien thi checkout; tham so gateway la entity DB, tra ve CoursePaymentGatewayDto.
    private CoursePaymentGatewayDto toCheckoutGateway(PaymentGateway gateway) {
        // Step 1: Lay providerType de quyet dinh nhan ngan va mau badge.
        String providerType = normalizeGatewayCode(gateway.getProviderType());

        // Step 2: Tao DTO chi gom thong tin an toan cho user chon thanh toan.
        return new CoursePaymentGatewayDto(
                normalizeGatewayCode(gateway.getCode()),
                gateway.getDisplayName(),
                providerType,
                gateway.getDescription(),
                gatewayBadgeText(providerType),
                gatewayBadgeClass(providerType)
        );
    }

    // Tao nhan ngan cho gateway checkout; tham so providerType da chuan hoa, tra ve chuoi hien thi.
    private String gatewayBadgeText(String providerType) {
        // Step 1: Hien thi nhan theo nhom gateway pho bien.
        return switch (providerType) {
            case "vnpay" -> "VNPay";
            case "momo" -> "MoMo";
            case "stripe" -> "Card";
            case "bank_transfer" -> "Bank";
            case "paypal" -> "PayPal";
            default -> providerType;
        };
    }

    // Tao class mau cho badge gateway; tham so providerType da chuan hoa, tra ve class CSS dung trong template.
    private String gatewayBadgeClass(String providerType) {
        // Step 1: Map mau badge theo provider de user nhan dien nhanh hon.
        return switch (providerType) {
            case "vnpay" -> "gateway-vnpay";
            case "momo" -> "gateway-momo";
            case "stripe" -> "gateway-visa";
            case "bank_transfer" -> "gateway-bank";
            case "paypal" -> "gateway-paypal";
            default -> "";
        };
    }

    // Chuan hoa code gateway/provider; tham so value co the khac hoa thuong, tra ve lower-case code de so sanh DB.
    private String normalizeGatewayCode(String value) {
        // Step 1: Cat khoang trang va chuyen ve lower-case de khop voi PaymentGateway.code.
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    // Lay khoa hoc active theo courseId; tra ve Course, nem loi neu khong ton tai hoac da bi an.
    private Course getActiveCourse(Long courseId) {
        // Step 1: Tim khoa hoc trong database va chi chap nhan khoa hoc dang active.
        return courseRepository.findById(courseId)
                .filter(course -> Boolean.TRUE.equals(course.getActiveFlag()))
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay khoa hoc dang mo thanh toan."));
    }

    // Tao payment thanh cong cho student va course; paymentMethod da duoc validate tu form, tra ve Payment da luu, khong nem loi nghiep vu rieng.
    private Payment createCompletedPayment(User student, Course course, String paymentMethod) {
        // Step 1: Tao giao dich thanh toan mo phong voi trang thai COMPLETED.
        Payment payment = new Payment();
        payment.setStudentId(student.getId());
        payment.setCourseId(course.getId());
        payment.setAmount(course.getPrice());
        payment.setStatus(PaymentStatus.COMPLETED);

        // Step 2: Ghi audit theo schema hien co; gateway chi moi duoc validate o form vi bang payments chua co cot luu cong.
        payment.setCreatedBy(student.getUsername());
        payment.setUpdatedBy(student.getUsername());
        return paymentsRepository.save(payment);
    }

    // Tao enrollment ACTIVE moi cho student va course; paymentId lien ket giao dich da thanh toan, khong tra ve gia tri.
    private void createEnrollment(User student, Course course, Long paymentId) {
        // Step 1: Khoi tao enrollment tu khoa hoc, hoc vien va payment vua hoan tat.
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setPaymentId(paymentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());

        // Step 2: Luu enrollment de hoc vien co quyen truy cap khoa hoc.
        enrollment.setCreatedBy(student.getUsername());
        enrollment.setUpdatedBy(student.getUsername());
        enrollmentRepository.save(enrollment);
    }

    // Kich hoat enrollment da co; tham so enrollment/paymentId/username de gan giao dich moi, khong tra ve gia tri.
    private void activateEnrollment(Enrollment enrollment, Long paymentId, String username) {
        // Step 1: Gan lai trang thai ACTIVE va lien ket payment moi nhat.
        enrollment.setPaymentId(paymentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        if (enrollment.getEnrolledAt() == null) {
            enrollment.setEnrolledAt(LocalDateTime.now());
        }

        // Step 2: Cap nhat audit va luu thay doi enrollment.
        enrollment.setUpdatedBy(username);
        enrollmentRepository.save(enrollment);
    }

    // Chuan hoa imageUrl cua khoa hoc; tham so imageUrl co the rong, tra ve duong dan anh hop le de template render.
    private String normalizeImage(String imageUrl) {
        // Step 1: Dung anh mac dinh neu khoa hoc chua co anh.
        if (imageUrl == null || imageUrl.isBlank()) {
            return DEFAULT_COURSE_IMAGE;
        }

        // Step 2: Giu nguyen URL tuyet doi hoac duong dan da bat dau bang slash.
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("/")) {
            return imageUrl;
        }

        // Step 3: Them slash dau duong dan tuong doi de template truy cap qua static resource.
        return "/" + imageUrl;
    }
}
