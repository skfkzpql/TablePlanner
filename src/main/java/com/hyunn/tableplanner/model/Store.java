// Store.java
import lombok.Data;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Data
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "PARTNER_ID")
    private Partner partner;
  
    @OneToMany(mappedBy = "store")
    private List<Reservation> reservations;
}