package com.utfpr.estagio.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estatísticas de duração de estágios separadas por tipo de estágio
 * Contém métricas estatísticas para ambos os tipos (obrigatório/não obrigatório)
 * dentro de cada categoria de tipo de estágio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasDuracaoPorTipoEstagio {

	/**
	 * Mapa contendo as estatísticas por tipo de estágio
	 * Chave: tipo de estágio (ex: "Estágio em UCE", "Bolsista ou voluntário em projeto")
	 * Valor: objeto contendo estatísticas de obrigatório e não obrigatório
	 */
	private Map<String, EstatisticasDuracaoPorTipoDto> estatisticasPorTipo;

	/**
	 * Lista de tipos de estágio disponíveis na planilha
	 */
	private java.util.List<String> tiposDisponiveis;
}
