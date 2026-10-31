package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MunicipioDTO(
    Long id,

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 60, message = "O nome deve conter entre 2 e 60 caracteres")
    String nome,

    @NotNull(message = "O estado é obrigatório")
    EstadoDTO estado
) {
    public MunicipioDTO withId(Long generatedId) {
        return new MunicipioDTO(generatedId, this.nome(), this.estado());
    }
}
