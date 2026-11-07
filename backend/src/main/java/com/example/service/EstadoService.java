package com.example.service;

import com.example.dto.EstadoDTO;
import com.example.dto.PageResponse;
import java.util.List;

public interface EstadoService {
    List<EstadoDTO> findAll();
    PageResponse<EstadoDTO> findPaginated(int page, int pageSize);
    PageResponse<EstadoDTO> findByNome(String nome, int page, int pageSize);
    EstadoDTO findById(Long id);
    EstadoDTO findBySigla(String sigla);
    EstadoDTO create(EstadoDTO dto);
    EstadoDTO update(Long id, EstadoDTO dto);
    void delete(Long id);
}
