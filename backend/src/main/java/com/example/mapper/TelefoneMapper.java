package com.example.mapper;

import com.example.dto.TelefoneDTO;
import com.example.model.Telefone;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TelefoneMapper {

    public TelefoneDTO toDTO(Telefone entity) {
        if (entity == null) {
            return null;
        }
        return new TelefoneDTO(
            entity.getId(),
            entity.getCodigoArea(),
            entity.getNumero(),
            entity.isWhatsapp()
        );
    }

    public Telefone toEntity(TelefoneDTO dto) {
        if (dto == null) {
            return null;
        }
        Telefone entity = new Telefone();
        entity.setId(dto.id());
        updateEntityFromDTO(dto, entity);
        return entity;
    }

    public void updateEntityFromDTO(TelefoneDTO dto, Telefone entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.codigoArea() != null) {
            entity.setCodigoArea(dto.codigoArea().trim());
        }
        if (dto.numero() != null) {
            entity.setNumero(dto.numero().trim());
        }
        if (dto.whatsapp() != null) {
            entity.setWhatsapp(dto.whatsapp());
        }
    }
}
