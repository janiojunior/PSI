package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Map;
import java.util.Objects;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Perfil {
    ADMINISTRADOR(1L, "Administrador"),
    ATENDENTE(2L, "Atendente"),
    PACIENTE(3L, "Paciente"),
    PSICOLOGO(4L, "Psicólogo");

    private final Long id;
    private final String nome;

    Perfil(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static Perfil valueOf(Long id) {
        if (id == null) {
            return null;
        }
        for (Perfil perfil : Perfil.values()) {
            if (Objects.equals(perfil.getId(), id)) {
                return perfil;
            }
        }
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Perfil fromJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return valueOf(number.longValue());
        }
        if (value instanceof String str) {
            String trimmed = str.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                return valueOf(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {
                for (Perfil p : Perfil.values()) {
                    if (p.name().equalsIgnoreCase(trimmed) || p.getNome().equalsIgnoreCase(trimmed)) {
                        return p;
                    }
                }
            }
        }
        if (value instanceof Map<?, ?> map) {
            Object idVal = map.get("id");
            if (idVal instanceof Number number) {
                return valueOf(number.longValue());
            }
            if (idVal instanceof String str) {
                try {
                    return valueOf(Long.parseLong(str.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
