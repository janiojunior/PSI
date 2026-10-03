package com.example.service;

import com.example.dto.EstadoDTO;
import java.util.List;

public interface EstadoService {
    List<EstadoDTO> findAll();
    EstadoDTO findById(Long id);
    EstadoDTO create(EstadoDTO dto);
    EstadoDTO update(Long id, EstadoDTO dto);
    void delete(Long id);
}
