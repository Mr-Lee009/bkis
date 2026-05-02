package vn.edu.bkis.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.bkis.model.User;
import vn.edu.bkis.security.CustomOAuth2User;
import vn.edu.bkis.security.CustomUserDetails;
import vn.edu.bkis.service.MyCoursesService;

@Controller
public class MyCoursesController {
    private final MyCoursesService myCoursesService;

    // Khởi tạo controller hiển thị danh sách khóa học đã đăng ký của học viên hiện tại.
    public MyCoursesController(MyCoursesService myCoursesService) {
        this.myCoursesService = myCoursesService;
    }

    // Hiển thị các khóa học mà user hiện tại đã đăng ký, mỗi khóa học link về trang detail.
    @GetMapping("/my-courses")
    public String myCourses(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        model.addAttribute("pageTitle", "Khóa học của bạn");
        model.addAttribute("myCourses", myCoursesService.getMyCourses(currentUser.getId()));
        return "07-my-courses";
    }

    // Lấy user local từ principal để hỗ trợ cả đăng nhập thường và SSO.
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUser();
        }
        throw new IllegalArgumentException("Bạn cần đăng nhập để xem khóa học của bạn.");
    }
}
