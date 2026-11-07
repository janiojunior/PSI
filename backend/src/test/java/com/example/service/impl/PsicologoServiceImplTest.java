package com.example.service.impl;

import com.example.dto.EstadoDTO;
import com.example.dto.MunicipioDTO;
import com.example.dto.PsicologoDTO;
import com.example.dto.TelefoneDTO;
import com.example.mapper.EstadoMapper;
import com.example.mapper.MunicipioMapper;
import com.example.mapper.PsicologoMapper;
import com.example.mapper.TelefoneMapper;
import com.example.model.Estado;
import com.example.model.Municipio;
import com.example.model.Pessoa;
import com.example.model.Psicologo;
import com.example.repository.MunicipioRepository;
import com.example.repository.PessoaRepository;
import com.example.repository.PsicologoRepository;
import com.example.resource.PsicologoResource;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PsicologoServiceImplTest {

    private PsicologoServiceImpl service;
    private PsicologoResource resource;
    private FakePsicologoRepository psicologoRepository;

    @BeforeEach
    void setUp() throws Exception {
        psicologoRepository = new FakePsicologoRepository();

        MunicipioMapper municipioMapper = new MunicipioMapper();
        var estField = MunicipioMapper.class.getDeclaredField("estadoMapper");
        estField.setAccessible(true);
        estField.set(municipioMapper, new EstadoMapper());

        PsicologoMapper psicologoMapper = new PsicologoMapper();
        var telField = PsicologoMapper.class.getDeclaredField("telefoneMapper");
        telField.setAccessible(true);
        telField.set(psicologoMapper, new TelefoneMapper());
        var munField = PsicologoMapper.class.getDeclaredField("municipioMapper");
        munField.setAccessible(true);
        munField.set(psicologoMapper, municipioMapper);

        service = new PsicologoServiceImpl();
        service.psicologoRepository = psicologoRepository;
        service.pessoaRepository = new FakePessoaRepository();
        service.municipioRepository = new FakeMunicipioRepository();
        service.psicologoMapper = psicologoMapper;

        resource = new PsicologoResource();
        var resField = PsicologoResource.class.getDeclaredField("psicologoService");
        resField.setAccessible(true);
        resField.set(resource, service);
    }

    @Test
    void deveCriarConsultarPorCrpEExcluirPsicologo() {
        PsicologoDTO dto = new PsicologoDTO(
                null,
                " Isabela Freitas ",
                "99999999999",
                "isabela.freitas@sga.com",
                LocalDate.of(1998, 8, 30),
                "Feminino",
                "Taquaral",
                new MunicipioDTO(10L, "Palmas", new EstadoDTO(1L, "TO", "Tocantins")),
                List.of(new TelefoneDTO("19", "994440009", false)),
                " crp-01/10001 ",
                "Terapia Cognitivo-Comportamental",
                "Profissional com foco em ansiedade.",
                new BigDecimal("180.00"),
                50,
                true
        );

        Response postResp = resource.create(dto);
        assertEquals(201, postResp.getStatus());
        PsicologoDTO criado = (PsicologoDTO) postResp.getEntity();
        assertEquals(1L, criado.id());
        assertEquals("CRP-01/10001", criado.crp());

        // Busca por CRP
        Response crpResp = resource.getByCrp("crp-01/10001");
        assertEquals(200, crpResp.getStatus());
        assertEquals("Isabela Freitas", ((PsicologoDTO) crpResp.getEntity()).nome());

        // Bloqueia CRP duplicado
        PsicologoDTO outroComMesmoCrp = new PsicologoDTO(
                null, "Outro", "88888888888", "outro@sga.com",
                LocalDate.of(1990, 1, 1), null, null, null,
                List.of(new TelefoneDTO("11", "999999999", true)),
                "CRP-01/10001", "Psicologia Clínica", null,
                new BigDecimal("150.00"), 50, true
        );
        assertThrows(BadRequestException.class, () -> service.create(outroComMesmoCrp));

        // Exclusão
        assertEquals(204, resource.delete(1L).getStatus());
        assertThrows(NotFoundException.class, () -> resource.getById(1L));
    }

    static class FakePsicologoRepository extends PsicologoRepository {
        final List<Psicologo> dados = new ArrayList<>();
        long seq = 1L;

        @Override
        public void persist(Psicologo entity) {
            entity.setId(seq++);
            if (entity.getPessoa().getId() == null) {
                entity.getPessoa().setId(seq * 10);
            }
            dados.add(entity);
        }

        @Override
        public Optional<Psicologo> findByIdWithPessoa(Long id) {
            return dados.stream().filter(p -> id.equals(p.getId())).findFirst();
        }

        @Override
        public Optional<Psicologo> findByCpf(String cpf) {
            return dados.stream().filter(p -> cpf.equals(p.getPessoa().getCpf())).findFirst();
        }

        @Override
        public Optional<Psicologo> findByCrp(String crp) {
            return dados.stream().filter(p -> p.getCrp().equalsIgnoreCase(crp.trim())).findFirst();
        }

        @Override
        public Optional<Psicologo> findByCrpExceptId(String crp, Long id) {
            return dados.stream()
                    .filter(p -> p.getCrp().equalsIgnoreCase(crp.trim()) && !p.getId().equals(id))
                    .findFirst();
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
            return Optional.of(new Municipio(id, "Palmas", new Estado(1L, "TO", "Tocantins")));
        }
    }
}
