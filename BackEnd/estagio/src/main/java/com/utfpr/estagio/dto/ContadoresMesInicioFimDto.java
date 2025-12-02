package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContadoresMesInicioFimDto {
	private int ano;
	private int mes;
	private int iniciadosObrigatorios = 0;
	private int iniciadosNaoObrigatorios = 0;
	private int finalizadosObrigatorios = 0;
	private int finalizadosNaoObrigatorios = 0;

	public ContadoresMesInicioFimDto(int ano, int mes) {
		this.ano = ano;
		this.mes = mes;
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
