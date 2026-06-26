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

/**
 * REST controller quản lý API cấu hình cổng thanh toán cho màn hình admin.
 *
 * <p>Controller này nhận request từ giao diện quản trị, gọi service để lấy dữ liệu
 * hoặc cập nhật cấu hình, sau đó trả về phản hồi dạng JSON cho AJAX.</p>
 */
@RestController
@RequestMapping("/api/admin/payment-gateways")
public class AdminPaymentGatewayRestController {
    private final AdminPaymentGatewayService adminPaymentGatewayService;

    /**
     * Khởi tạo REST controller với service xử lý nghiệp vụ cổng thanh toán.
     *
     * @param adminPaymentGatewayService service xử lý đọc, ghi và kiểm tra gateway
     */
    public AdminPaymentGatewayRestController(AdminPaymentGatewayService adminPaymentGatewayService) {
        this.adminPaymentGatewayService = adminPaymentGatewayService;
    }

    /**
     * Lấy dữ liệu tổng hợp của màn hình cấu hình cổng thanh toán.
     *
     * @return {@link ApiResponse} chứa {@link PaymentGatewayPageDto} để render màn hình admin
     */
    @GetMapping
    public ApiResponse<PaymentGatewayPageDto> getPaymentGatewayPage() {
        return ApiResponse.success(adminPaymentGatewayService.getPage());
    }

    /**
     * Lấy chi tiết một cổng thanh toán theo mã định danh.
     *
     * @param code mã gateway cần truy vấn
     * @return {@link ApiResponse} chứa {@link PaymentGatewayDto} của gateway tương ứng
     */
    @GetMapping("/{code}")
    public ApiResponse<PaymentGatewayDto> getGateway(@PathVariable String code) {
        return ApiResponse.success(adminPaymentGatewayService.getGateway(code));
    }

    /**
     * Tạo mới một cổng thanh toán từ dữ liệu submit trên giao diện admin.
     *
     * @param request dữ liệu tạo mới gateway lấy từ body của request
     * @return {@link ApiResponse} chứa {@link PaymentGatewayDto} của gateway vừa tạo
     */
    @PostMapping
    public ApiResponse<PaymentGatewayDto> createGateway(@RequestBody PaymentGatewayUpsertRequest request) {
        return ApiResponse.success(adminPaymentGatewayService.createGateway(request));
    }

    /**
     * Cập nhật cấu hình của một cổng thanh toán theo mã gateway.
     *
     * @param code mã gateway cần cập nhật
     * @param request dữ liệu cập nhật lấy từ body của request
     * @return {@link ApiResponse} chứa {@link PaymentGatewayDto} sau khi cập nhật
     */
    @PutMapping("/{code}")
    public ApiResponse<PaymentGatewayDto> updateGateway(@PathVariable String code,
                                                        @RequestBody PaymentGatewayUpsertRequest request) {
        return ApiResponse.success(adminPaymentGatewayService.updateGateway(code, request));
    }

    /**
     * Kiểm tra kết nối giả lập của một cổng thanh toán theo mã gateway.
     *
     * @param code mã gateway cần test
     * @return {@link ApiResponse} chứa {@link PaymentGatewayTestResultDto} của lần kiểm tra
     */
    @PostMapping("/{code}/test")
    public ApiResponse<PaymentGatewayTestResultDto> testGateway(@PathVariable String code) {
        return ApiResponse.success(adminPaymentGatewayService.testGateway(code));
    }

    /**
     * Kiểm tra kết nối giả lập cho tất cả cổng thanh toán đang cấu hình.
     *
     * @return {@link ApiResponse} chứa danh sách {@link PaymentGatewayTestResultDto}
     */
    @PostMapping("/test-all")
    public ApiResponse<List<PaymentGatewayTestResultDto>> testAllGateways() {
        return ApiResponse.success(adminPaymentGatewayService.testAllGateways());
    }

    /**
     * Chuyển lỗi validate thành response 400 để phía AJAX hiển thị đúng thông báo.
     *
     * @param ex lỗi validate phát sinh khi xử lý request
     * @return response 400 kèm nội dung lỗi để client hiển thị
     * @throws IllegalArgumentException không ném tiếp ra ngoài vì đã được bắt và đổi sang HTTP 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
