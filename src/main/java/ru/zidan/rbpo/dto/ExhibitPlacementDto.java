package ru.zidan.rbpo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExhibitPlacementDto {
    private Long id;
    private Long exhibitId;
    private Long exhibitionId;
    private Long hallId;
    private String mountingStatus;
    private LocalDateTime createdAt;
}
