package com.utfpr.estagio.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstudanteDto {
    
    private String nomeEstudante;
    private List<PeriodoEstagioDto> periodosEstagio;

    @Data
    @Builder
    public static class PeriodoEstagioDto {
        @JsonFormat(pattern = "dd/MM/yyyy")
        private LocalDate dataInicioEstagio;
        
        @JsonFormat(pattern = "dd/MM/yyyy")
        private LocalDate dataTerminoEstagio;
        
        private List<DatasRelatoriosParciaisAlunoDto> datasRelatoriosParciaisAluno;
        
        private List<DatasRelatoriosParciaisOrientadorDto> datasRelatoriosParciaisOrientador;
        
        private AvaliacaoDto intervaloAvaliacao;
        
        @JsonFormat(pattern = "dd/MM/yyyy")
        private LocalDate dataRelatorioVisita;
        
        @Builder.Default
        private boolean enviadoRelatorioVisita = false;

        @JsonFormat(pattern = "dd/MM/yyyy")
        private LocalDate dataRelatorioFinal; 
        
        @Builder.Default
        private boolean enviadoRelatorioFinal = false;
        
        private Boolean obrigatorio;
        
        private String empresa;
        
        
    }
}