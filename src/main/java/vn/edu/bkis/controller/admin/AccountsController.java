package vn.edu.bkis.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/accounts")
public class AccountsController {

  @GetMapping("/")
  public String accounts() {
    return "admin/ad-02-accounts";
  }
}
