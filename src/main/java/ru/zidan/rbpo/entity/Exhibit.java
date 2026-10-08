package ru.zidan.rbpo.entity;

import lombok.Data;

import java.time.LocalDateTime;

// экспонат
@Data
public class Exhibit {

    private Long id;
    private String name;
    private String condition; // состояние, например "хорошее" или "нужна реставрация"
    private Double requiredArea; // сколько места нужно в зале, м2
    private LocalDateTime createdAt;

}
