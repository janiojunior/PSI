package com.example.service;

import com.example.dto.PageResponse;
import com.example.dto.UsuarioDTO;
import com.example.dto.UsuarioRequestDTO;
import com.example.dto.UsuarioUpdateDTO;
import java.util.List;

public interface UsuarioService {
    List<UsuarioDTO> findAll();
    PageResponse<UsuarioDTO> findPaginated(int page, int pageSize);
    PageResponse<UsuarioDTO> findByNome(String nome, int page, int pageSize);
    UsuarioDTO findById(Long id);
    UsuarioDTO findByCpf(String cpf);
    UsuarioDTO create(UsuarioRequestDTO dto);
    UsuarioDTO update(Long id, UsuarioUpdateDTO dto);
    void delete(Long id);
    void validarAlteracaoEmail(Long pessoaId, String email);
}
