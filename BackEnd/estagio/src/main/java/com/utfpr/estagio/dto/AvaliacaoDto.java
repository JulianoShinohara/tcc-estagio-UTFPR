package com.utfpr.estagio.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvaliacaoDto {

	private LocalDate dataInicio;
	private LocalDate dataFim;


}
