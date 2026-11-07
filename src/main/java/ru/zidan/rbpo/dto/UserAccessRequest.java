package ru.zidan.rbpo.dto;

import lombok.Data;

@Data
public class UserAccessRequest {
    private String role;
    private Boolean enabled;
}
