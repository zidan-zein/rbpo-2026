package ru.zidan.rbpo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// экспозиционный зал
@Entity
@Table(name = "exhibition_halls", schema = "app")
@Getter
@Setter
@NoArgsConstructor
public class ExhibitionHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Double area; // площадь зала, м2

    @Column(nullable = false)
    private Boolean available; // доступен зал или нет (например ремонт)

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
