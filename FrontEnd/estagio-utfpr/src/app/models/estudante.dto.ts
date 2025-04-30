export interface PeriodoRelatorio {
  inicioPeriodo: Date;
  fimPeriodo: Date;
}

export interface PeriodoEstagio {
  dataInicioEstagio: Date;
  dataTerminoEstagio: Date;
  datasRelatoriosParciais: PeriodoRelatorio[];
  dataRelatorioVisita: Date;
  dataRelatorioFinal: Date;
}

export interface EstudanteDto {
  nomeEstudante: string;
  periodosEstagio: PeriodoEstagio[];
}
