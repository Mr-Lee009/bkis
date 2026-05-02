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
import vn.edu.bkis.model.User;
import vn.edu.bkis.security.CustomOAuth2User;
import vn.edu.bkis.security.CustomUserDetails;
import vn.edu.bkis.service.CourseSignupService;

@Controller
public class CourseSignupController {
    private final CourseSignupService courseSignupService;

    // Khởi tạo controller xử lý màn hình đăng ký khóa học cho học viên.
    public CourseSignupController(CourseSignupService courseSignupService) {
        this.courseSignupService = courseSignupService;
    }

    // Hiển thị form đăng ký khóa học sau khi Spring Security xác nhận user đã đăng nhập.
    @GetMapping("/courses/{courseId}/signup")
    public String signupPage(@PathVariable Long courseId, Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        model.addAttribute("pageTitle", "Đăng ký khóa học");
        model.addAttribute("signupPage", courseSignupService.getSignupPage(courseId, currentUser));
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new CourseSignupFormDto());
        }
        return "06-course-signup";
    }

    // Nhận form đăng ký và tạo enrollment ACTIVE cho học viên hiện tại.
    @PostMapping("/courses/{courseId}/signup")
    public String signup(@PathVariable Long courseId,
                         @ModelAttribute("signupForm") CourseSignupFormDto form,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            courseSignupService.signup(courseId, getCurrentUser(authentication), form);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký khóa học thành công. Bạn đã có quyền học khóa này.");
            return "redirect:/courses/" + courseId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("signupForm", form);
            return "redirect:/courses/" + courseId + "/signup";
        }
    }

    // Lấy user local từ principal để dùng chung cho đăng nhập thường và đăng nhập SSO.
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUser();
        }
        throw new IllegalArgumentException("Bạn cần đăng nhập bằng tài khoản học viên để đăng ký khóa học.");
    }
}
