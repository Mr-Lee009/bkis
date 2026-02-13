package vn.edu.bkis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Progress extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "student_id", nullable = false, length = 36)
    private String studentId;

    @Column(name = "lesson_video_id", nullable = false)
    private Long lessonVideoId;

    @Column(name = "watched_duration")
    private Integer watchedDuration;

    @Column(name = "is_completed")
    private Boolean completed;
}
