package ru.zidan.rbpo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

// выставка
@Data
public class Exhibition {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long curatorId; // id пользователя, который куратор
    private LocalDateTime createdAt;

}
