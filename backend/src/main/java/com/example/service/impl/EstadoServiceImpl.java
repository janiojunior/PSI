package com.example.service.impl;

import com.example.dto.EstadoDTO;
import com.example.dto.PageResponse;
import com.example.mapper.EstadoMapper;
import com.example.model.Estado;
import com.example.repository.EstadoRepository;
import com.example.service.EstadoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
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
    public PageResponse<EstadoDTO> findPaginated(int page, int pageSize) {
        validarPaginacao(page, pageSize);
        List<Estado> items = estadoRepository.listPaginated(page, pageSize);
        long total = estadoRepository.count();
        return PageResponse.of(items, page, pageSize, total, estadoMapper::toDTO);
    }

    @Override
    public PageResponse<EstadoDTO> findByNome(String nome, int page, int pageSize) {
        validarPaginacao(page, pageSize);
        List<Estado> items = estadoRepository.findByNome(nome, page, pageSize);
        long total = estadoRepository.countByNome(nome);
        return PageResponse.of(items, page, pageSize, total, estadoMapper::toDTO);
    }

    @Override
    public EstadoDTO findById(Long id) {
        Estado entity = estadoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Estado com ID " + id + " não encontrado."));
        return estadoMapper.toDTO(entity);
    }

    @Override
    public EstadoDTO findBySigla(String sigla) {
        Estado entity = estadoRepository.findBySigla(sigla)
                .orElseThrow(() -> new NotFoundException("Estado com sigla " + sigla + " não encontrado."));
        return estadoMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public EstadoDTO create(EstadoDTO dto) {
        validarUnicidadeSigla(dto.sigla(), null);
        Estado entity = estadoMapper.toEntity(dto);
        estadoRepository.persist(entity);
        return estadoMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public EstadoDTO update(Long id, EstadoDTO dto) {
        Estado entity = estadoRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Estado com ID " + id + " não encontrado."));
        validarUnicidadeSigla(dto.sigla(), id);
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

    private void validarUnicidadeSigla(String sigla, Long idAtual) {
        if (sigla != null && estadoRepository.findBySiglaExceptId(sigla, idAtual).isPresent()) {
            throw new BadRequestException("Já existe um estado cadastrado com essa sigla.");
        }
    }

    private void validarPaginacao(int page, int pageSize) {
        if (page < 0 || pageSize < 1 || pageSize > 100) {
            throw new BadRequestException("Use página não negativa e tamanho entre 1 e 100.");
        }
    }
}
