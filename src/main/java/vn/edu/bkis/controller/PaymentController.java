package vn.edu.bkis.controller;

import org.springframework.web.bind.annotation.*;
import vn.edu.bkis.dto.payment.CreatePaymentRequestDto;
import vn.edu.bkis.dto.payment.ResponseCreatePaymentDto;
import vn.edu.bkis.service.PaymentService;


@RestController
@RequestMapping("api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{provider}/create")
    public ResponseCreatePaymentDto createPayment(
        CreatePaymentRequestDto request, @PathVariable("provider") String provider) {
        return paymentService.createPayment(request, provider);
    }

    @PostMapping("/{provider}/call-back")
    public String callbackURL() {
        return "/payment";
    }

    @GetMapping("/{provider}/call-back")
    public String callbackURL(String queryParams) {
        return "/payment";
    }
}
