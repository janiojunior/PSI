package com.example.service;

import com.example.dto.PacienteDTO;
import com.example.dto.PageResponse;
import java.util.List;

public interface PacienteService {
    List<PacienteDTO> findAll();
    PageResponse<PacienteDTO> findPaginated(int page, int pageSize);
    PageResponse<PacienteDTO> findByNome(String nome, int page, int pageSize);
    PacienteDTO findById(Long id);
    PacienteDTO findByCpf(String cpf);
    PacienteDTO create(PacienteDTO dto);
    PacienteDTO update(Long id, PacienteDTO dto);
    void delete(Long id);
}
