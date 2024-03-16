package com.gritlab.buy01.userservice.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String password;
}
