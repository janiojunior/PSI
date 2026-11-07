package com.example.service.impl;

import com.example.dto.MunicipioDTO;
import com.example.dto.PageResponse;
import com.example.dto.PsicologoDTO;
import com.example.mapper.PsicologoMapper;
import com.example.model.Municipio;
import com.example.model.Pessoa;
import com.example.model.Psicologo;
import com.example.repository.MunicipioRepository;
import com.example.repository.PessoaRepository;
import com.example.repository.PsicologoRepository;
import com.example.service.PsicologoService;
import com.example.service.UsuarioService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

@ApplicationScoped
public class PsicologoServiceImpl implements PsicologoService {

    @Inject
    PsicologoRepository psicologoRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    UsuarioService usuarioService;

    @Inject
    PsicologoMapper psicologoMapper;

    @Override
    public List<PsicologoDTO> findAll() {
        return psicologoRepository.listAllWithPessoa().stream()
                .map(psicologoMapper::toDTO)
                .toList();
    }

    @Override
    public PageResponse<PsicologoDTO> findPaginated(int page, int pageSize) {
        validarPaginacao(page, pageSize);
        List<Psicologo> items = psicologoRepository.listPaginated(page, pageSize);
        long total = psicologoRepository.count();
        return PageResponse.of(items, page, pageSize, total, psicologoMapper::toDTO);
    }

    @Override
    public PageResponse<PsicologoDTO> findByNome(String nome, int page, int pageSize) {
        validarPaginacao(page, pageSize);
        List<Psicologo> items = psicologoRepository.findByNomePaginated(nome, page, pageSize);
        long total = psicologoRepository.countByNome(nome);
        return PageResponse.of(items, page, pageSize, total, psicologoMapper::toDTO);
    }

    @Override
    public PsicologoDTO findById(Long id) {
        Psicologo entity = psicologoRepository.findByIdWithPessoa(id)
                .orElseThrow(() -> new NotFoundException("Psicólogo com ID " + id + " não encontrado."));
        return psicologoMapper.toDTO(entity);
    }

    @Override
    public PsicologoDTO findByCpf(String cpf) {
        return psicologoRepository.findByCpf(cpf)
                .map(psicologoMapper::toDTO)
                .or(() -> pessoaRepository.findByCpf(cpf).map(psicologoMapper::toDTOFromPessoa))
                .orElseThrow(() -> new NotFoundException("Psicólogo com CPF " + cpf + " não encontrado."));
    }

    @Override
    public PsicologoDTO findByCrp(String crp) {
        Psicologo entity = psicologoRepository.findByCrp(crp)
                .orElseThrow(() -> new NotFoundException("Psicólogo com CRP " + crp + " não encontrado."));
        return psicologoMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public PsicologoDTO create(PsicologoDTO dto) {
        if (psicologoRepository.findByCpf(dto.cpf()).isPresent()) {
            throw new BadRequestException("Já existe um psicólogo cadastrado com esse CPF.");
        }

        Pessoa pessoaExistente = pessoaRepository.findByCpf(dto.cpf()).orElse(null);
        Long idPessoa = pessoaExistente != null ? pessoaExistente.getId() : null;

        validarDados(dto, null, idPessoa);

        Psicologo novoPsicologo = new Psicologo();
        novoPsicologo.setPessoa(pessoaExistente != null ? pessoaExistente : new Pessoa());
        psicologoMapper.updateEntityFromDTO(dto, novoPsicologo);
        novoPsicologo.getPessoa().setMunicipio(resolverMunicipioOpcional(dto.municipio()));

        psicologoRepository.persist(novoPsicologo);
        return psicologoMapper.toDTO(novoPsicologo);
    }

    @Override
    @Transactional
    public PsicologoDTO update(Long id, PsicologoDTO dto) {
        Psicologo entity = psicologoRepository.findByIdWithPessoa(id)
                .orElseThrow(() -> new NotFoundException("Psicólogo com ID " + id + " não encontrado."));
        validarDados(dto, id, entity.getPessoa().getId());
        psicologoMapper.updateEntityFromDTO(dto, entity);
        entity.getPessoa().setMunicipio(resolverMunicipioOpcional(dto.municipio()));
        return psicologoMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        boolean deleted = psicologoRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Psicólogo com ID " + id + " não encontrado.");
        }
    }

    private void validarDados(PsicologoDTO dto, Long idPsicologo, Long idPessoa) {
        if (usuarioService != null) {
            usuarioService.validarAlteracaoEmail(idPessoa, dto.email());
        }
        if (pessoaRepository.findByCpfExceptId(dto.cpf(), idPessoa).isPresent()) {
            throw new BadRequestException("Já existe uma pessoa cadastrada com esse CPF.");
        }
        if (pessoaRepository.findByEmailExceptId(dto.email(), idPessoa).isPresent()) {
            throw new BadRequestException("Já existe uma pessoa cadastrada com esse e-mail.");
        }
        if (psicologoRepository.findByCrpExceptId(dto.crp(), idPsicologo).isPresent()) {
            throw new BadRequestException("Já existe um psicólogo cadastrado com esse CRP.");
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
