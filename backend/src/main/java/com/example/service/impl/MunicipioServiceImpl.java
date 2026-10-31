package com.example.service.impl;

import com.example.dto.MunicipioDTO;
import com.example.mapper.MunicipioMapper;
import com.example.model.Estado;
import com.example.model.Municipio;
import com.example.repository.EstadoRepository;
import com.example.repository.MunicipioRepository;
import com.example.service.MunicipioService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

@ApplicationScoped
public class MunicipioServiceImpl implements MunicipioService {

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    EstadoRepository estadoRepository;

    @Inject
    MunicipioMapper municipioMapper;

    @Override
    public List<MunicipioDTO> findAll() {
        return municipioRepository.listAllWithEstado().stream()
                .map(municipioMapper::toDTO)
                .toList();
    }

    @Override
    public MunicipioDTO findById(Long id) {
        Municipio entity = municipioRepository.findByIdWithEstado(id)
                .orElseThrow(() -> new NotFoundException("Município com ID " + id + " não encontrado."));
        return municipioMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public MunicipioDTO create(MunicipioDTO dto) {
        Estado estado = buscarEstadoOuFalhar(dto);
        Municipio entity = municipioMapper.toEntity(dto);
        entity.setEstado(estado);
        municipioRepository.persist(entity);
        return municipioMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public MunicipioDTO update(Long id, MunicipioDTO dto) {
        Municipio entity = municipioRepository.findByIdWithEstado(id)
                .orElseThrow(() -> new NotFoundException("Município com ID " + id + " não encontrado."));
        Estado estado = buscarEstadoOuFalhar(dto);
        municipioMapper.updateEntityFromDTO(dto, entity);
        entity.setEstado(estado);
        return municipioMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        boolean deleted = municipioRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Município com ID " + id + " não encontrado.");
        }
    }

    private Estado buscarEstadoOuFalhar(MunicipioDTO dto) {
        if (dto.estado() == null || dto.estado().id() == null) {
            throw new BadRequestException("O ID do estado é obrigatório.");
        }
        Long idEstado = dto.estado().id();
        return estadoRepository.findByIdOptional(idEstado)
                .orElseThrow(() -> new NotFoundException("Estado com ID " + idEstado + " não encontrado."));
    }
}
