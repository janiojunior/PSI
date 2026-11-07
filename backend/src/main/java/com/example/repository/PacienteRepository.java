package com.example.repository;

import com.example.model.Paciente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PacienteRepository implements PanacheRepository<Paciente> {

    public List<Paciente> listAllWithPessoa() {
        return find(
                "SELECT DISTINCT pac FROM Paciente pac JOIN FETCH pac.pessoa p LEFT JOIN FETCH p.telefones LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado ORDER BY p.nome"
        ).list();
    }

    public List<Paciente> listPaginated(int page, int pageSize) {
        return find(
                "SELECT pac FROM Paciente pac JOIN FETCH pac.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado ORDER BY p.nome"
        ).page(Page.of(page, pageSize)).list();
    }

    public Optional<Paciente> findByIdWithPessoa(Long id) {
        return find(
                "SELECT DISTINCT pac FROM Paciente pac JOIN FETCH pac.pessoa p LEFT JOIN FETCH p.telefones LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE pac.id = ?1",
                id
        ).firstResultOptional();
    }

    public Optional<Paciente> findByCpf(String cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        return find(
                "SELECT DISTINCT pac FROM Paciente pac JOIN FETCH pac.pessoa p LEFT JOIN FETCH p.telefones LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE p.cpf = ?1",
                cpf.replaceAll("\\D", "").trim()
        ).firstResultOptional();
    }

    public List<Paciente> findByNomePaginated(String nome, int page, int pageSize) {
        String filtro = "%" + (nome == null ? "" : nome.trim()) + "%";
        return find(
                "SELECT pac FROM Paciente pac JOIN FETCH pac.pessoa p LEFT JOIN FETCH p.municipio m LEFT JOIN FETCH m.estado WHERE LOWER(p.nome) LIKE LOWER(?1) ORDER BY p.nome",
                filtro
        ).page(Page.of(page, pageSize)).list();
    }

    public long countByNome(String nome) {
        String filtro = "%" + (nome == null ? "" : nome.trim()) + "%";
        return count("LOWER(pessoa.nome) LIKE LOWER(?1)", filtro);
    }
}
