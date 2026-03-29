package vn.edu.bkis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import vn.edu.bkis.dto.HomeCourseDto;
import vn.edu.bkis.model.Course;
import vn.edu.bkis.model.User;
import vn.edu.bkis.repository.CourseRepository;
import vn.edu.bkis.repository.UserRepository;
import vn.edu.bkis.util.BkisNumberUtils;

/**
 * Service layer for home page functionality.
 * Handles featured course retrieval, filtering by tags, and data transformation.
 */
@Service
public class HomeService {

  private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";

  private final CourseRepository courseRepository;
  private final UserRepository userRepository;

  /**
   * Constructor for dependency injection.
   * @param courseRepository for course data access
   * @param userRepository for instructor information
   */
  public HomeService(CourseRepository courseRepository, UserRepository userRepository) {
    this.courseRepository = courseRepository;
    this.userRepository = userRepository;
  }

  /**
   * Retrieve top 6 active courses ordered by creation date (newest first).
   * Enriches course data with instructor names and normalized image paths.
   *
   * @return list of up to 6 featured courses as DTOs
   */
  public List<HomeCourseDto> getFeaturedCourses() {
    List<Course> courses = courseRepository.findTop6ByActiveFlagTrueOrderByCreatedAtDesc();
    Map<String, String> teachers = userRepository.findAllById(
            courses.stream().map(Course::getTeacherId).filter(Objects::nonNull).distinct().toList())
        .stream().collect(Collectors.toMap(User::getId, User::getUsername));

    return courses.stream().map(
        course -> new HomeCourseDto(course.getId(), course.getTitle(), course.getDescription(),
            teachers.getOrDefault(course.getTeacherId(), "BKIS Instructor"),
            BkisNumberUtils.defaultPrice(course.getPrice()),
            BkisNumberUtils.defaultInteger(course.getTotalStudents(), 0),
            2 + (int) ((course.getId() == null ? 1 : course.getId()) % 4),
            BkisNumberUtils.defaultInteger(course.getRating(), 5),
            120 + (int) ((course.getId() == null ? 1 : course.getId()) * 3),
            normalizeImage(course.getImageUrl()), course.getTag())).toList();
  }

  /**
   * Retrieve courses filtered by tag, ordered by creation date (newest first).
   * Enriches course data with instructor names and normalized image paths.
   *
   * @param tag the course category/tag to filter by
   * @return list of up to 6 active courses with matching tag as DTOs
   */
  public List<HomeCourseDto> getFeaturedCoursesByTag(String tag) {
    List<Course> courses = courseRepository.findTop6ByActiveFlagTrueAndTagOrderByCreatedAtDesc(tag);
    Map<String, String> teachers = userRepository.findAllById(
            courses.stream().map(Course::getTeacherId).filter(Objects::nonNull).distinct().toList())
        .stream().collect(Collectors.toMap(User::getId, User::getUsername));

    return courses.stream().map(
        course -> new HomeCourseDto(course.getId(), course.getTitle(), course.getDescription(),
            teachers.getOrDefault(course.getTeacherId(), "BKIS Instructor"),
            BkisNumberUtils.defaultPrice(course.getPrice()),
            BkisNumberUtils.defaultInteger(course.getTotalStudents(), 0),
            2 + (int) ((course.getId() == null ? 1 : course.getId()) % 4),
            BkisNumberUtils.defaultInteger(course.getRating(), 5),
            120 + (int) ((course.getId() == null ? 1 : course.getId()) * 3),
            normalizeImage(course.getImageUrl()), course.getTag())).toList();
  }

  /**
   * Retrieve top 4 active courses ordered by number of enrollments (most enrolled first).
   * Enriches course data with instructor names and normalized image paths.
   *
   * @return list of up to 4 most popular courses by enrollment count as DTOs
   */
  public List<HomeCourseDto> getTop4CoursesByEnrollment() {
    List<Course> courses = courseRepository.findTop4ByActiveFlagTrueOrderByTotalStudentsDesc();
    Map<String, String> teachers = userRepository.findAllById(
            courses.stream().map(Course::getTeacherId).filter(Objects::nonNull).distinct().toList())
        .stream().collect(Collectors.toMap(User::getId, User::getUsername));

    return courses.stream().map(
        course -> new HomeCourseDto(course.getId(), course.getTitle(), course.getDescription(),
            teachers.getOrDefault(course.getTeacherId(), "BKIS Instructor"),
            BkisNumberUtils.defaultPrice(course.getPrice()),
            BkisNumberUtils.defaultInteger(course.getTotalStudents(), 0),
            2 + (int) ((course.getId() == null ? 1 : course.getId()) % 4),
            BkisNumberUtils.defaultInteger(course.getRating(), 5),
            120 + (int) ((course.getId() == null ? 1 : course.getId()) * 3),
            normalizeImage(course.getImageUrl()), course.getTag())).toList();
  }

  /**
   * Normalize image URLs to ensure consistent path format.
   * Returns absolute path or HTTP URL; falls back to default image if null.
   * @param imageUrl the raw image URL from database
   * @return normalized image URL path
   */
  private String normalizeImage(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return DEFAULT_COURSE_IMAGE;
    }
    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith(
        "/")) {
      return imageUrl;
    }
    return "/" + imageUrl;
  }
}
