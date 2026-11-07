package com.example.service.impl;

import com.example.dto.MunicipioDTO;
import com.example.dto.PageResponse;
import com.example.dto.UsuarioDTO;
import com.example.dto.UsuarioRequestDTO;
import com.example.dto.UsuarioUpdateDTO;
import com.example.mapper.UsuarioMapper;
import com.example.model.Municipio;
import com.example.model.Perfil;
import com.example.model.Pessoa;
import com.example.model.Usuario;
import com.example.repository.MunicipioRepository;
import com.example.repository.PessoaRepository;
import com.example.repository.UsuarioRepository;
import com.example.service.SenhaService;
import com.example.service.UsuarioService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    UsuarioMapper usuarioMapper;

    @Inject
    SenhaService senhaService;

    @Override
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.listAllWithPessoa().stream()
                .map(usuarioMapper::toDTO)
                .toList();
    }

    @Override
    public PageResponse<UsuarioDTO> findPaginated(int page, int pageSize) {
        validarPaginacao(page, pageSize);
        List<Usuario> items = usuarioRepository.listPaginated(page, pageSize);
        long total = usuarioRepository.count();
        return PageResponse.of(items, page, pageSize, total, usuarioMapper::toDTO);
    }

    @Override
    public PageResponse<UsuarioDTO> findByNome(String nome, int page, int pageSize) {
        validarPaginacao(page, pageSize);
        List<Usuario> items = usuarioRepository.findByNomePaginated(nome, page, pageSize);
        long total = usuarioRepository.countByNome(nome);
        return PageResponse.of(items, page, pageSize, total, usuarioMapper::toDTO);
    }

    @Override
    public UsuarioDTO findById(Long id) {
        Usuario entity = usuarioRepository.findByIdWithPessoa(id)
                .orElseThrow(() -> new NotFoundException("Usuário com ID " + id + " não encontrado."));
        return usuarioMapper.toDTO(entity);
    }

    @Override
    public UsuarioDTO findByCpf(String cpf) {
        Usuario entity = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new NotFoundException("Usuário com CPF " + cpf + " não encontrado."));
        return usuarioMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public UsuarioDTO create(UsuarioRequestDTO dto) {
        Pessoa pessoa = pessoaRepository.findByCpf(dto.cpf()).orElse(null);
        Long pessoaId = pessoa != null ? pessoa.getId() : null;

        if (pessoa != null && usuarioRepository.findByPessoaId(pessoa.getId()).isPresent()) {
            throw new BadRequestException("Essa pessoa já possui um usuário.");
        }

        if (pessoaRepository.findByEmailExceptId(dto.email(), pessoaId).isPresent()) {
            throw new BadRequestException("Já existe uma pessoa cadastrada com esse e-mail.");
        }

        if (pessoa == null) {
            pessoa = new Pessoa();
        }

        usuarioMapper.updatePessoaFromRequest(dto, pessoa);
        pessoa.setMunicipio(resolverMunicipioOpcional(dto.municipio()));

        String email = pessoa.getEmail();
        if (email == null || email.isBlank() || email.length() > 120) {
            throw new BadRequestException("A pessoa deve possuir um e-mail válido de até 120 caracteres.");
        }
        if (usuarioRepository.findByLogin(email).isPresent()) {
            throw new BadRequestException("Já existe um usuário com o e-mail dessa pessoa.");
        }

        Set<Perfil> perfis = validarPerfis(dto.perfis());

        Usuario usuario = new Usuario();
        usuario.setPessoa(pessoa);
        usuario.setLogin(email);
        usuario.setSenha(senhaService.gerarHash(email, dto.senha()));
        usuario.setPerfis(perfis);
        usuario.setAtivo(true);

        usuarioRepository.persist(usuario);
        return usuarioMapper.toDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO update(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findByIdWithPessoa(id)
                .orElseThrow(() -> new NotFoundException("Usuário com ID " + id + " não encontrado."));
        Set<Perfil> perfis = validarPerfis(dto.perfis());
        if (dto.senha() != null) {
            if (dto.senha().isBlank()) {
                throw new BadRequestException("A senha não pode conter apenas espaços.");
            }
            usuario.setSenha(senhaService.gerarHash(usuario.getPessoa().getEmail(), dto.senha()));
        }
        usuario.setAtivo(dto.ativo());
        usuario.setPerfis(perfis);
        return usuarioMapper.toDTO(usuario);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        boolean deleted = usuarioRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Usuário com ID " + id + " não encontrado.");
        }
    }

    @Override
    public void validarAlteracaoEmail(Long pessoaId, String email) {
        if (pessoaId == null || email == null) {
            return;
        }
        usuarioRepository.findByPessoaId(pessoaId).ifPresent(usuario -> {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (!usuario.getLogin().equalsIgnoreCase(normalizedEmail)) {
                throw new BadRequestException(
                        "O e-mail está vinculado ao login. A alteração exige um fluxo de troca de credenciais."
                );
            }
        });
    }

    private Set<Perfil> validarPerfis(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("Ao menos um perfil deve ser informado.");
        }
        Set<Perfil> perfis = new HashSet<>();
        for (Long id : ids) {
            Perfil perfil = Perfil.valueOf(id);
            if (perfil == null) {
                throw new BadRequestException("Perfil inválido: " + id + ".");
            }
            perfis.add(perfil);
        }
        return perfis;
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
