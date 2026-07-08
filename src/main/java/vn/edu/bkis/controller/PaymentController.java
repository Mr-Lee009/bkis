package vn.edu.bkis.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.bkis.dto.CourseSignupFormDto;
import vn.edu.bkis.dto.payment.PaymentDetailResponseDto;
import vn.edu.bkis.dto.payment.ResponseCreatePaymentDto;
import vn.edu.bkis.model.User;
import vn.edu.bkis.security.CustomOAuth2User;
import vn.edu.bkis.security.CustomUserDetails;
import vn.edu.bkis.service.PaymentService;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Khoi tao PaymentController voi PaymentService xu ly toan bo payment flow.
     *
     * @param paymentService service xu ly tao giao dich, callback va tra cuu trang thai
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Tao giao dich moi cho mot khoa hoc tu form checkout.
     *
     * @param courseId id khoa hoc can thanh toan
     * @param request du lieu form checkout tu frontend
     * @param authentication thong tin dang nhap hien tai
     * @param redirectAttributes doi tuong chua flash message khi can quay lai trang checkout
     * @return redirect den paymentUrl neu co; nguoc lai quay ve trang khoa hoc
     */
    @PostMapping("/courses/{courseId}")
    public String createPayment(@PathVariable Long courseId,
        @ModelAttribute("signupForm") CourseSignupFormDto request,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {
        try {
            // Step 1: Lay user dang dang nhap va chuyen toan bo request qua PaymentService.
            User currentUser = getCurrentUser(authentication);
            ResponseCreatePaymentDto response =
                paymentService.createCoursePayment(courseId, currentUser, request);

            // Step 2: Neu co paymentUrl thi redirect sang provider, neu chua co thi quay ve trang khoa hoc.
            if (response.getPaymentUrl() != null && !response.getPaymentUrl().isBlank()) {
                return "redirect:" + response.getPaymentUrl();
            }
            redirectAttributes.addFlashAttribute("successMessage",
                "Đã tạo giao dịch thanh toán. Vui lòng theo dõi trạng thái mới nhất.");
            return "redirect:/courses/" + courseId;
        } catch (IllegalArgumentException ex) {
            // Step 3: Giu lai loi va quay ve trang checkout de user thao tac lai.
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("signupForm", request);
            return "redirect:/courses/" + courseId + "/signup";
        }
    }

    /**
     * Query trang thai giao dich theo payment code.
     *
     * @param paymentCode ma giao dich noi bo
     * @return thong tin trang thai giao dich da chuan hoa
     */
    @GetMapping("/{paymentCode}")
    public ResponseEntity<PaymentDetailResponseDto> getPaymentStatus(@PathVariable String paymentCode) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(paymentCode));
    }

    /**
     * Callback POST tu provider quay ve payment controller.
     *
     * @param provider ma cong thanh toan
     * @param queryParams toan bo tham so callback tu provider
     * @return ket qua xu ly callback da chuan hoa
     */
    @PostMapping("/{provider}/call-back")
    public ResponseEntity<PaymentDetailResponseDto> callbackURL(@PathVariable String provider,
        @RequestParam Map<String, String> queryParams) {
        return ResponseEntity.ok(paymentService.handleCallback(provider, queryParams));
    }

    /**
     * Lay user local tu Authentication cua Spring Security.
     *
     * @param authentication thong tin dang nhap hien tai
     * @return user local cua he thong
     * @throws IllegalArgumentException neu principal khong phai tai khoan hop le de thanh toan
     */
    private User getCurrentUser(Authentication authentication) {
        // Step 1: Lay principal hien tai tu Spring Security.
        Object principal = authentication == null ? null : authentication.getPrincipal();

        // Step 2: Ho tro tai khoan dang nhap bang username/password.
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        // Step 3: Ho tro tai khoan dang nhap bang OAuth2.
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUser();
        }
        throw new IllegalArgumentException("Bạn cần đăng nhập bằng tài khoản học viên để thanh toán khóa học.");
    }
}
