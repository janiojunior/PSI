package com.example.service.impl;

import com.example.dto.AuthRequestDTO;
import com.example.dto.AuthResponseDTO;
import com.example.dto.UsuarioDTO;
import com.example.mapper.UsuarioMapper;
import com.example.model.Usuario;
import com.example.repository.UsuarioRepository;
import com.example.service.AuthService;
import com.example.service.SenhaService;
import com.example.service.TokenJwtService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    UsuarioMapper usuarioMapper;

    @Inject
    SenhaService senhaService;

    @Inject
    TokenJwtService tokenJwtService;

    @Override
    public AuthResponseDTO autenticar(AuthRequestDTO dto) {
        if (dto == null || dto.login() == null || dto.senha() == null) {
            throw criarExcecaoNaoAutorizado("Login e senha são obrigatórios.");
        }

        String loginNormalizado = dto.login().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByLogin(loginNormalizado).orElse(null);

        if (usuario == null || !usuario.isAtivo()) {
            throw criarExcecaoNaoAutorizado("E-mail ou senha inválidos, ou usuário inativo.");
        }

        boolean senhaValida = senhaService.verificar(usuario.getLogin(), dto.senha(), usuario.getSenha());
        if (!senhaValida) {
            throw criarExcecaoNaoAutorizado("E-mail ou senha inválidos, ou usuário inativo.");
        }

        UsuarioDTO usuarioDTO = usuarioMapper.toDTO(usuario);
        String token = tokenJwtService.generateJwt(usuarioDTO);

        return new AuthResponseDTO(
            token,
            "Bearer",
            tokenJwtService.getDurationSeconds(),
            usuarioDTO
        );
    }

    @Override
    public UsuarioDTO buscarUsuarioAutenticado(String login) {
        if (login == null || login.isBlank()) {
            throw criarExcecaoNaoAutorizado("Usuário não autenticado.");
        }
        Usuario usuario = usuarioRepository.findByLogin(login.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado."));
        if (!usuario.isAtivo()) {
            throw criarExcecaoNaoAutorizado("Usuário inativo.");
        }
        return usuarioMapper.toDTO(usuario);
    }

    private NotAuthorizedException criarExcecaoNaoAutorizado(String mensagem) {
        return new NotAuthorizedException(
            Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("message", mensagem))
                .build()
        );
    }
}
