package com.utfpr.estagio.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaComSupervisoresDto {
    private String nomeEmpresa;
    private List<SupervisorDto> supervisores;
    private double valorMedioBolsa;
    private double valorMedioBeneficio;
    private int quantidadeObrigatorios;
    private int quantidadeNaoObrigatorios;
}
