package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estatísticas de duração de estágios
 * Contém métricas estatísticas sobre o tempo de realização dos estágios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasDuracaoEstagioDto {

	/**
	 * Duração média dos estágios em dias
	 */
	private double durracaoMedia;

	/**
	 * Duração mínima dos estágios em dias
	 */
	private long duracaoMinima;

	/**
	 * Duração máxima dos estágios em dias
	 */
	private long duracaoMaxima;

	/**
	 * Primeiro quartil (Q1) - 25º percentil em dias
	 * 25% dos estágios duram menos que este valor
	 */
	private long primeiroQuartil;

	/**
	 * Terceiro quartil (Q3) - 75º percentil em dias
	 * 75% dos estágios duram menos que este valor
	 */
	private long terceiroQuartil;

	/**
	 * Mediana (Q2) - 50º percentil em dias
	 * Valor central quando os dados são ordenados
	 */
	private long mediana;

	/**
	 * Quantidade total de estágios considerados
	 */
	private int totalEstagios;
}
