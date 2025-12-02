package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContadoresSemestreInicioFimDto {
	private int ano;
	private int semestre;
	private int iniciadosObrigatorios = 0;
	private int iniciadosNaoObrigatorios = 0;
	private int finalizadosObrigatorios = 0;
	private int finalizadosNaoObrigatorios = 0;

	public ContadoresSemestreInicioFimDto(int ano, int semestre) {
		this.ano = ano;
		this.semestre = semestre;
	}

	public void incrementarIniciadosObrigatorios() {
		this.iniciadosObrigatorios++;
	}

	public void incrementarIniciadosNaoObrigatorios() {
		this.iniciadosNaoObrigatorios++;
	}

	public void incrementarFinalizadosObrigatorios() {
		this.finalizadosObrigatorios++;
	}

	public void incrementarFinalizadosNaoObrigatorios() {
		this.finalizadosNaoObrigatorios++;
	}
}
