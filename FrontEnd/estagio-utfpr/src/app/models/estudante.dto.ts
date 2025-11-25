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

export interface DatasRelatoriosParciaisAlunoDto {
    inicioPeriodoRelatorio: Date;
    fimPeriodoRelatorio: Date;
    enviado: boolean;
}

export interface DatasRelatoriosParciaisOrientadorDto {
    inicioPeriodoRelatorio: Date;
    fimPeriodoRelatorio: Date;
    enviado: boolean;
}

export interface AvaliacaoDto {
    dataInicio: Date;
    dataFim: Date;
}

export interface RelatorioVisitaDto {
    dataLimite: Date;
    enviado: boolean;
}

export interface RelatorioFinalDto {
    dataLimite: Date;
    enviado: boolean;
}
