package vn.edu.bkis.dto;

import java.time.LocalDateTime;

public interface NewestStudentDto {

  String getUsername();

  String getTitleCourse();

  LocalDateTime getEnrolledAt();

  String getStatus();
}
