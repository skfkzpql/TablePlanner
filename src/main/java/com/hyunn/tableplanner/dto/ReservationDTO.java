import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reservations")
public class ReservationDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;
    
    @Column(name="store_id", nullable = false)
    private Long storeId;

    @Column(name="reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Enumerated(EnumType.STRING)
    @Column(name="approval_status", nullable = false)
    private ApprovalStatus approvalStatus;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED,
        OVERDUE
    }
    
    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}