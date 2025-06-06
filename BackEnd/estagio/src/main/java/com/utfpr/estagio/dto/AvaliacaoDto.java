package com.utfpr.estagio.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvaliacaoDto {

	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataInicio;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataFim;

}
