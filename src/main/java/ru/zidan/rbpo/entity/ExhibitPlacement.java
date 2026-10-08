package ru.zidan.rbpo.entity;

import lombok.Data;

import java.time.LocalDateTime;

// размещение экспоната: какой экспонат, на какой выставке и в каком зале
@Data
public class ExhibitPlacement {

    private Long id;
    private Long exhibitId;
    private Long exhibitionId;
    private Long hallId;
    private String mountingStatus; // статус монтажа
    private LocalDateTime createdAt;

}
