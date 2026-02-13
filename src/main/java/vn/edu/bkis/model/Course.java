package vn.edu.bkis.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description")
    @Lob
    private String description;

    @Column(name = "teacher_id", nullable = false, length = 36)
    private String teacherId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "total_students")
    private Integer totalStudents;

    @Column(name = "active_flag")
    private Boolean activeFlag;

    @Column(name = "tag", length = 100)
    private String tag;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "rating")
    private Integer rating;
}
