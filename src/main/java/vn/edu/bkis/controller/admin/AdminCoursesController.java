package vn.edu.bkis.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.bkis.dto.admin.AdminCourseDetailDto;
import vn.edu.bkis.dto.admin.AdminCourseFilterDto;
import vn.edu.bkis.dto.admin.AdminCourseModuleFormDto;
import vn.edu.bkis.dto.admin.AdminCourseUpdateFormDto;
import vn.edu.bkis.dto.admin.AdminCourseVideoFormDto;
import vn.edu.bkis.service.admin.AdminCourseManagementService;

/**
 * MVC controller for admin course management pages.
 */
@Controller
@RequestMapping("/admin/courses")
public class AdminCoursesController {
    private final AdminCourseManagementService adminCourseManagementService;

    public AdminCoursesController(AdminCourseManagementService adminCourseManagementService) {
        this.adminCourseManagementService = adminCourseManagementService;
    }

    /**
     * Render the admin courses list.
     *
     * @param filter requested filters
     * @param model MVC model
     * @return courses list template
     */
    @GetMapping({"", "/"})
    public String courses(@ModelAttribute("courseFilter") AdminCourseFilterDto filter, Model model) {
        model.addAttribute("pageTitle", "Courses");
        model.addAttribute("coursePage", adminCourseManagementService.getCourseListPage(filter));
        return "admin/ad-04-courses";
    }

    /**
     * Render one course detail.
     *
     * @param courseId requested course id
     * @param model MVC model
     * @return course detail template
     */
    @GetMapping("/{courseId}")
    public String courseDetail(@PathVariable Long courseId, Model model) {
        AdminCourseDetailDto course = adminCourseManagementService.getCourseDetail(courseId);
        model.addAttribute("pageTitle", "Courses");
        model.addAttribute("course", course);
        model.addAttribute("teacherOptions", adminCourseManagementService.getTeacherOptions());
        if (!model.containsAttribute("courseUpdateForm")) {
            model.addAttribute("courseUpdateForm", adminCourseManagementService.toUpdateForm(course));
        }
        return "admin/ad-04-course-detail";
    }

    /**
     * Update a course from the detail form.
     *
     * @param courseId requested course id
     * @param form submitted form
     * @param redirectAttributes flash attributes
     * @return redirect to detail page
     */
    @PostMapping("/{courseId}/update")
    public String updateCourse(@PathVariable Long courseId,
                               @ModelAttribute("courseUpdateForm") AdminCourseUpdateFormDto form,
                               RedirectAttributes redirectAttributes) {
        try {
            form.setId(courseId);
            adminCourseManagementService.updateCourse(form);
            redirectAttributes.addFlashAttribute("successMessage", "Course updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("courseUpdateForm", form);
        }
        return "redirect:/admin/courses/" + courseId;
    }

    /**
     * Delete or archive a course.
     *
     * @param courseId requested course id
     * @param redirectAttributes flash attributes
     * @return redirect to courses list or detail
     */
    @PostMapping("/{courseId}/delete")
    public String deleteCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = adminCourseManagementService.deleteOrArchiveCourse(courseId);
            redirectAttributes.addFlashAttribute(
                "successMessage",
                deleted ? "Course deleted successfully." : "Course has enrollments or payments, so it was hidden instead."
            );
            return "redirect:/admin/courses/";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/courses/" + courseId;
        }
    }

    @PostMapping("/{courseId}/modules")
    public String createModule(@PathVariable Long courseId,
                               @ModelAttribute AdminCourseModuleFormDto form,
                               RedirectAttributes redirectAttributes) {
        try {
            adminCourseManagementService.createModule(courseId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Module created successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/modules/{moduleId}/update")
    public String updateModule(@PathVariable Long courseId,
                               @PathVariable Long moduleId,
                               @ModelAttribute AdminCourseModuleFormDto form,
                               RedirectAttributes redirectAttributes) {
        try {
            adminCourseManagementService.updateModule(courseId, moduleId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Module updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/modules/{moduleId}/delete")
    public String deleteModule(@PathVariable Long courseId,
                               @PathVariable Long moduleId,
                               RedirectAttributes redirectAttributes) {
        try {
            adminCourseManagementService.deleteModule(courseId, moduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Module deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/modules/{moduleId}/videos")
    public String createVideo(@PathVariable Long courseId,
                              @PathVariable Long moduleId,
                              @ModelAttribute AdminCourseVideoFormDto form,
                              RedirectAttributes redirectAttributes) {
        try {
            adminCourseManagementService.createVideo(courseId, moduleId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Video created successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/modules/{moduleId}/videos/{videoId}/update")
    public String updateVideo(@PathVariable Long courseId,
                              @PathVariable Long moduleId,
                              @PathVariable Long videoId,
                              @ModelAttribute AdminCourseVideoFormDto form,
                              RedirectAttributes redirectAttributes) {
        try {
            adminCourseManagementService.updateVideo(courseId, moduleId, videoId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Video updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/modules/{moduleId}/videos/{videoId}/delete")
    public String deleteVideo(@PathVariable Long courseId,
                              @PathVariable Long moduleId,
                              @PathVariable Long videoId,
                              RedirectAttributes redirectAttributes) {
        try {
            adminCourseManagementService.deleteVideo(courseId, moduleId, videoId);
            redirectAttributes.addFlashAttribute("successMessage", "Video deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }
}
