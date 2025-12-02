package com.utfpr.estagio.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContadoresUceDto {
	private String nomeEmpresa;
	private Map<String, Integer> orientadores = new HashMap<>();
	private List<Double> valoresBolsa = new ArrayList<>();
	private List<Double> valoresBeneficio = new ArrayList<>();
	private int obrigatorios = 0;
	private int naoObrigatorios = 0;

	public ContadoresUceDto(String nomeEmpresa) {
		this.nomeEmpresa = nomeEmpresa;
		this.orientadores = new HashMap<>();
		this.valoresBolsa = new ArrayList<>();
		this.valoresBeneficio = new ArrayList<>();
	}

	public void incrementarOrientador(String nomeOrientador) {
		orientadores.put(nomeOrientador, orientadores.getOrDefault(nomeOrientador, 0) + 1);
	}

	public void adicionarValorBolsa(double valor) {
		if (valor > 0) {
			valoresBolsa.add(valor);
		}
	}

	public void adicionarValorBeneficio(double valor) {
		if (valor > 0) {
			valoresBeneficio.add(valor);
		}
	}

	public void incrementarObrigatorios() {
		this.obrigatorios++;
	}

	public void incrementarNaoObrigatorios() {
		this.naoObrigatorios++;
	}

	public double getValorMedioBolsa() {
		if (valoresBolsa.isEmpty()) {
			return 0.0;
		}
		return valoresBolsa.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
	}

	public double getValorMedioBeneficio() {
		if (valoresBeneficio.isEmpty()) {
			return 0.0;
		}
		return valoresBeneficio.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
	}
}
