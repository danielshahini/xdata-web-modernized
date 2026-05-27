package com.xdata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Login-ID darf nicht leer sein")
    private String loginId;

    @NotBlank(message = "Passwort darf nicht leer sein")
    private String password;
}
