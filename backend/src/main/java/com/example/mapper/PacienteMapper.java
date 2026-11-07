package com.example.mapper;

import com.example.dto.PacienteDTO;
import com.example.model.Paciente;
import com.example.model.Pessoa;
import com.example.model.Telefone;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class PacienteMapper {

    @Inject
    TelefoneMapper telefoneMapper;

    @Inject
    MunicipioMapper municipioMapper;

    public PacienteDTO toDTO(Paciente entity) {
        if (entity == null) {
            return null;
        }
        Pessoa pessoa = entity.getPessoa();
        return new PacienteDTO(
            entity.getId(),
            pessoa != null ? pessoa.getNome() : null,
            pessoa != null ? pessoa.getCpf() : null,
            pessoa != null ? pessoa.getEmail() : null,
            pessoa != null ? pessoa.getDataNascimento() : null,
            pessoa != null ? pessoa.getGenero() : null,
            pessoa != null ? pessoa.getEndereco() : null,
            pessoa != null && municipioMapper != null ? municipioMapper.toDTO(pessoa.getMunicipio()) : null,
            pessoa != null && pessoa.getTelefones() != null && telefoneMapper != null
                ? pessoa.getTelefones().stream().map(telefoneMapper::toDTO).toList()
                : List.of(),
            entity.getObservacoes(),
            entity.isAtivo()
        );
    }

    public PacienteDTO toDTOFromPessoa(Pessoa pessoa) {
        if (pessoa == null) {
            return null;
        }
        return new PacienteDTO(
            null,
            pessoa.getNome(),
            pessoa.getCpf(),
            pessoa.getEmail(),
            pessoa.getDataNascimento(),
            pessoa.getGenero(),
            pessoa.getEndereco(),
            municipioMapper != null ? municipioMapper.toDTO(pessoa.getMunicipio()) : null,
            pessoa.getTelefones() != null && telefoneMapper != null
                ? pessoa.getTelefones().stream().map(telefoneMapper::toDTO).toList()
                : List.of(),
            null,
            null
        );
    }

    public Paciente toEntity(PacienteDTO dto) {
        if (dto == null) {
            return null;
        }
        Paciente entity = new Paciente();
        entity.setId(dto.id());
        entity.setPessoa(new Pessoa());
        updateEntityFromDTO(dto, entity);
        return entity;
    }

    public void updateEntityFromDTO(PacienteDTO dto, Paciente entity) {
        if (dto == null || entity == null) {
            return;
        }
        Pessoa pessoa = entity.getPessoa();
        if (pessoa == null) {
            pessoa = new Pessoa();
            entity.setPessoa(pessoa);
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
        entity.setObservacoes(dto.observacoes() != null ? dto.observacoes().trim() : null);
        if (dto.ativo() != null) {
            entity.setAtivo(dto.ativo());
        }
    }
}
