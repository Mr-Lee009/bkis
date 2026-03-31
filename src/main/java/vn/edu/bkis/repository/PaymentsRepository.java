package vn.edu.bkis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.bkis.model.Payment;

import java.math.BigDecimal;

public interface PaymentsRepository extends JpaRepository<Payment,Long> {

  @Query(value =  "SELECT IFNULL(SUM(p.amount), 0) AS total_amount"
                + " FROM payments p"
                + " WHERE p.status = 'COMPLETED'"
                + "   AND p.created_at BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())",nativeQuery = true)
  BigDecimal getMonthlyRevenue();

  @Query(value =  "SELECT IFNULL(SUM(p.amount), 0) AS total_amount " 
      + " FROM payments p "
      + " WHERE p.status = 'COMPLETED' "
      + "   AND (p.created_at BETWEEN DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01') "
      + "     AND LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)))",nativeQuery = true)
  BigDecimal getRevenueLastMonth();
}
