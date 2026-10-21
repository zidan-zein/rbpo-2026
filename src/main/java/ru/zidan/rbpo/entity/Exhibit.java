package ru.zidan.rbpo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// экспонат
@Entity
@Table(name = "exhibits", schema = "app")
@Getter
@Setter
@NoArgsConstructor
public class Exhibit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // GOOD, NEEDS_RESTORATION или IN_RESTORATION
    @Column(name = "exhibit_condition", nullable = false)
    private String condition;

    @Column(nullable = false)
    private Double requiredArea; // сколько места нужно в зале, м2

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
