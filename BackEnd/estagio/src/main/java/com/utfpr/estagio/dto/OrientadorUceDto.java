package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrientadorUceDto {
	private String nomeOrientador;
	private int quantidadeEstagios;
}