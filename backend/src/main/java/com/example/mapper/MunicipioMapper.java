package com.example.mapper;

import com.example.dto.MunicipioDTO;
import com.example.model.Municipio;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MunicipioMapper {

    @Inject
    EstadoMapper estadoMapper;

    public MunicipioDTO toDTO(Municipio entity) {
        if (entity == null) {
            return null;
        }
        return new MunicipioDTO(
            entity.getId(),
            entity.getNome(),
            estadoMapper.toDTO(entity.getEstado())
        );
    }

    public Municipio toEntity(MunicipioDTO dto) {
        if (dto == null) {
            return null;
        }
        Municipio entity = new Municipio();
        entity.setId(dto.id());
        entity.setNome(dto.nome() != null ? dto.nome().trim() : null);
        return entity;
    }

    public void updateEntityFromDTO(MunicipioDTO dto, Municipio entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.nome() != null) {
            entity.setNome(dto.nome().trim());
        }
    }
}
