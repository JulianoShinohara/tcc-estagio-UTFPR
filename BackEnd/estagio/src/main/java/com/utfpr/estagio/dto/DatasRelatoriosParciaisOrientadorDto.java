package com.utfpr.estagio.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasRelatoriosParciaisOrientadorDto {
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate inicioPeriodoRelatorio;

	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate fimPeriodoRelatorio;

	@Builder.Default
	private boolean enviado = false;
}
