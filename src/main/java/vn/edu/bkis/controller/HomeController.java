package vn.edu.bkis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.edu.bkis.service.HomeService;

/**
 * HomeController handle requests for the home page of the application.
 */
@Controller
public class HomeController {

    // Inject HomeService to fetch featured courses for the home page
    private final HomeService homeService;

    // Constructor for HomeController, allowing Spring to inject the HomeService dependency
    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * Handle GET requests to the root URL ("/") and return the home page view.
     *
     * @param model the Model object to pass data to the view
     * @return the name of the view template for the home page
     * This method adds a welcome message, page title, and a list of featured courses to the model,
     * which will be rendered in the "03-home" view template.
     * The featured courses are retrieved from the HomeService, allowing dynamic content to be displayed on the home page.
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
