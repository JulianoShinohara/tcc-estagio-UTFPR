import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EstatisticasSemestreDto {
  ano: number;
  semestre: number;
  quantidadeObrigatorios: number;
  quantidadeNaoObrigatorios: number;
  total: number;
}

export interface QtdeEstagiosOrientadorDto {
  nomeOrientador: string;
  estatisticasPorSemestre: EstatisticasSemestreDto[];
  totalObrigatorios: number;
  totalNaoObrigatorios: number;
  totalGeral: number;
}

export interface TiposEstagiosPorSemestreDto {
  ano: number;
  semestre: number;
  quantidadePorTipo: { [key: string]: number };
  totalEstagios: number;
}

export interface SupervisorDto {
  nomeSupervisor: string;
  quantidadeEstagios: number;
  semestre?: number;
  ano?: number;
}

export interface EmpresaComSupervisoresDto {
  nomeEmpresa: string;
  supervisores: SupervisorDto[];
  valorMedioBolsa: number;
  valorMedioBeneficio: number;
  quantidadeObrigatorios: number;
  quantidadeNaoObrigatorios: number;
}

export interface EstatisticasDuracaoEstagioDto {
  duracaoMedia: number;
  duracaoMinima: number;
  duracaoMaxima: number;
  primeiroQuartil: number;
  terceiroQuartil: number;
  mediana: number;
  totalEstagios: number;
}

@Injectable({
  providedIn: 'root'
})
export class PraeService {
  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  /**
   * Busca estatísticas de orientações por orientador
   */
  getEstagiosPorOrientador(): Observable<QtdeEstagiosOrientadorDto[]> {
    const url = `${this.baseUrl}/prae/estagios-por-orientador`;
    return this.http.get<QtdeEstagiosOrientadorDto[]>(url);
  }

  /**
   * Busca tipos de estágios por semestre
   */
  getTiposEstagiosPorSemestre(): Observable<TiposEstagiosPorSemestreDto[]> {
    const url = `${this.baseUrl}/prae/tipos-estagios-por-semestre`;
    return this.http.get<TiposEstagiosPorSemestreDto[]>(url);
  }

  /**
   * Busca estágios iniciados e finalizados por mês
   */
  getEstagiosIniciadosFinalizadosPorMes(): Observable<any[]> {
    const url = `${this.baseUrl}/prae/estagios-por-mes`;
    return this.http.get<any[]>(url);
  }

  /**
   * Busca supervisores agrupados por empresa
   */
  getSupervisores(): Observable<EmpresaComSupervisoresDto[]> {
    const url = `${this.baseUrl}/prae/supervisores`;
    return this.http.get<EmpresaComSupervisoresDto[]>(url);
  }

  /**
   * Busca estatísticas de duração dos estágios
   */
  getEstatisticasDuracao(): Observable<EstatisticasDuracaoEstagioDto> {
    const url = `${this.baseUrl}/prae/estatisticas-duracao`;
    return this.http.get<EstatisticasDuracaoEstagioDto>(url);
  }

  /**
   * Busca estatísticas de duração dos estágios separadas por tipo
   */
  getEstatisticasDuracaoPorTipo(): Observable<any> {
    const url = `${this.baseUrl}/prae/estatisticas-duracao-por-tipo`;
    return this.http.get<any>(url);
  }

  /**
   * Busca estatísticas de duração dos estágios separadas por tipo de estágio
   */
  getEstatisticasDuracaoPorTipoEstagio(): Observable<any> {
    const url = `${this.baseUrl}/prae/estatisticas-duracao-por-tipo-estagio`;
    return this.http.get<any>(url);
  }
}