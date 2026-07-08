package vn.edu.bkis.service.gateway;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import vn.edu.bkis.dto.payment.PaymentProvider;

@Component
public class PaymentGatewayResolver {

    private final List<PaymentGateway> gateways;

    /**
     * Khoi tao resolver voi danh sach PaymentGateway bean dang co trong Spring context.
     *
     * @param gateways cac implementation adapter cua gateway
     */
    public PaymentGatewayResolver(List<PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    /**
     * Tim adapter gateway phu hop theo provider.
     *
     * @param provider ma cong thanh toan
     * @return adapter hop le neu he thong da dang ky implementation
     */
    public Optional<PaymentGateway> resolve(String provider) {
        // Step 1: chuan hoa provider dau vao ve enum contract chung.
        PaymentProvider normalized = normalize(provider);
        if (normalized == null) {
            return Optional.empty();
        }

        // Step 2: chon implementation tu danh sach bean dang co trong Spring context.
        return gateways.stream()
            .filter(gateway -> normalized.equals(gateway.provider()))
            .findFirst();
    }

    /**
     * Chuan hoa provider string ve enum contract chung.
     *
     * @param provider provider can parse
     * @return enum provider neu khop, nguoc lai tra ve null
     */
    private PaymentProvider normalize(String provider) {
        // Step 1: map chuoi provider ve enum de resolver khong phu thuoc string literal.
        if (provider == null) {
            return null;
        }
        return switch (provider.trim().toLowerCase()) {
            case "momo" -> PaymentProvider.MOMO;
            case "vnpay", "vn_pay" -> PaymentProvider.VN_PAY;
            case "zalo_pay", "zalopay" -> PaymentProvider.ZALO_PAY;
            default -> null;
        };
    }
}