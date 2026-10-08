package ru.zidan.rbpo.entity;

import lombok.Data;

import java.time.LocalDateTime;

// пользователь системы, например куратор выставки
@Data
public class User {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;

}
