package vn.edu.bkis.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.bkis.model.PaymentTransactionEntity;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    /**
     * Tim giao dich theo ma payment noi bo.
     *
     * @param paymentCode ma giao dich can tra cuu
     * @return giao dich neu ton tai
     */
    Optional<PaymentTransactionEntity> findByPaymentCode(String paymentCode);

    List<PaymentTransactionEntity> findByProviderIn(Collection<String> providers);

    List<PaymentTransactionEntity> findTop10ByOrderByUpdatedAtDesc();

    long countByStatusIn(Collection<String> statuses);

    @Query(value = """
        SELECT COALESCE(SUM(pt.amount), 0)
        FROM payment_transaction pt
        WHERE pt.status = 'COMPLETED'
          AND pt.created_at BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())
        """, nativeQuery = true)
    BigDecimal getMonthlyRevenue();

    @Query(value = """
        SELECT COALESCE(SUM(pt.amount), 0)
        FROM payment_transaction pt
        WHERE pt.status = 'COMPLETED'
          AND pt.created_at BETWEEN DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01')
          AND LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))
        """, nativeQuery = true)
    BigDecimal getRevenueLastMonth();
}
