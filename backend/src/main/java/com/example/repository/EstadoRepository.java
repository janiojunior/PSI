package com.example.repository;

import com.example.model.Estado;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EstadoRepository implements PanacheRepository<Estado> {

    public Optional<Estado> findBySigla(String sigla) {
        if (sigla == null) {
            return Optional.empty();
        }
        return find("UPPER(sigla) = UPPER(?1)", sigla.trim()).firstResultOptional();
    }

    public Optional<Estado> findBySiglaExceptId(String sigla, Long id) {
        if (sigla == null) {
            return Optional.empty();
        }
        if (id == null) {
            return findBySigla(sigla);
        }
        return find("UPPER(sigla) = UPPER(?1) AND id <> ?2", sigla.trim(), id).firstResultOptional();
    }

    public List<Estado> listPaginated(int page, int pageSize) {
        return find("ORDER BY nome").page(Page.of(page, pageSize)).list();
    }

    public List<Estado> findByNome(String nome, int page, int pageSize) {
        String filtro = "%" + (nome == null ? "" : nome.trim()) + "%";
        return find("LOWER(nome) LIKE LOWER(?1) ORDER BY nome", filtro)
                .page(Page.of(page, pageSize))
                .list();
    }

    public long countByNome(String nome) {
        String filtro = "%" + (nome == null ? "" : nome.trim()) + "%";
        return count("LOWER(nome) LIKE LOWER(?1)", filtro);
    }
}
