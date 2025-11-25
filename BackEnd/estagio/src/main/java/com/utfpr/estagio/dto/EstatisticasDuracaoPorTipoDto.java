package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estatísticas de duração de estágios separadas por tipo (obrigatório/não obrigatório)
 * Contém métricas estatísticas para ambos os tipos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasDuracaoPorTipoDto {

	/**
	 * Estatísticas para estágios obrigatórios
	 */
	private EstatisticasDuracaoEstagioDto obrigatorios;

	/**
	 * Estatísticas para estágios não obrigatórios
	 */
	private EstatisticasDuracaoEstagioDto naoObrigatorios;
}
