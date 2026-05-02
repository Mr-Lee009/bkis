package vn.edu.bkis.controller.admin;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.bkis.dto.ApiResponse;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayPageDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayTestResultDto;
import vn.edu.bkis.dto.admin.payment.PaymentGatewayUpsertRequest;
import vn.edu.bkis.service.admin.AdminPaymentGatewayService;

@RestController
@RequestMapping("/api/admin/payment-gateways")
public class AdminPaymentGatewayRestController {
    private final AdminPaymentGatewayService adminPaymentGatewayService;

    // Khoi tao REST controller cho cac API cau hinh cong thanh toan.
    public AdminPaymentGatewayRestController(AdminPaymentGatewayService adminPaymentGatewayService) {
        this.adminPaymentGatewayService = adminPaymentGatewayService;
    }

    // Lay du lieu tong hop cua man hinh cau hinh gateway.
    @GetMapping
    public ApiResponse<PaymentGatewayPageDto> getPaymentGatewayPage() {
        return ApiResponse.success(adminPaymentGatewayService.getPage());
    }

    // Lay chi tiet mot gateway theo code.
    @GetMapping("/{code}")
    public ApiResponse<PaymentGatewayDto> getGateway(@PathVariable String code) {
        return ApiResponse.success(adminPaymentGatewayService.getGateway(code));
    }

    // Tao moi gateway tu popup tren UI.
    @PostMapping
    public ApiResponse<PaymentGatewayDto> createGateway(@RequestBody PaymentGatewayUpsertRequest request) {
        return ApiResponse.success(adminPaymentGatewayService.createGateway(request));
    }

    // Cap nhat cau hinh gateway dang chon.
    @PutMapping("/{code}")
    public ApiResponse<PaymentGatewayDto> updateGateway(@PathVariable String code,
                                                        @RequestBody PaymentGatewayUpsertRequest request) {
        return ApiResponse.success(adminPaymentGatewayService.updateGateway(code, request));
    }

    // Test mock ket noi mot gateway.
    @PostMapping("/{code}/test")
    public ApiResponse<PaymentGatewayTestResultDto> testGateway(@PathVariable String code) {
        return ApiResponse.success(adminPaymentGatewayService.testGateway(code));
    }

    // Test mock ket noi tat ca gateway.
    @PostMapping("/test-all")
    public ApiResponse<List<PaymentGatewayTestResultDto>> testAllGateways() {
        return ApiResponse.success(adminPaymentGatewayService.testAllGateways());
    }

    // Chuyen loi validate thanh response 400 cho AJAX.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
