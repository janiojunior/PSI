package com.example.dto;

public record AuthResponseDTO(
    String token,
    String tokenType,
    long expiresIn,
    UsuarioDTO usuario
) {
}
