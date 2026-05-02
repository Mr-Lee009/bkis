package vn.edu.bkis.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/payment-gateways")
public class AdminPaymentGatewayController {

    // Hien thi page cau hinh cong thanh toan, du lieu chi tiet se nap bang API.
    @GetMapping({"", "/"})
    public String paymentGatewayPage(Model model) {
        model.addAttribute("pageTitle", "Payment Gateways");
        return "admin/ad-05-payment-gateways";
    }
}
