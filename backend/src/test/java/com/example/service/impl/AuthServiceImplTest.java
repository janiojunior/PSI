package com.example.service.impl;

import com.example.dto.AuthRequestDTO;
import com.example.dto.AuthResponseDTO;
import com.example.dto.UsuarioDTO;
import com.example.mapper.EstadoMapper;
import com.example.mapper.MunicipioMapper;
import com.example.mapper.TelefoneMapper;
import com.example.mapper.UsuarioMapper;
import com.example.model.Perfil;
import com.example.model.Pessoa;
import com.example.model.Usuario;
import com.example.repository.UsuarioRepository;
import com.example.resource.AuthResource;
import com.example.service.SenhaService;
import com.example.service.TokenJwtService;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceImplTest {

    private AuthServiceImpl authService;
    private AuthResource authResource;
    private SenhaService senhaService;
    private TokenJwtService tokenJwtService;
    private FakeAuthUsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() throws Exception {
        senhaService = new SenhaService();
        tokenJwtService = new TokenJwtService();
        usuarioRepository = new FakeAuthUsuarioRepository();

        MunicipioMapper municipioMapper = new MunicipioMapper();
        var estField = MunicipioMapper.class.getDeclaredField("estadoMapper");
        estField.setAccessible(true);
        estField.set(municipioMapper, new EstadoMapper());

        UsuarioMapper usuarioMapper = new UsuarioMapper();
        var telField = UsuarioMapper.class.getDeclaredField("telefoneMapper");
        telField.setAccessible(true);
        telField.set(usuarioMapper, new TelefoneMapper());
        var munField = UsuarioMapper.class.getDeclaredField("municipioMapper");
        munField.setAccessible(true);
        munField.set(usuarioMapper, municipioMapper);

        authService = new AuthServiceImpl();
        authService.usuarioRepository = usuarioRepository;
        authService.usuarioMapper = usuarioMapper;
        authService.senhaService = senhaService;
        authService.tokenJwtService = tokenJwtService;

        authResource = new AuthResource();
        var resField = AuthResource.class.getDeclaredField("authService");
        resField.setAccessible(true);
        resField.set(authResource, authService);
    }

    @Test
    void deveAutenticarComSucessoERetornarTokenJwt() {
        String hashSeed = "pbkdf2-sha256-email-v1$600000$MDEyMzQ1Njc4OWFiY2RlZg==$qpO6kYSUf8PIi026yE4TeAzDfbN8DTi8/z6JjEolZgg=";
        assertTrue(senhaService.verificar("admin@psi.com.br", "Admin@Psi2026!Segura", hashSeed));

        Pessoa pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome("Dra. Helena Martins");
        pessoa.setCpf("00000000000");
        pessoa.setEmail("admin@psi.com.br");

        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setLogin("admin@psi.com.br");
        admin.setSenha(hashSeed);
        admin.setAtivo(true);
        admin.setPessoa(pessoa);
        admin.setPerfis(Set.of(Perfil.ADMINISTRADOR));
        usuarioRepository.usuario = admin;

        Response resp = authResource.login(new AuthRequestDTO("ADMIN@psi.com.br ", "Admin@Psi2026!Segura"));
        assertEquals(200, resp.getStatus());

        String authHeader = resp.getHeaderString(HttpHeaders.AUTHORIZATION);
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("Bearer eyJ"));

        AuthResponseDTO body = (AuthResponseDTO) resp.getEntity();
        assertNotNull(body);
        assertNotNull(body.token());
        assertEquals("Bearer", body.tokenType());
        assertEquals(28800L, body.expiresIn());
        assertEquals("admin@psi.com.br", body.usuario().login());
        assertEquals("Dra. Helena Martins", body.usuario().nome());
        assertTrue(body.usuario().perfis().contains(Perfil.ADMINISTRADOR));

        UsuarioDTO me = authService.buscarUsuarioAutenticado("admin@psi.com.br");
        assertEquals("Dra. Helena Martins", me.nome());
    }

    @Test
    void deveRejeitarCredenciaisInvalidasOuUsuarioInativoCom401() {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(1L);
        pessoa.setNome("Admin");
        pessoa.setEmail("admin@psi.com.br");

        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setLogin("admin@psi.com.br");
        admin.setSenha(senhaService.gerarHash("admin@psi.com.br", "SenhaCorreta1234567"));
        admin.setAtivo(true);
        admin.setPessoa(pessoa);
        admin.setPerfis(Set.of(Perfil.ADMINISTRADOR));
        usuarioRepository.usuario = admin;

        // Senha incorreta -> 401
        assertThrows(NotAuthorizedException.class,
                () -> authResource.login(new AuthRequestDTO("admin@psi.com.br", "SenhaIncorreta9999")));

        // Usuário inexistente -> 401
        assertThrows(NotAuthorizedException.class,
                () -> authResource.login(new AuthRequestDTO("inexistente@psi.com.br", "SenhaCorreta1234567")));

        // Usuário inativo -> 401
        admin.setAtivo(false);
        assertThrows(NotAuthorizedException.class,
                () -> authResource.login(new AuthRequestDTO("admin@psi.com.br", "SenhaCorreta1234567")));
    }

    static class FakeAuthUsuarioRepository extends UsuarioRepository {
        Usuario usuario;

        @Override
        public Optional<Usuario> findByLogin(String login) {
            if (usuario != null && usuario.getLogin().equalsIgnoreCase(login.trim())) {
                return Optional.of(usuario);
            }
            return Optional.empty();
        }
    }
}
