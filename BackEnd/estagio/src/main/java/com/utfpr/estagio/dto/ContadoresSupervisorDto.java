package com.utfpr.estagio.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContadoresSupervisorDto {
	private String nomeSupervisor;
	private List<Double> valoresBolsa = new ArrayList<>();
	private List<Double> valoresBeneficio = new ArrayList<>();
	private int quantidadeEstagios = 0;
	private int obrigatorios = 0;
	private int naoObrigatorios = 0;

	public ContadoresSupervisorDto(String nomeSupervisor) {
		this.nomeSupervisor = nomeSupervisor;
		this.valoresBolsa = new ArrayList<>();
		this.valoresBeneficio = new ArrayList<>();
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

	public void incrementarQuantidadeEstagios() {
		this.quantidadeEstagios++;
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
