package com.example.service.impl;

import com.example.model.Regiao;
import com.example.service.RegiaoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class RegiaoServiceImpl implements RegiaoService {

    @Override
    public List<Regiao> findAll() {
        return Arrays.asList(Regiao.values());
    }

    @Override
    public Regiao findById(Long id) {
        Regiao regiao = Regiao.valueOf(id);
        if (regiao == null) {
            throw new NotFoundException("Região com ID " + id + " não encontrada.");
        }
        return regiao;
    }
}
