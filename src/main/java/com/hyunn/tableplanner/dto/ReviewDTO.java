import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reviews")
public class ReviewDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Integer rating;

    @Column
    private String comment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}