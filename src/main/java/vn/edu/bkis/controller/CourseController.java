package vn.edu.bkis.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.bkis.dto.CourseDetailPageDto;
import vn.edu.bkis.model.User;
import vn.edu.bkis.security.CustomOAuth2User;
import vn.edu.bkis.security.CustomUserDetails;
import vn.edu.bkis.service.CourseDetailService;

/**
 * REST controller for handling course-related requests.
 * Provides endpoints to display course details and related course information.
 */
@Controller
public class CourseController {

    private final CourseDetailService courseDetailService;

    /**
     * Constructor for dependency injection of CourseDetailService.
     * @param courseDetailService service for fetching course details
     */
    public CourseController(CourseDetailService courseDetailService) {
        this.courseDetailService = courseDetailService;
    }

    /**
     * Displays the course detail page for a specific04-course-detail course.
     * Fetches complete course information including lessons, reviews, and related courses.
     *
     * @param id the unique identifier of the course to display
     * @param model the Spring MVC model to bind data for the view
     * @return the name of the Thymeleaf template (04-course-detail)
     * @throws IllegalArgumentException if course is not found or inactive
     */
    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable("id") Long id, Authentication authentication, Model model) {
        CourseDetailPageDto courseDetail = courseDetailService.getCourseDetail(id, getCurrentUser(authentication));
        model.addAttribute("pageTitle", courseDetail.title());
        model.addAttribute("course", courseDetail);
        return "04-course-detail";
    }

    // Lấy user local từ principal nếu request đã đăng nhập, anonymous thì trả null để vẫn xem được trang public.
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUser();
        }
        return null;
    }
}
