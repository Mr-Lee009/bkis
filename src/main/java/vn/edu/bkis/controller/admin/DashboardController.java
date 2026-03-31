package vn.edu.bkis.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.bkis.service.admin.DashboardService;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  /**
   * Constructor
   * @param dashboardService dashboard service.
   */
  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/")
  public String dashboard(Model model) {
    model.addAttribute("infoDto", dashboardService.getDashboardInfo());
    return "admin/ad-01-dashboard";
  }
}
