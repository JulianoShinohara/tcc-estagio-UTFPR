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
        
        @JsonFormat(pattern = "dd/MM/yyyy")
        private LocalDate dataRelatorioFinal; 
        
        @Data
        @Builder
        public static class DatasRelatoriosParciaisAlunoDto {
            @JsonFormat(pattern = "dd/MM/yyyy")
            private LocalDate inicioPeriodoRelatorio;
            
            @JsonFormat(pattern = "dd/MM/yyyy")
            private LocalDate fimPeriodoRelatorio;
        }
        
        @Data
        @Builder
        public static class DatasRelatoriosParciaisOrientadorDto {
            @JsonFormat(pattern = "dd/MM/yyyy")
            private LocalDate inicioPeriodoRelatorio;
            
            @JsonFormat(pattern = "dd/MM/yyyy")
            private LocalDate fimPeriodoRelatorio;
        }
    }
}