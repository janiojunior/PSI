package com.example.repository;

import com.example.model.Psicologo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PsicologoRepository implements PanacheRepository<Psicologo> {

    public List<Psicologo> listAllWithPessoa() {
        return find(
                "SELECT DISTINCT psi FROM Psicologo psi JOIN FETCH psi.pessoa p LEFT JOIN FETCH p.telefones LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado ORDER BY p.nome"
        ).list();
    }

    public List<Psicologo> listPaginated(int page, int pageSize) {
        return find(
                "SELECT psi FROM Psicologo psi JOIN FETCH psi.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado ORDER BY p.nome"
        ).page(Page.of(page, pageSize)).list();
    }

    public Optional<Psicologo> findByIdWithPessoa(Long id) {
        return find(
                "SELECT DISTINCT psi FROM Psicologo psi JOIN FETCH psi.pessoa p LEFT JOIN FETCH p.telefones LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE psi.id = ?1",
                id
        ).firstResultOptional();
    }

    public Optional<Psicologo> findByCpf(String cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        return find(
                "SELECT DISTINCT psi FROM Psicologo psi JOIN FETCH psi.pessoa p LEFT JOIN FETCH p.telefones LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE p.cpf = ?1",
                cpf.replaceAll("\\D", "").trim()
        ).firstResultOptional();
    }

    public Optional<Psicologo> findByCrp(String crp) {
        if (crp == null) {
            return Optional.empty();
        }
        return find(
                "SELECT DISTINCT psi FROM Psicologo psi JOIN FETCH psi.pessoa p LEFT JOIN FETCH p.telefones LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE UPPER(psi.crp) = UPPER(?1)",
                crp.trim()
        ).firstResultOptional();
    }

    public Optional<Psicologo> findByCrpExceptId(String crp, Long id) {
        if (crp == null) {
            return Optional.empty();
        }
        String sanitized = crp.trim();
        if (id == null) {
            return find("UPPER(crp) = UPPER(?1)", sanitized).firstResultOptional();
        }
        return find("UPPER(crp) = UPPER(?1) AND id <> ?2", sanitized, id).firstResultOptional();
    }

    public List<Psicologo> findByNomePaginated(String nome, int page, int pageSize) {
        String filtro = "%" + (nome == null ? "" : nome.trim()) + "%";
        return find(
                "SELECT psi FROM Psicologo psi JOIN FETCH psi.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE LOWER(p.nome) LIKE LOWER(?1) ORDER BY p.nome",
                filtro
        ).page(Page.of(page, pageSize)).list();
    }

    public long countByNome(String nome) {
        String filtro = "%" + (nome == null ? "" : nome.trim()) + "%";
        return count("LOWER(pessoa.nome) LIKE LOWER(?1)", filtro);
    }
}
