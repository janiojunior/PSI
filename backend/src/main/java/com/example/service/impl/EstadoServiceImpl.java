package com.example.service.impl;

import com.example.dto.EstadoDTO;
import com.example.mapper.EstadoMapper;
import com.example.model.Estado;
import com.example.repository.EstadoRepository;
import com.example.service.EstadoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

@ApplicationScoped
public class EstadoServiceImpl implements EstadoService {

    @Inject
    EstadoRepository estadoRepository;

    @Inject
    EstadoMapper estadoMapper;

    @Override
    public List<EstadoDTO> findAll() {
        return estadoRepository.listAll().stream()
                .map(estadoMapper::toDTO)
                .toList();
    }

    @Override
    public EstadoDTO findById(Long id) {
        Estado entity = estadoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Estado com ID " + id + " não encontrado."));
        return estadoMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public EstadoDTO create(EstadoDTO dto) {
        Estado entity = estadoMapper.toEntity(dto);
        estadoRepository.persist(entity);
        return estadoMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public EstadoDTO update(Long id, EstadoDTO dto) {
        Estado entity = estadoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Estado com ID " + id + " não encontrado."));
        estadoMapper.updateEntityFromDTO(dto, entity);
        return estadoMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        boolean deleted = estadoRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Estado com ID " + id + " não encontrado.");
        }
    }
}
