package com.example.mapper;

import com.example.dto.EstadoDTO;
import com.example.dto.MunicipioDTO;
import com.example.model.Estado;
import com.example.model.Municipio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MunicipioMapperTest {

    private MunicipioMapper municipioMapper;

    @BeforeEach
    void setUp() {
        municipioMapper = new MunicipioMapper();
        municipioMapper.estadoMapper = new EstadoMapper();
    }

    @Test
    void deveConverterEntidadeParaDTOComEstado() {
        Estado estado = new Estado(1L, "TO", "Tocantins");
        Municipio municipio = new Municipio(10L, "Palmas", estado);

        MunicipioDTO dto = municipioMapper.toDTO(municipio);

        assertNotNull(dto);
        assertEquals(10L, dto.id());
        assertEquals("Palmas", dto.nome());
        assertNotNull(dto.estado());
        assertEquals(1L, dto.estado().id());
        assertEquals("TO", dto.estado().sigla());
        assertEquals("Tocantins", dto.estado().nome());
    }

    @Test
    void deveHigienizarEspacosAoConverterDTOParaEntidade() {
        EstadoDTO estadoDTO = new EstadoDTO(1L, "TO", "Tocantins");
        MunicipioDTO dto = new MunicipioDTO(null, "  Araguaína  ", estadoDTO);

        Municipio entity = municipioMapper.toEntity(dto);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("Araguaína", entity.getNome());
    }

    @Test
    void deveAtualizarEntidadeExistenteAPartirDoDTO() {
        Estado estado = new Estado(1L, "TO", "Tocantins");
        Municipio entity = new Municipio(5L, "Nome Antigo", estado);
        MunicipioDTO dto = new MunicipioDTO(5L, "  Gurupi  ", new EstadoDTO(1L, "TO", "Tocantins"));

        municipioMapper.updateEntityFromDTO(dto, entity);

        assertEquals(5L, entity.getId());
        assertEquals("Gurupi", entity.getNome());
    }
}
