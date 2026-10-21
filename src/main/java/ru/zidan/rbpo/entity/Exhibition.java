package ru.zidan.rbpo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// выставка
@Entity
@Table(name = "exhibitions", schema = "app")
@Getter
@Setter
@NoArgsConstructor
public class Exhibition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // PLANNED, OPEN, CLOSED или CANCELLED
    @Column(nullable = false)
    private String status;

    // у одного куратора может быть много выставок
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curator_id")
    private User curator;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
