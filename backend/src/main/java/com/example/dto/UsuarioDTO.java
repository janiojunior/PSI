package com.example.dto;

import com.example.model.Perfil;
import java.util.Set;

public record UsuarioDTO(
    Long id,
    String login,
    String cpf,
    String nome,
    String email,
    String genero,
    String endereco,
    MunicipioDTO municipio,
    Boolean ativo,
    Set<Perfil> perfis
) {
}
