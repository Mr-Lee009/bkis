package vn.edu.bkis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.edu.bkis.service.HomeService;

/**
 * Controller handling the home page requests.
 * Provides featured courses and category data for the landing page.
 */
@Controller
public class HomeController {

    private final HomeService homeService;

    /**
     * Constructor for dependency injection of HomeService.
     * @param homeService service for fetching home page course data
     */
    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * Display the home page with featured courses.
     * Retrieves top active courses and courses filtered by tag for display.
     *
     * @param model the Spring MVC model to bind data for the view
     * @return the name of the Thymeleaf template (03-home)
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Chào mừng đến BKIS MVC!");
        model.addAttribute("pageTitle", "Courses");
        model.addAttribute("featuredCourses", homeService.getFeaturedCourses());
        model.addAttribute("javaCourses", homeService.getFeaturedCoursesByTag("#java"));
        return "03-home";
    }
}
