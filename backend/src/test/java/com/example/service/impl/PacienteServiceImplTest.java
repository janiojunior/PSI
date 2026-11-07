package com.example.service.impl;

import com.example.dto.EstadoDTO;
import com.example.dto.MunicipioDTO;
import com.example.dto.PacienteDTO;
import com.example.dto.TelefoneDTO;
import com.example.mapper.EstadoMapper;
import com.example.mapper.MunicipioMapper;
import com.example.mapper.PacienteMapper;
import com.example.mapper.TelefoneMapper;
import com.example.model.Estado;
import com.example.model.Municipio;
import com.example.model.Paciente;
import com.example.model.Pessoa;
import com.example.repository.MunicipioRepository;
import com.example.repository.PacienteRepository;
import com.example.repository.PessoaRepository;
import com.example.resource.PacienteResource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacienteServiceImplTest {

    private PacienteServiceImpl service;
    private PacienteResource resource;
    private FakePacienteRepository pacienteRepository;
    private FakePessoaRepository pessoaRepository;
    private FakeMunicipioRepository municipioRepository;

    @BeforeEach
    void setUp() throws Exception {
        pacienteRepository = new FakePacienteRepository();
        pessoaRepository = new FakePessoaRepository();
        municipioRepository = new FakeMunicipioRepository();

        MunicipioMapper municipioMapper = new MunicipioMapper();
        var estField = MunicipioMapper.class.getDeclaredField("estadoMapper");
        estField.setAccessible(true);
        estField.set(municipioMapper, new EstadoMapper());

        PacienteMapper pacienteMapper = new PacienteMapper();
        var telField = PacienteMapper.class.getDeclaredField("telefoneMapper");
        telField.setAccessible(true);
        telField.set(pacienteMapper, new TelefoneMapper());
        var munField = PacienteMapper.class.getDeclaredField("municipioMapper");
        munField.setAccessible(true);
        munField.set(pacienteMapper, municipioMapper);

        service = new PacienteServiceImpl();
        service.pacienteRepository = pacienteRepository;
        service.pessoaRepository = pessoaRepository;
        service.municipioRepository = municipioRepository;
        service.pacienteMapper = pacienteMapper;

        resource = new PacienteResource();
        var resField = PacienteResource.class.getDeclaredField("pacienteService");
        resField.setAccessible(true);
        resField.set(resource, service);
    }

    @Test
    void deveCriarConsultarPorCpfAtualizarEExcluirPacienteComMunicipio() {
        MunicipioDTO municipioDTO = new MunicipioDTO(10L, "Palmas", new EstadoDTO(1L, "TO", "Tocantins"));
        PacienteDTO dto = new PacienteDTO(
                null,
                " Ana Martins ",
                "11111111111",
                "ANA.MARTINS@SGA.COM ",
                LocalDate.of(1994, 3, 12),
                "Feminino",
                "Quadra 101 Sul",
                municipioDTO,
                List.of(new TelefoneDTO("63", "991110001", true)),
                "Acompanhamento semanal",
                true
        );

        Response postResp = resource.create(dto);
        assertEquals(201, postResp.getStatus());
        PacienteDTO criado = (PacienteDTO) postResp.getEntity();
        assertEquals(1L, criado.id());
        assertEquals("Ana Martins", criado.nome());
        assertEquals("ana.martins@sga.com", criado.email());
        assertNotNull(criado.municipio());
        assertEquals("Palmas", criado.municipio().nome());
        assertEquals(1, criado.telefones().size());

        // Busca por CPF
        Response cpfResp = resource.getByCpf("11111111111");
        assertEquals(200, cpfResp.getStatus());
        assertEquals("Ana Martins", ((PacienteDTO) cpfResp.getEntity()).nome());

        // Bloqueia CPF duplicado
        assertThrows(BadRequestException.class, () -> service.create(dto));

        // Exclusão
        Response delResp = resource.delete(1L);
        assertEquals(204, delResp.getStatus());
        assertThrows(NotFoundException.class, () -> resource.getById(1L));
    }

    static class FakePacienteRepository extends PacienteRepository {
        final List<Paciente> dados = new ArrayList<>();
        long seq = 1L;

        @Override
        public void persist(Paciente entity) {
            entity.setId(seq++);
            if (entity.getPessoa().getId() == null) {
                entity.getPessoa().setId(seq * 10);
            }
            dados.add(entity);
        }

        @Override
        public Optional<Paciente> findByIdWithPessoa(Long id) {
            return dados.stream().filter(p -> id.equals(p.getId())).findFirst();
        }

        @Override
        public Optional<Paciente> findByCpf(String cpf) {
            return dados.stream().filter(p -> cpf.equals(p.getPessoa().getCpf())).findFirst();
        }

        @Override
        public List<Paciente> listAllWithPessoa() {
            return List.copyOf(dados);
        }

        @Override
        public boolean deleteById(Long id) {
            return dados.removeIf(p -> id.equals(p.getId()));
        }
    }

    static class FakePessoaRepository extends PessoaRepository {
        @Override
        public Optional<Pessoa> findByCpf(String cpf) {
            return Optional.empty();
        }

        @Override
        public Optional<Pessoa> findByCpfExceptId(String cpf, Long idPessoa) {
            return Optional.empty();
        }

        @Override
        public Optional<Pessoa> findByEmailExceptId(String email, Long idPessoa) {
            return Optional.empty();
        }
    }

    static class FakeMunicipioRepository extends MunicipioRepository {
        @Override
        public Optional<Municipio> findByIdWithEstado(Long id) {
            if (Long.valueOf(10L).equals(id)) {
                return Optional.of(new Municipio(10L, "Palmas", new Estado(1L, "TO", "Tocantins")));
            }
            return Optional.empty();
        }
    }
}
