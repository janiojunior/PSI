package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TelefoneDTO(
    Long id,

    @NotBlank(message = "O código de área é obrigatório")
    @Size(min = 2, max = 3, message = "O código de área deve conter entre 2 e 3 caracteres")
    String codigoArea,

    @NotBlank(message = "O número do telefone é obrigatório")
    @Size(min = 8, max = 15, message = "O número deve conter entre 8 e 15 caracteres")
    String numero,

    @NotNull(message = "A indicação de WhatsApp é obrigatória")
    Boolean whatsapp
) {
    public TelefoneDTO(String codigoArea, String numero, Boolean whatsapp) {
        this(null, codigoArea, numero, whatsapp);
    }
}
