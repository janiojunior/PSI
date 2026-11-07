package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record UsuarioRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres")
    String nome,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail informado é inválido")
    @Size(max = 120, message = "O e-mail deve conter no máximo 120 caracteres")
    String email,

    @NotEmpty(message = "Ao menos um telefone deve ser informado")
    List<@Valid TelefoneDTO> telefones,

    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 11, message = "O CPF deve conter exatamente 11 caracteres")
    String cpf,

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve ser anterior à data atual")
    LocalDate dataNascimento,

    @Size(max = 30, message = "O gênero deve conter no máximo 30 caracteres")
    String genero,

    @Size(max = 255, message = "O endereço deve conter no máximo 255 caracteres")
    String endereco,

    MunicipioDTO municipio,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 15, max = 128, message = "A senha deve conter entre 15 e 128 caracteres")
    String senha,

    @NotEmpty(message = "Ao menos um perfil deve ser informado")
    Set<@NotNull @Positive Long> perfis
) {
    @Override
    public String toString() {
        return "UsuarioRequestDTO[nome=" + nome + ", email=" + email + ", cpf=" + cpf
                + ", senha=[PROTEGIDA], perfis=" + perfis + "]";
    }
}
