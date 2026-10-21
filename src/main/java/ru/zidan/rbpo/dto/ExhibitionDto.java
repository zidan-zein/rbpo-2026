package ru.zidan.rbpo.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

// выставка в запросах и ответах, вместо куратора только его id
@Data
public class ExhibitionDto {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long curatorId;
    private LocalDateTime createdAt;
}
