package com.example.repository;

import com.example.model.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public List<Usuario> listAllWithPessoa() {
        return find(
                "SELECT u FROM Usuario u JOIN FETCH u.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado ORDER BY p.nome, p.id"
        ).list();
    }

    public List<Usuario> listPaginated(int page, int pageSize) {
        return find(
                "SELECT u FROM Usuario u JOIN FETCH u.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado ORDER BY p.nome, p.id"
        ).page(Page.of(page, pageSize)).list();
    }

    public List<Usuario> findByNomePaginated(String nome, int page, int pageSize) {
        String filtro = "%" + (nome == null ? "" : nome.trim()) + "%";
        return find(
                "SELECT u FROM Usuario u JOIN FETCH u.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE LOWER(p.nome) LIKE LOWER(?1) ORDER BY p.nome, p.id",
                filtro
        ).page(Page.of(page, pageSize)).list();
    }

    public long countByNome(String nome) {
        String filtro = "%" + (nome == null ? "" : nome.trim()) + "%";
        return count("LOWER(pessoa.nome) LIKE LOWER(?1)", filtro);
    }

    public Optional<Usuario> findByIdWithPessoa(Long id) {
        return find(
                "SELECT u FROM Usuario u JOIN FETCH u.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE u.id = ?1",
                id
        ).firstResultOptional();
    }

    public Optional<Usuario> findByLogin(String login) {
        if (login == null) {
            return Optional.empty();
        }
        return find(
                "SELECT u FROM Usuario u JOIN FETCH u.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE LOWER(u.login) = LOWER(?1)",
                login.trim()
        ).firstResultOptional();
    }

    public Optional<Usuario> findByPessoaId(Long pessoaId) {
        if (pessoaId == null) {
            return Optional.empty();
        }
        return find("pessoa.id = ?1", pessoaId).firstResultOptional();
    }

    public Optional<Usuario> findByCpf(String cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        return find(
                "SELECT u FROM Usuario u JOIN FETCH u.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE p.cpf = ?1",
                cpf.replaceAll("\\D", "").trim()
        ).firstResultOptional();
    }
}
