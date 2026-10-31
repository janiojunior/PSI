package com.example.service;

import com.example.dto.MunicipioDTO;
import java.util.List;

public interface MunicipioService {
    List<MunicipioDTO> findAll();
    MunicipioDTO findById(Long id);
    MunicipioDTO create(MunicipioDTO dto);
    MunicipioDTO update(Long id, MunicipioDTO dto);
    void delete(Long id);
}
