package vn.edu.bkis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.bkis.model.PaymentGatewayConfigEntity;

public interface PaymentGatewayConfigRepository extends JpaRepository<PaymentGatewayConfigEntity, Long> {

    Optional<PaymentGatewayConfigEntity> findByProvider(String provider);

    boolean existsByProvider(String provider);

    List<PaymentGatewayConfigEntity> findAllByOrderByPriorityAscIdAsc();

    long countByEnabledTrue();
}
