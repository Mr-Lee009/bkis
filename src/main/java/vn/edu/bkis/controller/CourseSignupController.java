package vn.edu.bkis.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.bkis.dto.CourseSignupFormDto;
import vn.edu.bkis.dto.CourseSignupPageDto;
import vn.edu.bkis.model.User;
import vn.edu.bkis.security.CustomOAuth2User;
import vn.edu.bkis.security.CustomUserDetails;
import vn.edu.bkis.service.CourseSignupService;

@Controller
public class CourseSignupController {
    private final CourseSignupService courseSignupService;

    // Khoi tao controller xu ly trang thanh toan khoa hoc; tham so service dung de lay du lieu checkout va tao payment, khong tra ve gia tri.
    public CourseSignupController(CourseSignupService courseSignupService) {
        this.courseSignupService = courseSignupService;
    }

    // Hien thi trang thanh toan cho courseId va user hien tai; tra ve template 06-course-signup, nem loi neu principal khong hop le.
    @GetMapping("/courses/{courseId}/signup")
    public String signupPage(@PathVariable Long courseId, Authentication authentication, Model model) {
        // Step 1: Lay user dang dang nhap de service kiem tra quyen thanh toan.
        User currentUser = getCurrentUser(authentication);

        // Step 2: Nap du lieu khoa hoc, hoc vien va tieu de checkout vao model.
        model.addAttribute("pageTitle", "Thanh toán khóa học");
        CourseSignupPageDto signupPage = courseSignupService.getSignupPage(courseId, currentUser);
        model.addAttribute("signupPage", signupPage);

        // Step 3: Tao form mac dinh voi gateway uu tien dau tien tu PaymentGateway neu chua co flash form tu lan submit loi.
        if (!model.containsAttribute("signupForm")) {
            CourseSignupFormDto signupForm = new CourseSignupFormDto();
            signupPage.getPaymentGateways().stream()
                    .findFirst()
                    .ifPresent(gateway -> signupForm.setPaymentMethod(gateway.code()));
            signupForm.setAcceptedTerms(true);
            model.addAttribute("signupForm", signupForm);
        }
        return "06-course-signup";
    }

    // Nhan yeu cau thanh toan cho courseId; form gom paymentMethod va dieu khoan, tra ve redirect, nem loi nghiep vu qua flash message.
    @PostMapping("/courses/{courseId}/signup")
    public String signup(@PathVariable Long courseId,
                         @ModelAttribute("signupForm") CourseSignupFormDto form,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            // Step 1: Goi service tao payment COMPLETED va kich hoat enrollment trong cung transaction.
            courseSignupService.signup(courseId, getCurrentUser(authentication), form);

            // Step 2: Bao thanh cong va dua user ve trang chi tiet khoa hoc.
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thanh toán khóa học thành công. Bạn đã có quyền học khóa học này."
            );
            return "redirect:/courses/" + courseId;
        } catch (IllegalArgumentException ex) {
            // Step 3: Giu lai form va thong bao loi de user chon lai cong thanh toan neu can.
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("signupForm", form);
            return "redirect:/courses/" + courseId + "/signup";
        }
    }

    // Lay user local tu Authentication; tham so authentication co the den tu login thuong hoac SSO, tra ve User, nem loi neu chua dang nhap hop le.
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
