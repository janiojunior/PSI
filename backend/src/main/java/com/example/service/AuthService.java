package com.example.service;

import com.example.dto.AuthRequestDTO;
import com.example.dto.AuthResponseDTO;
import com.example.dto.UsuarioDTO;

public interface AuthService {
    AuthResponseDTO autenticar(AuthRequestDTO dto);
    UsuarioDTO buscarUsuarioAutenticado(String login);
}
