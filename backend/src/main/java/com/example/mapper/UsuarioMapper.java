package com.example.mapper;

import com.example.dto.UsuarioDTO;
import com.example.dto.UsuarioRequestDTO;
import com.example.model.Pessoa;
import com.example.model.Telefone;
import com.example.model.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ApplicationScoped
public class UsuarioMapper {

    @Inject
    TelefoneMapper telefoneMapper;

    @Inject
    MunicipioMapper municipioMapper;

    public UsuarioDTO toDTO(Usuario entity) {
        if (entity == null) {
            return null;
        }
        Pessoa pessoa = entity.getPessoa();
        return new UsuarioDTO(
            entity.getId(),
            entity.getLogin(),
            pessoa != null ? pessoa.getCpf() : null,
            pessoa != null ? pessoa.getNome() : null,
            pessoa != null ? pessoa.getEmail() : null,
            pessoa != null ? pessoa.getGenero() : null,
            pessoa != null ? pessoa.getEndereco() : null,
            pessoa != null && municipioMapper != null ? municipioMapper.toDTO(pessoa.getMunicipio()) : null,
            entity.isAtivo(),
            entity.getPerfis() != null ? Set.copyOf(entity.getPerfis()) : Set.of()
        );
    }

    public void updatePessoaFromRequest(UsuarioRequestDTO dto, Pessoa pessoa) {
        if (dto == null || pessoa == null) {
            return;
        }
        if (dto.nome() != null) {
            pessoa.setNome(dto.nome().trim());
        }
        if (dto.cpf() != null) {
            pessoa.setCpf(dto.cpf().replaceAll("\\D", "").trim());
        }
        if (dto.email() != null) {
            pessoa.setEmail(dto.email().trim().toLowerCase(Locale.ROOT));
        }
        if (dto.dataNascimento() != null) {
            pessoa.setDataNascimento(dto.dataNascimento());
        }
        pessoa.setGenero(dto.genero() != null ? dto.genero().trim() : null);
        pessoa.setEndereco(dto.endereco() != null ? dto.endereco().trim() : null);
        if (dto.telefones() != null && telefoneMapper != null) {
            List<Telefone> telefones = new ArrayList<>(dto.telefones().stream().map(telefoneMapper::toEntity).toList());
            pessoa.setTelefones(telefones);
        }
    }
}
