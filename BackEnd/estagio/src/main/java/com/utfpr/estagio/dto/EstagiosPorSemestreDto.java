package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstagiosPorSemestreDto {
    private int ano;
    private int semestre;
    private int qtdeIniciados;
    private int qtdeFinalizados;
    private int qtdeIniciadosObrigatorios;
    private int qtdeIniciadosNaoObrigatorios;
    private int qtdeFinalizadosObrigatorios;
    private int qtdeFinalizadosNaoObrigatorios;
}