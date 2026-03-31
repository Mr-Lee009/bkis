package vn.edu.bkis.service.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vn.edu.bkis.dto.DashboardInfoDto;
import vn.edu.bkis.dto.NewestStudentDto;
import vn.edu.bkis.dto.StudentDto;
import vn.edu.bkis.model.User;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.EnrollmentRepository;
import vn.edu.bkis.repository.PaymentsRepository;
import vn.edu.bkis.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {
  private final UserRepository userRepo;
  private final CourseRepository courseRepo;
  private final PaymentsRepository paymentsRepo;
  private final EnrollmentRepository EnrollmentRepo;

  /**
   * Constructure
   *
   * @param userRepo     user repo.
   * @param courseRepo   course repo.
   * @param paymentsRepo payments repo.
   */
  public DashboardService(UserRepository userRepo, CourseRepository courseRepo,
      PaymentsRepository paymentsRepo,  EnrollmentRepository EnrollmentRepo) {
    this.userRepo = userRepo;
    this.courseRepo = courseRepo;
    this.paymentsRepo = paymentsRepo;
    this.EnrollmentRepo = EnrollmentRepo;
  }

  /**
   * get basic info dashboard.
   *
   * @return DashboardInfoDto object
   */
  @Transactional
  public DashboardInfoDto getDashboardInfo() {

    Long totalStudent = userRepo.countAllStudents();
    Long totalStudentCreateThisMonth = userRepo.countAllStudentsCreateThisMonth();

    Long totalCourse = courseRepo.countAllCourses();
    Long totalCourseCreateThisMonth = courseRepo.countAllCoursesCreateThisMonth();

    BigDecimal revenueThisMonth = paymentsRepo.getMonthlyRevenue();     // nên trả về BigDecimal
    BigDecimal revenueLastMonth = paymentsRepo.getRevenueLastMonth();   // nên trả về BigDecimal

    BigDecimal profitRate = BigDecimal.ZERO;

    if (revenueLastMonth != null && revenueLastMonth.compareTo(BigDecimal.ZERO) != 0) {
      profitRate = revenueThisMonth.subtract(revenueLastMonth)     // (this - last)
          .divide(revenueLastMonth, 4, RoundingMode.HALF_UP) // chia lấy 4 chữ số thập phân
          .multiply(BigDecimal.valueOf(100))                       // * 100 (%)
          .setScale(2, RoundingMode.HALF_UP);             // làm tròn 2 số thập phân
    }

    // get list of student create new this month
    List<NewestStudentDto> userEntities = EnrollmentRepo.getStudentCreateThisMonth();

    return new DashboardInfoDto(totalStudent.toString(), totalStudentCreateThisMonth.toString(),
        totalCourse.toString(), totalCourseCreateThisMonth.toString(), revenueThisMonth.toString(),
        profitRate.toString(), userEntities);
  }
}
