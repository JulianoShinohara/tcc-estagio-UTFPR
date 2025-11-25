package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasSemestreDto {
	private int ano;
	private int semestre;
	private int quantidadeObrigatorios;
	private int quantidadeNaoObrigatorios;
	private int total;
}