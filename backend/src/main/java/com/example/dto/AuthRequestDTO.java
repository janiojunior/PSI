package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDTO(
    @NotBlank(message = "O login é obrigatório")
    @Email(message = "O login deve ser um e-mail válido")
    @Size(max = 120, message = "O login deve conter no máximo 120 caracteres")
    String login,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "A senha é obrigatória")
    String senha
) {
    @Override
    public String toString() {
        return "AuthRequestDTO[login=" + login + ", senha=[PROTEGIDA]]";
    }
}
