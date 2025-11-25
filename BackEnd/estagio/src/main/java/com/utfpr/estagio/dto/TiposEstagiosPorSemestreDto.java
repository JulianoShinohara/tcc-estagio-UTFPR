package com.utfpr.estagio.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiposEstagiosPorSemestreDto {
    private int ano;
    private int semestre;
    private Map<String, Integer> quantidadePorTipo;
    private int totalEstagios;
}