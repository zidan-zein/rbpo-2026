package ru.zidan.rbpo.entity;

import lombok.Data;

import java.time.LocalDateTime;

// экспозиционный зал
@Data
public class ExhibitionHall {

    private Long id;
    private String name;
    private Double area; // площадь зала, м2
    private Boolean available; // доступен зал или нет (например ремонт)
    private LocalDateTime createdAt;

}
