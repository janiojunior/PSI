package com.example.service.impl;

import com.example.dto.TelefoneDTO;
import com.example.dto.UsuarioDTO;
import com.example.dto.UsuarioRequestDTO;
import com.example.dto.UsuarioUpdateDTO;
import com.example.mapper.EstadoMapper;
import com.example.mapper.MunicipioMapper;
import com.example.mapper.TelefoneMapper;
import com.example.mapper.UsuarioMapper;
import com.example.model.Perfil;
import com.example.model.Pessoa;
import com.example.model.Usuario;
import com.example.repository.PessoaRepository;
import com.example.repository.UsuarioRepository;
import com.example.resource.UsuarioResource;
import com.example.service.SenhaService;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioAndSenhaServiceTest {

    private UsuarioServiceImpl service;
    private UsuarioResource resource;
    private SenhaService senhaService;
    private FakeUsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() throws Exception {
        usuarioRepository = new FakeUsuarioRepository();
        senhaService = new SenhaService();

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

        service = new UsuarioServiceImpl();
        service.usuarioRepository = usuarioRepository;
        service.pessoaRepository = new FakePessoaRepository();
        service.usuarioMapper = usuarioMapper;
        service.senhaService = senhaService;

        resource = new UsuarioResource();
        var resField = UsuarioResource.class.getDeclaredField("usuarioService");
        resField.setAccessible(true);
        resField.set(resource, service);
    }

    @Test
    void deveGerarEVerificarHashDeSenhaComSaltEEmail() {
        String hash = senhaService.gerarHash("admin@sga.com", "SenhaForteComMaisDe15Chars!");
        assertNotNull(hash);
        assertTrue(hash.startsWith("pbkdf2-sha256-email-v1$600000$"));
        assertTrue(senhaService.verificar("ADMIN@sga.com", "SenhaForteComMaisDe15Chars!", hash));
        assertFalse(senhaService.verificar("outro@sga.com", "SenhaForteComMaisDe15Chars!", hash));
        assertFalse(senhaService.verificar("admin@sga.com", "SenhaErrada12345678", hash));
    }

    @Test
    void deveCriarAtualizarEValidarTrocaDeEmailDeUsuario() {
        UsuarioRequestDTO req = new UsuarioRequestDTO(
                "Carlos Admin",
                "carlos.admin@sga.com",
                List.of(new TelefoneDTO("63", "991112222", true)),
                "12345678901",
                LocalDate.of(1990, 5, 20),
                "Masculino",
                "Plano Diretor Norte",
                null,
                "SenhaSuperSeguraCom18Chars",
                Set.of(1L, 2L)
        );

        Response postResp = resource.create(req);
        assertEquals(201, postResp.getStatus());
        UsuarioDTO criado = (UsuarioDTO) postResp.getEntity();
        assertEquals(1L, criado.id());
        assertEquals("carlos.admin@sga.com", criado.login());
        assertTrue(criado.perfis().contains(Perfil.ADMINISTRADOR));
        assertTrue(criado.perfis().contains(Perfil.ATENDENTE));

        // Valida bloqueio de alteração de e-mail vinculado a login
        Long idPessoa = usuarioRepository.dados.getFirst().getPessoa().getId();
        assertThrows(BadRequestException.class,
                () -> service.validarAlteracaoEmail(idPessoa, "novo.email@sga.com"));

        // Atualiza perfis e status ativo
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO(null, false, Set.of(4L));
        Response putResp = resource.update(1L, updateDTO);
        assertEquals(200, putResp.getStatus());
        UsuarioDTO atualizado = (UsuarioDTO) putResp.getEntity();
        assertFalse(atualizado.ativo());
        assertEquals(Set.of(Perfil.PSICOLOGO), atualizado.perfis());
    }

    static class FakeUsuarioRepository extends UsuarioRepository {
        final List<Usuario> dados = new ArrayList<>();
        long seq = 1L;

        @Override
        public void persist(Usuario entity) {
            entity.setId(seq++);
            if (entity.getPessoa().getId() == null) {
                entity.getPessoa().setId(seq * 10);
            }
            dados.add(entity);
        }

        @Override
        public Optional<Usuario> findByIdWithPessoa(Long id) {
            return dados.stream().filter(u -> id.equals(u.getId())).findFirst();
        }

        @Override
        public Optional<Usuario> findByLogin(String login) {
            return dados.stream().filter(u -> u.getLogin().equalsIgnoreCase(login.trim())).findFirst();
        }

        @Override
        public Optional<Usuario> findByPessoaId(Long pessoaId) {
            return dados.stream().filter(u -> pessoaId.equals(u.getPessoa().getId())).findFirst();
        }
    }

    static class FakePessoaRepository extends PessoaRepository {
        @Override
        public Optional<Pessoa> findByCpf(String cpf) {
            return Optional.empty();
        }

        @Override
        public Optional<Pessoa> findByEmailExceptId(String email, Long idPessoa) {
            return Optional.empty();
        }
    }
}
