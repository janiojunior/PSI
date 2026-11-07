package com.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record PacienteDTO(
    Long id,

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres")
    String nome,

    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 11, message = "O CPF deve conter exatamente 11 caracteres")
    String cpf,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail informado é inválido")
    @Size(max = 120, message = "O e-mail deve conter no máximo 120 caracteres")
    String email,

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve ser anterior à data atual")
    LocalDate dataNascimento,

    @Size(max = 30, message = "O gênero deve conter no máximo 30 caracteres")
    String genero,

    @Size(max = 255, message = "O endereço deve conter no máximo 255 caracteres")
    String endereco,

    MunicipioDTO municipio,

    @NotEmpty(message = "Ao menos um telefone deve ser informado")
    List<@Valid TelefoneDTO> telefones,

    String observacoes,

    @NotNull(message = "O campo ativo é obrigatório")
    Boolean ativo
) {
    public PacienteDTO withId(Long generatedId) {
        return new PacienteDTO(
            generatedId,
            this.nome(),
            this.cpf(),
            this.email(),
            this.dataNascimento(),
            this.genero(),
            this.endereco(),
            this.municipio(),
            this.telefones(),
            this.observacoes(),
            this.ativo()
        );
    }
}
