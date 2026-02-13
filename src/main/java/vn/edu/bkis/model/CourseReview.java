package vn.edu.bkis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseReview extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "student_id", nullable = false, length = 36)
    private String studentId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment")
    @Lob
    private String comment;
}
