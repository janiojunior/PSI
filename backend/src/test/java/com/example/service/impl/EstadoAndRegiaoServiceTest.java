package com.example.service.impl;

import com.example.dto.EstadoDTO;
import com.example.dto.PageResponse;
import com.example.mapper.EstadoMapper;
import com.example.model.Estado;
import com.example.model.Regiao;
import com.example.repository.EstadoRepository;
import com.example.resource.EstadoResource;
import com.example.resource.RegiaoResource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstadoAndRegiaoServiceTest {

    private EstadoServiceImpl estadoService;
    private EstadoResource estadoResource;
    private RegiaoServiceImpl regiaoService;
    private RegiaoResource regiaoResource;
    private FakeEstadoRepository estadoRepository;

    @BeforeEach
    void setUp() throws Exception {
        estadoRepository = new FakeEstadoRepository();
        EstadoMapper estadoMapper = new EstadoMapper();

        estadoService = new EstadoServiceImpl();
        estadoService.estadoRepository = estadoRepository;
        estadoService.estadoMapper = estadoMapper;

        estadoResource = new EstadoResource();
        var estField = EstadoResource.class.getDeclaredField("estadoService");
        estField.setAccessible(true);
        estField.set(estadoResource, estadoService);

        regiaoService = new RegiaoServiceImpl();
        regiaoResource = new RegiaoResource();
        var regField = RegiaoResource.class.getDeclaredField("regiaoService");
        regField.setAccessible(true);
        regField.set(regiaoResource, regiaoService);
    }

    @Test
    void deveCriarConsultarPaginarAtualizarEExcluirEstadoComRegiao() {
        EstadoDTO entrada = new EstadoDTO(null, " to ", " Tocantins ", Regiao.NORTE);
        Response postResp = estadoResource.create(entrada);
        assertEquals(201, postResp.getStatus());
        EstadoDTO criado = (EstadoDTO) postResp.getEntity();
        assertEquals(1L, criado.id());
        assertEquals("TO", criado.sigla());
        assertEquals("Tocantins", criado.nome());
        assertEquals(Regiao.NORTE, criado.regiao());

        // Busca por sigla
        Response siglaResp = estadoResource.getBySigla("to");
        assertEquals(200, siglaResp.getStatus());
        assertEquals("Tocantins", ((EstadoDTO) siglaResp.getEntity()).nome());

        // Busca paginada e por nome
        Response pageResp = estadoResource.getPaginated(0, 10);
        assertEquals(200, pageResp.getStatus());
        PageResponse<?> page = (PageResponse<?>) pageResp.getEntity();
        assertEquals(1, page.totalItems());

        Response nomeResp = estadoResource.getByNome("Toc", 0, 10);
        assertEquals(200, nomeResp.getStatus());
        assertEquals(1, ((PageResponse<?>) nomeResp.getEntity()).items().size());

        // Bloqueia sigla duplicada em novo cadastro
        assertThrows(BadRequestException.class,
                () -> estadoService.create(new EstadoDTO(null, "TO", "Outro", Regiao.NORTE)));

        // Atualização
        EstadoDTO atualizacao = new EstadoDTO(1L, "TO", "Estado do Tocantins", Regiao.NORTE);
        Response putResp = estadoResource.update(1L, atualizacao);
        assertEquals(200, putResp.getStatus());
        assertEquals("Estado do Tocantins", ((EstadoDTO) putResp.getEntity()).nome());

        // Exclusão
        Response delResp = estadoResource.delete(1L);
        assertEquals(204, delResp.getStatus());
        assertThrows(NotFoundException.class, () -> estadoResource.getById(1L));
    }

    @Test
    void deveListarEBuscarRegioesPorId() {
        Response listResp = regiaoResource.getAll();
        assertEquals(200, listResp.getStatus());
        assertEquals(5, ((List<?>) listResp.getEntity()).size());

        Response getResp = regiaoResource.getById(2L);
        assertEquals(200, getResp.getStatus());
        assertEquals(Regiao.NORTE, getResp.getEntity());

        assertThrows(NotFoundException.class, () -> regiaoResource.getById(99L));
    }

    static class FakeEstadoRepository extends EstadoRepository {
        private final List<Estado> dados = new ArrayList<>();
        private long seq = 1L;

        @Override
        public void persist(Estado entity) {
            entity.setId(seq++);
            dados.add(entity);
        }

        @Override
        public List<Estado> listAll() {
            return List.copyOf(dados);
        }

        @Override
        public List<Estado> listPaginated(int page, int pageSize) {
            return List.copyOf(dados);
        }

        @Override
        public long count() {
            return dados.size();
        }

        @Override
        public List<Estado> findByNome(String nome, int page, int pageSize) {
            return dados.stream()
                    .filter(e -> e.getNome().toLowerCase().contains(nome.toLowerCase()))
                    .toList();
        }

        @Override
        public long countByNome(String nome) {
            return findByNome(nome, 0, 10).size();
        }

        @Override
        public Optional<Estado> findByIdOptional(Long id) {
            return dados.stream().filter(e -> id.equals(e.getId())).findFirst();
        }

        @Override
        public Optional<Estado> findBySigla(String sigla) {
            return dados.stream().filter(e -> e.getSigla().equalsIgnoreCase(sigla.trim())).findFirst();
        }

        @Override
        public Optional<Estado> findBySiglaExceptId(String sigla, Long id) {
            return dados.stream()
                    .filter(e -> e.getSigla().equalsIgnoreCase(sigla.trim()) && !e.getId().equals(id))
                    .findFirst();
        }

        @Override
        public boolean deleteById(Long id) {
            return dados.removeIf(e -> id.equals(e.getId()));
        }
    }
}
