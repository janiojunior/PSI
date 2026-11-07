package com.example.model.converterjpa;

import com.example.model.Regiao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RegiaoConverter implements AttributeConverter<Regiao, Long> {

    @Override
    public Long convertToDatabaseColumn(Regiao regiao) {
        return regiao == null ? null : regiao.getId();
    }

    @Override
    public Regiao convertToEntityAttribute(Long id) {
        return Regiao.valueOf(id);
    }
}
