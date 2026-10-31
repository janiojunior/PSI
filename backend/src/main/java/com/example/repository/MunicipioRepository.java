package com.example.repository;

import com.example.model.Municipio;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MunicipioRepository implements PanacheRepository<Municipio> {

    public List<Municipio> listAllWithEstado() {
        return find("SELECT m FROM Municipio m JOIN FETCH m.estado ORDER BY m.nome").list();
    }

    public Optional<Municipio> findByIdWithEstado(Long id) {
        return find("SELECT m FROM Municipio m JOIN FETCH m.estado WHERE m.id = ?1", id)
                .firstResultOptional();
    }

    public List<Municipio> findByNome(String nome) {
        return find(
                "SELECT m FROM Municipio m JOIN FETCH m.estado WHERE LOWER(m.nome) LIKE LOWER(?1) ORDER BY m.nome",
                "%" + nome.trim() + "%"
        ).list();
    }
}
