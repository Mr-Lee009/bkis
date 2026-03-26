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

@Service
public class HomeService {

  private static final String DEFAULT_COURSE_IMAGE = "/img/course-1.jpg";

  private final CourseRepository courseRepository;
  private final UserRepository userRepository;

  public HomeService(CourseRepository courseRepository, UserRepository userRepository) {
    this.courseRepository = courseRepository;
    this.userRepository = userRepository;
  }

  /**
   * Fetches the top 6 active courses ordered by creation date and maps them to HomeCourseDto. It
   * also retrieves the teacher names for the courses and normalizes the image URLs.
   *
   * @return a list of HomeCourseDto representing the featured courses
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
   * Fetches the top 6 active courses with a specific tag ordered by creation date and maps them to
   * HomeCourseDto. It also retrieves the teacher names for the courses and normalizes the image
   * URLs.
   *
   * @param tag the tag to filter courses by
   * @return a list of HomeCourseDto representing the featured courses with the specified tag
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
