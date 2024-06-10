package com.hyunn.tableplanner.model;// Review.java
import lombok.Data;
import jakarta.persistence.*;

@Entity
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "RESERVATION_ID")
    private Reservation reservation;
}