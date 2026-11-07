package com.example.service;

import com.example.dto.MunicipioDTO;
import com.example.dto.PageResponse;
import java.util.List;

public interface MunicipioService {
    List<MunicipioDTO> findAll();
    PageResponse<MunicipioDTO> findPaginated(int page, int pageSize);
    PageResponse<MunicipioDTO> findByNome(String nome, int page, int pageSize);
    MunicipioDTO findById(Long id);
    MunicipioDTO create(MunicipioDTO dto);
    MunicipioDTO update(Long id, MunicipioDTO dto);
    void delete(Long id);
}
