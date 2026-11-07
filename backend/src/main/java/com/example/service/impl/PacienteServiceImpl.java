package com.example.service.impl;

import com.example.dto.MunicipioDTO;
import com.example.dto.PacienteDTO;
import com.example.dto.PageResponse;
import com.example.mapper.PacienteMapper;
import com.example.model.Municipio;
import com.example.model.Paciente;
import com.example.model.Pessoa;
import com.example.repository.MunicipioRepository;
import com.example.repository.PacienteRepository;
import com.example.repository.PessoaRepository;
import com.example.service.PacienteService;
import com.example.service.UsuarioService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

@ApplicationScoped
public class PacienteServiceImpl implements PacienteService {

    @Inject
    PacienteRepository pacienteRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    UsuarioService usuarioService;

    @Inject
    PacienteMapper pacienteMapper;

    @Override
    public List<PacienteDTO> findAll() {
        return pacienteRepository.listAllWithPessoa().stream()
                .map(pacienteMapper::toDTO)
                .toList();
    }

    @Override
    public PageResponse<PacienteDTO> findPaginated(int page, int pageSize) {
        validarPaginacao(page, pageSize);
        List<Paciente> items = pacienteRepository.listPaginated(page, pageSize);
        long total = pacienteRepository.count();
        return PageResponse.of(items, page, pageSize, total, pacienteMapper::toDTO);
    }

    @Override
    public PageResponse<PacienteDTO> findByNome(String nome, int page, int pageSize) {
        validarPaginacao(page, pageSize);
        List<Paciente> items = pacienteRepository.findByNomePaginated(nome, page, pageSize);
        long total = pacienteRepository.countByNome(nome);
        return PageResponse.of(items, page, pageSize, total, pacienteMapper::toDTO);
    }

    @Override
    public PacienteDTO findById(Long id) {
        Paciente entity = pacienteRepository.findByIdWithPessoa(id)
                .orElseThrow(() -> new NotFoundException("Paciente com ID " + id + " não encontrado."));
        return pacienteMapper.toDTO(entity);
    }

    @Override
    public PacienteDTO findByCpf(String cpf) {
        return pacienteRepository.findByCpf(cpf)
                .map(pacienteMapper::toDTO)
                .or(() -> pessoaRepository.findByCpf(cpf).map(pacienteMapper::toDTOFromPessoa))
                .orElseThrow(() -> new NotFoundException("Paciente com CPF " + cpf + " não encontrado."));
    }

    @Override
    @Transactional
    public PacienteDTO create(PacienteDTO dto) {
        if (pacienteRepository.findByCpf(dto.cpf()).isPresent()) {
            throw new BadRequestException("Já existe um paciente cadastrado com esse CPF.");
        }

        Pessoa pessoaExistente = pessoaRepository.findByCpf(dto.cpf()).orElse(null);
        Long idPessoa = pessoaExistente != null ? pessoaExistente.getId() : null;

        validarDados(dto, idPessoa);

        Paciente novoPaciente = new Paciente();
        novoPaciente.setPessoa(pessoaExistente != null ? pessoaExistente : new Pessoa());
        pacienteMapper.updateEntityFromDTO(dto, novoPaciente);
        novoPaciente.getPessoa().setMunicipio(resolverMunicipioOpcional(dto.municipio()));

        pacienteRepository.persist(novoPaciente);
        return pacienteMapper.toDTO(novoPaciente);
    }

    @Override
    @Transactional
    public PacienteDTO update(Long id, PacienteDTO dto) {
        Paciente entity = pacienteRepository.findByIdWithPessoa(id)
                .orElseThrow(() -> new NotFoundException("Paciente com ID " + id + " não encontrado."));
        validarDados(dto, entity.getPessoa().getId());
        pacienteMapper.updateEntityFromDTO(dto, entity);
        entity.getPessoa().setMunicipio(resolverMunicipioOpcional(dto.municipio()));
        return pacienteMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        boolean deleted = pacienteRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Paciente com ID " + id + " não encontrado.");
        }
    }

    private void validarDados(PacienteDTO dto, Long idPessoa) {
        if (usuarioService != null) {
            usuarioService.validarAlteracaoEmail(idPessoa, dto.email());
        }
        if (pessoaRepository.findByCpfExceptId(dto.cpf(), idPessoa).isPresent()) {
            throw new BadRequestException("Já existe uma pessoa cadastrada com esse CPF.");
        }
        if (pessoaRepository.findByEmailExceptId(dto.email(), idPessoa).isPresent()) {
            throw new BadRequestException("Já existe uma pessoa cadastrada com esse e-mail.");
        }
    }

    private Municipio resolverMunicipioOpcional(MunicipioDTO municipioDTO) {
        if (municipioDTO == null || municipioDTO.id() == null || municipioRepository == null) {
            return null;
        }
        Long idMunicipio = municipioDTO.id();
        return municipioRepository.findByIdWithEstado(idMunicipio)
                .orElseThrow(() -> new NotFoundException("Município com ID " + idMunicipio + " não encontrado."));
    }

    private void validarPaginacao(int page, int pageSize) {
        if (page < 0 || pageSize < 1 || pageSize > 100) {
            throw new BadRequestException("Use página não negativa e tamanho entre 1 e 100.");
        }
    }
}
