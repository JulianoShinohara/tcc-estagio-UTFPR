package com.utfpr.estagio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorDto {
    private String nomeSupervisor;
    private int quantidadeEstagios;
    private Integer ano;
    private Integer semestre;
}
