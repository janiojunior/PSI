package com.example.repository;

import com.example.model.Pessoa;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class PessoaRepository implements PanacheRepository<Pessoa> {

    public Optional<Pessoa> findByCpf(String cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        return find(
                "SELECT DISTINCT p FROM Pessoa p LEFT JOIN FETCH p.telefones LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE p.cpf = ?1",
                cpf.replaceAll("\\D", "").trim()
        ).firstResultOptional();
    }

    public Optional<Pessoa> findByCpfExceptId(String cpf, Long idPessoa) {
        if (cpf == null) {
            return Optional.empty();
        }
        String sanitized = cpf.replaceAll("\\D", "").trim();
        if (idPessoa == null) {
            return find("cpf = ?1", sanitized).firstResultOptional();
        }
        return find("cpf = ?1 AND id <> ?2", sanitized, idPessoa).firstResultOptional();
    }

    public Optional<Pessoa> findByEmailExceptId(String email, Long idPessoa) {
        if (email == null) {
            return Optional.empty();
        }
        String sanitized = email.trim();
        if (idPessoa == null) {
            return find("LOWER(email) = LOWER(?1)", sanitized).firstResultOptional();
        }
        return find("LOWER(email) = LOWER(?1) AND id <> ?2", sanitized, idPessoa).firstResultOptional();
    }
}
