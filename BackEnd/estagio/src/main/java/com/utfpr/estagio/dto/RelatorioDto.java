package com.utfpr.estagio.dto;

import java.time.LocalDate;

import com.google.auto.value.AutoValue.Builder;

import lombok.Getter;

@Getter
@Builder
public class RelatorioDto {
    private String nomeEstudante;
    private String tipoRelatorio;
    private LocalDate inicioPeriodoRelatorio;
    private LocalDate fimPeriodoRelatorio;
}