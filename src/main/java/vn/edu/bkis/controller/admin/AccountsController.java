package vn.edu.bkis.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.bkis.dto.admin.AccountFilterDto;
import vn.edu.bkis.dto.admin.AccountFormDto;
import vn.edu.bkis.dto.admin.AccountUpdateFormDto;
import vn.edu.bkis.service.admin.AccountManagementService;

@Controller
@RequestMapping("/admin/accounts")
public class AccountsController {
  private final AccountManagementService accountManagementService;

  /**
   * Create the controller with the account management service.
   *
   * @param accountManagementService the service behind the admin accounts page
   */
  public AccountsController(AccountManagementService accountManagementService) {
    this.accountManagementService = accountManagementService;
  }

  /**
   * Render the admin account management page with filters and account data.
   *
   * @param filter the requested list filters
   * @param model the MVC model for the view
   * @return the account management template
   */
  @GetMapping("/")
  public String accounts(@ModelAttribute("accountFilter") AccountFilterDto filter, Model model) {
    model.addAttribute("pageTitle", "Accounts");
    model.addAttribute("accountPage", accountManagementService.getAccountManagementPage(filter));
    if (!model.containsAttribute("accountForm")) {
      model.addAttribute("accountForm", new AccountFormDto());
    }
    if (!model.containsAttribute("accountUpdateForm")) {
      model.addAttribute("accountUpdateForm", new AccountUpdateFormDto());
    }
    return "admin/ad-02-accounts";
  }

  /**
   * Create a new account from the admin account form.
   *
   * @param accountForm the submitted create-account form
   * @param redirectAttributes the redirect attributes for flash messages
   * @return redirect to the account management page
   */
  @PostMapping("/")
  public String createAccount(@ModelAttribute("accountForm") AccountFormDto accountForm,
                              RedirectAttributes redirectAttributes) {
    try {
      accountManagementService.createAccount(accountForm);
      redirectAttributes.addFlashAttribute("successMessage", "Account created successfully.");
    } catch (IllegalArgumentException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
      redirectAttributes.addFlashAttribute("accountForm", accountForm);
    }
    return "redirect:/admin/accounts/";
  }

  /**
   * Update an existing account from the admin modal form.
   *
   * @param accountUpdateForm the submitted update form
   * @param redirectAttributes the redirect attributes for flash messages
   * @return redirect to the account management page
   */
  @PostMapping("/update")
  public String updateAccount(@ModelAttribute("accountUpdateForm") AccountUpdateFormDto accountUpdateForm,
                              RedirectAttributes redirectAttributes) {
    try {
      accountManagementService.updateAccount(accountUpdateForm);
      redirectAttributes.addFlashAttribute("successMessage", "Account updated successfully.");
    } catch (IllegalArgumentException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    }
    return "redirect:/admin/accounts/";
  }
}
