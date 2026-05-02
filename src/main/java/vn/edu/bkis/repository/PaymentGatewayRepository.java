package vn.edu.bkis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.bkis.model.PaymentGateway;

public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {

    // Tim gateway theo ma cau hinh de thao tac qua API.
    Optional<PaymentGateway> findByCode(String code);

    // Kiem tra ma gateway da ton tai truoc khi tao moi.
    boolean existsByCode(String code);

    // Lay danh sach gateway theo thu tu dieu huong thanh toan.
    List<PaymentGateway> findAllByOrderByRoutingPriorityAscIdAsc();

    // Dem so gateway dang bat de hien thi summary.
    long countByEnabledTrue();
}
