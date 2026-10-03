package com.example.repository;

import com.example.model.Estado;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class EstadoRepository implements PanacheRepository<Estado> {

    public Optional<Estado> findBySigla(String sigla) {
        return find("UPPER(sigla) = UPPER(?1)", sigla).firstResultOptional();
    }
}
