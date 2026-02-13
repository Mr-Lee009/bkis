package vn.edu.bkis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description")
    @Lob
    private String description;

    @Column(name = "position", nullable = false)
    private Integer position;
}
