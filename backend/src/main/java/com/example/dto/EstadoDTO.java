package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstadoDTO(
    Long id,

    @NotBlank(message = "A sigla é obrigatória")
    @Size(min = 2, max = 2, message = "A sigla deve conter exatamente 2 caracteres")
    String sigla,

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve conter entre 2 e 100 caracteres")
    String nome
) {
    public EstadoDTO withId(Long generatedId) {
        return new EstadoDTO(generatedId, this.sigla(), this.nome());
    }
}
