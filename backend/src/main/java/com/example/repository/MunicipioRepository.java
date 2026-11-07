package com.example.repository;

import com.example.model.Municipio;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MunicipioRepository implements PanacheRepository<Municipio> {

    public List<Municipio> listAllWithEstado() {
        return find("SELECT m FROM Municipio m JOIN FETCH m.estado ORDER BY m.nome").list();
    }

    public List<Municipio> listPaginated(int page, int pageSize) {
        return find("SELECT m FROM Municipio m JOIN FETCH m.estado ORDER BY m.nome")
                .page(Page.of(page, pageSize))
                .list();
    }

    public Optional<Municipio> findByIdWithEstado(Long id) {
        return find("SELECT m FROM Municipio m JOIN FETCH m.estado WHERE m.id = ?1", id)
                .firstResultOptional();
    }

    public List<Municipio> findByNome(String nome) {
        return find(
                "SELECT m FROM Municipio m JOIN FETCH m.estado WHERE LOWER(m.nome) LIKE LOWER(?1) ORDER BY m.nome",
                "%" + (nome == null ? "" : nome.trim()) + "%"
        ).list();
    }

    public List<Municipio> findByNomePaginated(String nome, int page, int pageSize) {
        return find(
                "SELECT m FROM Municipio m JOIN FETCH m.estado WHERE LOWER(m.nome) LIKE LOWER(?1) ORDER BY m.nome",
                "%" + (nome == null ? "" : nome.trim()) + "%"
        ).page(Page.of(page, pageSize)).list();
    }

    public long countByNome(String nome) {
        return count("LOWER(nome) LIKE LOWER(?1)", "%" + (nome == null ? "" : nome.trim()) + "%");
    }
}
