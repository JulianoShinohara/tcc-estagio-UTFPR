export interface PeriodoRelatorio {
  inicioPeriodo: Date;
  fimPeriodo: Date;
}

export interface EstudanteDto {
  nomeEstudante: string;
  periodosEstagio: PeriodoEstagioDto[];
}
export interface PeriodoEstagioDto {
    dataInicioEstagio: Date;
    dataTerminoEstagio: Date;
    datasRelatoriosParciaisAluno: DatasRelatoriosParciaisAlunoDto[];
    datasRelatoriosParciaisOrientador: DatasRelatoriosParciaisOrientadorDto[];
    dataRelatorioVisita?: Date;
    enviadoRelatorioVisita?: boolean;
    dataRelatorioFinal: Date;
    enviadoRelatorioFinal?: boolean;
    intervaloAvaliacao?: AvaliacaoDto;
    empresa?: String;
    obrigatorio: boolean;
}

// DatasRelatoriosParciaisAlunoDto.ts
export interface DatasRelatoriosParciaisAlunoDto {
    inicioPeriodoRelatorio: Date;
    fimPeriodoRelatorio: Date;
    enviado: boolean;
}

// DatasRelatoriosParciaisOrientadorDto.ts
export interface DatasRelatoriosParciaisOrientadorDto {
    inicioPeriodoRelatorio: Date;
    fimPeriodoRelatorio: Date;
    enviado: boolean;
}

// AvaliacaoDto.ts
export interface AvaliacaoDto {
    dataInicio: Date;
    dataFim: Date;
}

// RelatorioVisitaDto.ts (opcional - pode ser incorporado no PeriodoEstagioDto)
export interface RelatorioVisitaDto {
    dataLimite: Date;
    enviado: boolean;
}

// RelatorioFinalDto.ts (opcional - pode ser incorporado no PeriodoEstagioDto)
export interface RelatorioFinalDto {
    dataLimite: Date;
    enviado: boolean;
}
