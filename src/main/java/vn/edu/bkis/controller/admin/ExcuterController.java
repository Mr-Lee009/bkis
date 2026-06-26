package vn.edu.bkis.controller.admin;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.bkis.dto.ApiResponse;
import vn.edu.bkis.service.admin.AsyncTransactionExecutor;

@RestController("/api/admin/payment-gateways")
@AllArgsConstructor
public class ExcuterController {

    private final AsyncTransactionExecutor asyncTransactionExecutor;

    @GetMapping("/insert_one_million")
    public ApiResponse<String> insertOneMillion() {
        asyncTransactionExecutor.insertOneMillionAsync();
        return ApiResponse.success("The process of inserting 1 million records has been initiated and is running in the background.");
    }

}
