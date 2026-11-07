package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Regiao {
    CENTRO_OESTE(1L, "Centro-Oeste"),
    NORTE(2L, "Norte"),
    NORDESTE(3L, "Nordeste"),
    SUDESTE(4L, "Sudeste"),
    SUL(5L, "Sul");

    private final Long id;
    private final String nome;

    Regiao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static Regiao valueOf(Long id) {
        if (id == null) {
            return null;
        }
        for (Regiao r : Regiao.values()) {
            if (Objects.equals(r.getId(), id)) {
                return r;
            }
        }
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Regiao fromJson(Object value) {
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
                for (Regiao r : Regiao.values()) {
                    if (r.name().equalsIgnoreCase(trimmed) || r.getNome().equalsIgnoreCase(trimmed)) {
                        return r;
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
            Object nomeVal = map.get("nome");
            if (nomeVal instanceof String nomeStr) {
                for (Regiao r : Regiao.values()) {
                    if (r.getNome().equalsIgnoreCase(nomeStr.trim()) || r.name().equalsIgnoreCase(nomeStr.trim())) {
                        return r;
                    }
                }
            }
        }
        return null;
    }
}
