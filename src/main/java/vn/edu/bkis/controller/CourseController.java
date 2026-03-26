package vn.edu.bkis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.bkis.dto.CourseDetailPageDto;
import vn.edu.bkis.service.CourseDetailService;

@Controller
public class CourseController {

    private final CourseDetailService courseDetailService;

    public CourseController(CourseDetailService courseDetailService) {
        this.courseDetailService = courseDetailService;
    }

    /**
     * Course detail page.
     * @param id the ID of the course.
     * @param model the model to be used when displaying the page.
     * @return the name of the Thymeleaf template for the course detail page.
     */
    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable("id") Long id, Model model) {
        CourseDetailPageDto courseDetail = courseDetailService.getCourseDetail(id);
        model.addAttribute("pageTitle", courseDetail.title());
        model.addAttribute("course", courseDetail);
        return "04-course-detail";
    }
}
