package vn.edu.bkis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DashboardInfoDto {
  private String totalStudent;
  private String totalStudentCurrentMonth;
  private String totalCourse;
  private String totalCourseCurrentMonth;
  private String revenueThisMonth;
  private String profitRate;
  private List<NewestStudentDto> students = new ArrayList<>();
}
