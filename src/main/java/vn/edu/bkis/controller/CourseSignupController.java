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

/**
 * Controller xu ly man hinh dang ky va thanh toan khoa hoc cho hoc vien.
 */
@Controller
public class CourseSignupController {
    private final CourseSignupService courseSignupService;

    /**
     * Khoi tao controller dang ky khoa hoc.
     *
     * @param courseSignupService service xu ly du lieu checkout va dang ky khoa hoc
     * @return khong tra ve gia tri vi day la constructor cua controller
     */
    public CourseSignupController(CourseSignupService courseSignupService) {
        this.courseSignupService = courseSignupService;
    }

    /**
     * Hien thi trang thanh toan cho khoa hoc duoc chon.
     *
     * @param courseId id khoa hoc can dang ky
     * @param authentication thong tin dang nhap hien tai tu Spring Security
     * @param model model dung de dua du lieu checkout ra giao dien
     * @return ten template trang dang ky khoa hoc
     * @throws IllegalArgumentException neu khong lay duoc hoc vien hop le tu phien dang nhap
     */
    @GetMapping("/courses/{courseId}/signup")
    public String signupPage(@PathVariable Long courseId, Authentication authentication, Model model) {
        // Step 1: Lay user dang dang nhap de service kiem tra quyen thanh toan.
        User currentUser = getCurrentUser(authentication);

        // Step 2: Nap du lieu khoa hoc, hoc vien va tieu de checkout vao model.
        model.addAttribute("pageTitle", "Thanh toán khóa học");
        CourseSignupPageDto signupPage = courseSignupService.getSignupPage(courseId, currentUser);
        model.addAttribute("signupPage", signupPage);

        // Step 3: Tao form mac dinh voi gateway uu tien dau tien tu payment_gateway_config neu chua co flash form tu lan submit loi.
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

    /**
     * Nhan yeu cau dang ky khoa hoc va xu ly thanh toan mo phong.
     *
     * @param courseId id khoa hoc dang duoc mua
     * @param form du lieu form dang ky gom cong thanh toan va dieu khoan
     * @param authentication thong tin dang nhap hien tai tu Spring Security
     * @param redirectAttributes doi tuong chua flash message sau khi redirect
     * @return duong dan redirect sau khi xu ly thanh cong hoac that bai
     */
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

    /**
     * Lay user local tu Authentication cua Spring Security.
     *
     * @param authentication thong tin dang nhap hien tai
     * @return user local cua he thong duoc gan voi phien dang nhap
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
