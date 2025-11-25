package com.utfpr.estagio.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QtdeEstagiosOrientadorDto {
    private String nomeOrientador;
    private List<EstatisticasSemestreDto> estatisticasPorSemestre;
    private int totalObrigatorios;
    private int totalNaoObrigatorios;
    private int totalGeral;
}