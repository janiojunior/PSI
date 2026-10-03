package com.example.mapper;

import com.example.dto.EstadoDTO;
import com.example.model.Estado;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EstadoMapper {

    public EstadoDTO toDTO(Estado entity) {
        if (entity == null) {
            return null;
        }
        return new EstadoDTO(
            entity.getId(),
            entity.getSigla(),
            entity.getNome()
        );
    }

    public Estado toEntity(EstadoDTO dto) {
        if (dto == null) {
            return null;
        }
        Estado entity = new Estado();
        entity.setId(dto.id());
        entity.setSigla(dto.sigla() != null ? dto.sigla().toUpperCase().trim() : null);
        entity.setNome(dto.nome() != null ? dto.nome().trim() : null);
        return entity;
    }

    public void updateEntityFromDTO(EstadoDTO dto, Estado entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.sigla() != null) {
            entity.setSigla(dto.sigla().toUpperCase().trim());
        }
        if (dto.nome() != null) {
            entity.setNome(dto.nome().trim());
        }
    }
}
