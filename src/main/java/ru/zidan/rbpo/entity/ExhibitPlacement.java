package ru.zidan.rbpo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// размещение экспоната: какой экспонат, на какой выставке и в каком зале
@Entity
@Table(name = "exhibit_placements", schema = "app")
@Getter
@Setter
@NoArgsConstructor
public class ExhibitPlacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exhibit_id", nullable = false)
    private Exhibit exhibit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exhibition_id", nullable = false)
    private Exhibition exhibition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private ExhibitionHall hall;

    // PLANNED, MOUNTED или DISMANTLED
    @Column(nullable = false)
    private String mountingStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
