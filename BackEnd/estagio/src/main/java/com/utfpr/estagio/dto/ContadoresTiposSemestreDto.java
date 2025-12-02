package com.utfpr.estagio.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContadoresTiposSemestreDto {
	private int ano;
	private int semestre;
	private Map<String, Integer> quantidadePorTipo = new HashMap<>();

	public ContadoresTiposSemestreDto(int ano, int semestre) {
		this.ano = ano;
		this.semestre = semestre;
		this.quantidadePorTipo = new HashMap<>();
	}

	public void incrementarTipo(String tipo) {
		quantidadePorTipo.put(tipo, quantidadePorTipo.getOrDefault(tipo, 0) + 1);
	}
}
