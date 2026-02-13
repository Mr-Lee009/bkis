package vn.edu.bkis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lesson_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonVideo extends BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "position", nullable = false)
    private Integer position;
}
