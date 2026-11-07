package com.example.service;

import com.example.dto.PageResponse;
import com.example.dto.PsicologoDTO;
import java.util.List;

public interface PsicologoService {
    List<PsicologoDTO> findAll();
    PageResponse<PsicologoDTO> findPaginated(int page, int pageSize);
    PageResponse<PsicologoDTO> findByNome(String nome, int page, int pageSize);
    PsicologoDTO findById(Long id);
    PsicologoDTO findByCpf(String cpf);
    PsicologoDTO findByCrp(String crp);
    PsicologoDTO create(PsicologoDTO dto);
    PsicologoDTO update(Long id, PsicologoDTO dto);
    void delete(Long id);
}
