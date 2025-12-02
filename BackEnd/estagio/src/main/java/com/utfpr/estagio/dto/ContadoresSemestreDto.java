package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContadoresSemestreDto {
	private int ano;
	private int semestre;
	private int obrigatorios = 0;
	private int naoObrigatorios = 0;

	public ContadoresSemestreDto(int ano, int semestre) {
		this.ano = ano;
		this.semestre = semestre;
	}

	public void incrementarObrigatorios() {
		this.obrigatorios++;
	}

	public void incrementarNaoObrigatorios() {
		this.naoObrigatorios++;
	}
}
