package com.example.service;

import com.example.model.Regiao;
import java.util.List;

public interface RegiaoService {
    List<Regiao> findAll();
    Regiao findById(Long id);
}
