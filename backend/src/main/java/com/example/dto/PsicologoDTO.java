package com.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PsicologoDTO(
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

    @NotBlank(message = "O CRP é obrigatório")
    @Size(max = 20, message = "O CRP deve conter no máximo 20 caracteres")
    String crp,

    @NotBlank(message = "A especialidade é obrigatória")
    @Size(max = 120, message = "A especialidade deve conter no máximo 120 caracteres")
    String especialidade,

    String bio,

    @NotNull(message = "O valor da consulta é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O valor da consulta deve ser maior que zero")
    BigDecimal valorConsulta,

    @NotNull(message = "A duração da consulta é obrigatória")
    @Positive(message = "A duração da consulta deve ser maior que zero")
    Integer duracaoConsulta,

    @NotNull(message = "O campo ativo é obrigatório")
    Boolean ativo
) {
    public PsicologoDTO withId(Long generatedId) {
        return new PsicologoDTO(
            generatedId,
            this.nome(),
            this.cpf(),
            this.email(),
            this.dataNascimento(),
            this.genero(),
            this.endereco(),
            this.municipio(),
            this.telefones(),
            this.crp(),
            this.especialidade(),
            this.bio(),
            this.valorConsulta(),
            this.duracaoConsulta(),
            this.ativo()
        );
    }
}
