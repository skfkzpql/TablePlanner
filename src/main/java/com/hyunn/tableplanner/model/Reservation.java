package com.hyunn.tableplanner.model;// Reservation.java
import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String time;
    
    @Column(nullable = false)
    private boolean isConfirmed;
    
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    private Store store;
}