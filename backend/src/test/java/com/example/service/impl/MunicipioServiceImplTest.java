package com.example.service.impl;

import com.example.dto.EstadoDTO;
import com.example.dto.MunicipioDTO;
import com.example.mapper.EstadoMapper;
import com.example.mapper.MunicipioMapper;
import com.example.model.Estado;
import com.example.model.Municipio;
import com.example.repository.EstadoRepository;
import com.example.repository.MunicipioRepository;
import com.example.resource.MunicipioResource;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MunicipioServiceImplTest {

    private MunicipioServiceImpl service;
    private MunicipioResource resource;
    private FakeMunicipioRepository municipioRepository;
    private FakeEstadoRepository estadoRepository;
    private Validator validator;

    @BeforeEach
    void setUp() {
        municipioRepository = new FakeMunicipioRepository();
        estadoRepository = new FakeEstadoRepository();
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        EstadoMapper estadoMapper = new EstadoMapper();
        MunicipioMapper municipioMapper = new MunicipioMapper();
        try {
            var field = MunicipioMapper.class.getDeclaredField("estadoMapper");
            field.setAccessible(true);
            field.set(municipioMapper, estadoMapper);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        service = new MunicipioServiceImpl();
        service.municipioRepository = municipioRepository;
        service.estadoRepository = estadoRepository;
        service.municipioMapper = municipioMapper;

        resource = new MunicipioResource();
        try {
            var resField = MunicipioResource.class.getDeclaredField("municipioService");
            resField.setAccessible(true);
            resField.set(resource, service);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deveCriarConsultarAtualizarEExcluirMunicipioViaResourceEService() {
        Estado tocantins = new Estado(1L, "TO", "Tocantins");
        Estado goias = new Estado(2L, "GO", "Goiás");
        estadoRepository.estados = List.of(tocantins, goias);

        // 1. POST (Criação com sanitização de espaços)
        MunicipioDTO entrada = new MunicipioDTO(null, "  Palmas  ", new EstadoDTO(1L, "TO", "Tocantins"));
        Response postResp = resource.create(entrada);
        assertEquals(201, postResp.getStatus());
        assertEquals("/municipios/100", postResp.getLocation().getPath());
        MunicipioDTO criado = (MunicipioDTO) postResp.getEntity();
        assertEquals(100L, criado.id());
        assertEquals("Palmas", criado.nome());
        assertEquals("TO", criado.estado().sigla());

        // 2. GET por ID e Listagem
        Response getResp = resource.getById(100L);
        assertEquals(200, getResp.getStatus());
        assertEquals("Palmas", ((MunicipioDTO) getResp.getEntity()).nome());

        Response listResp = resource.getAll();
        assertEquals(200, listResp.getStatus());
        assertEquals(1, ((List<?>) listResp.getEntity()).size());

        // 3. PUT (Atualização de nome e troca de Estado)
        MunicipioDTO edicao = new MunicipioDTO(100L, " Goiânia ", new EstadoDTO(2L, "GO", "Goiás"));
        Response putResp = resource.update(100L, edicao);
        assertEquals(200, putResp.getStatus());
        MunicipioDTO atualizado = (MunicipioDTO) putResp.getEntity();
        assertEquals("Goiânia", atualizado.nome());
        assertEquals("GO", atualizado.estado().sigla());

        // 4. DELETE (Exclusão)
        Response delResp = resource.delete(100L);
        assertEquals(204, delResp.getStatus());

        // 5. Confirma 404 após exclusão
        assertThrows(NotFoundException.class, () -> resource.getById(100L));
        assertThrows(NotFoundException.class, () -> resource.delete(100L));
    }

    @Test
    void deveLancarNotFoundQuandoEstadoNaoExistirNaCriacao() {
        estadoRepository.estados = List.of();
        MunicipioDTO entrada = new MunicipioDTO(null, "Palmas", new EstadoDTO(99L, "XX", "Inexistente"));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(entrada));
        assertEquals("Estado com ID 99 não encontrado.", ex.getMessage());
    }

    @Test
    void deveLancarBadRequestQuandoIdDoEstadoForNulo() {
        MunicipioDTO entrada = new MunicipioDTO(null, "Palmas", new EstadoDTO(null, "TO", "Tocantins"));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(entrada));
        assertEquals("O ID do estado é obrigatório.", ex.getMessage());
    }

    @Test
    void deveValidarRegrasBeanValidationDoMunicipioDTO() {
        MunicipioDTO invalidoSemNomeESemEstado = new MunicipioDTO(null, " ", null);
        var violacoes = validator.validate(invalidoSemNomeESemEstado);
        assertFalse(violacoes.isEmpty());

        MunicipioDTO invalidoNomeCurto = new MunicipioDTO(null, "A", new EstadoDTO(1L, "TO", "Tocantins"));
        var violacoesTamanho = validator.validate(invalidoNomeCurto);
        assertEquals(1, violacoesTamanho.size());
        assertEquals("O nome deve conter entre 2 e 60 caracteres", violacoesTamanho.iterator().next().getMessage());
    }

    static class FakeMunicipioRepository extends MunicipioRepository {
        Municipio persistido;

        @Override
        public void persist(Municipio entity) {
            entity.setId(100L);
            this.persistido = entity;
        }

        @Override
        public Optional<Municipio> findByIdWithEstado(Long id) {
            if (persistido != null && id.equals(persistido.getId())) {
                return Optional.of(persistido);
            }
            return Optional.empty();
        }

        @Override
        public List<Municipio> listAllWithEstado() {
            return persistido != null ? List.of(persistido) : List.of();
        }

        @Override
        public boolean deleteById(Long id) {
            if (persistido != null && id.equals(persistido.getId())) {
                persistido = null;
                return true;
            }
            return false;
        }
    }

    static class FakeEstadoRepository extends EstadoRepository {
        List<Estado> estados = List.of();

        @Override
        public Optional<Estado> findByIdOptional(Long id) {
            return estados.stream().filter(e -> id.equals(e.getId())).findFirst();
        }
    }
}
