package vn.edu.bkis.service.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.dto.DashboardInfoDto;
import vn.edu.bkis.dto.NewestStudentDto;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.EnrollmentRepository;
import vn.edu.bkis.repository.PaymentTransactionRepository;
import vn.edu.bkis.repository.UserRepository;

@Service
public class DashboardService {
  private final UserRepository userRepo;
  private final CourseRepository courseRepo;
  private final PaymentTransactionRepository paymentTransactionRepo;
  private final EnrollmentRepository enrollmentRepo;

  /**
   * Khoi tao service dashboard voi cac repository can dung.
   *
   * @param userRepo repository nguoi dung
   * @param courseRepo repository khoa hoc
   * @param paymentTransactionRepo repository giao dich thanh toan moi
   * @param enrollmentRepo repository ghi danh
   * @return khong tra ve gia tri vi day la constructor cua service
   */
  public DashboardService(UserRepository userRepo, CourseRepository courseRepo,
      PaymentTransactionRepository paymentTransactionRepo, EnrollmentRepository enrollmentRepo) {
    this.userRepo = userRepo;
    this.courseRepo = courseRepo;
    this.paymentTransactionRepo = paymentTransactionRepo;
    this.enrollmentRepo = enrollmentRepo;
  }

  /**
   * Lay thong tin tong hop cho dashboard admin.
   *
   * @return doi tuong tong hop so lieu hoc vien, khoa hoc va doanh thu
   */
  @Transactional(readOnly = true)
  public DashboardInfoDto getDashboardInfo() {
    // Step 1: doc cac chi so tong quan ve hoc vien va khoa hoc trong thang hien tai.
    Long totalStudent = userRepo.countAllStudents();
    Long totalStudentCreateThisMonth = userRepo.countAllStudentsCreateThisMonth();
    Long totalCourse = courseRepo.countAllCourses();
    Long totalCourseCreateThisMonth = courseRepo.countAllCoursesCreateThisMonth();

    // Step 2: lay doanh thu tu bang payment_transaction de thay the bang payments cu.
    BigDecimal revenueThisMonth = paymentTransactionRepo.getMonthlyRevenue();
    BigDecimal revenueLastMonth = paymentTransactionRepo.getRevenueLastMonth();
    BigDecimal profitRate = BigDecimal.ZERO;

    // Step 3: tinh ti le tang truong doanh thu khi thang truoc co du lieu de so sanh.
    if (revenueLastMonth != null && revenueLastMonth.compareTo(BigDecimal.ZERO) != 0) {
      profitRate = revenueThisMonth.subtract(revenueLastMonth)
          .divide(revenueLastMonth, 4, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100))
          .setScale(2, RoundingMode.HALF_UP);
    }

    // Step 4: lay danh sach hoc vien moi de hien thi o dashboard.
    List<NewestStudentDto> newestStudents = enrollmentRepo.getStudentCreateThisMonth();

    return new DashboardInfoDto(
        totalStudent.toString(),
        totalStudentCreateThisMonth.toString(),
        totalCourse.toString(),
        totalCourseCreateThisMonth.toString(),
        revenueThisMonth.toString(),
        profitRate.toString(),
        newestStudents
    );
  }
}
