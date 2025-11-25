import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, retry, timeout } from 'rxjs/operators';

interface EstudanteComEstagios {
  nomeEstudante: string;
  periodosEstagio: PeriodoEstagio[];
}

interface PeriodoEstagio {
  empresa?: string;
  obrigatorio: boolean;
  dataInicioEstagio: string;
  dataTerminoEstagio: string;
  datasRelatoriosParciaisAluno: RelatorioStatus[];
  datasRelatoriosParciaisOrientador: RelatorioStatus[];
  dataRelatorioVisita?: string;
  enviadoRelatorioVisita?: boolean;
  dataRelatorioFinal: string;
  enviadoRelatorioFinal: boolean;
  intervaloAvaliacao?: {
    dataInicio: string;
    dataFim: string;
  };
}

interface RelatorioStatus {
  inicioPeriodoRelatorio: string;
  fimPeriodoRelatorio: string;
  enviado: boolean;
}

interface EstatisticasOrientador {
  totalEstudantes: number;
  totalEstagios: number;
  relatoriosPendentes: number;
  relatoriosEnviados: number;
}

interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root',
})
export class OrientadorService {
  private readonly USE_MOCK = false;
  private readonly apiUrl = 'http://localhost:8080/orientador';
  private readonly timeoutDuration = 30000;

  constructor(private http: HttpClient) {}

  async getAllOrientadoresNomes(): Promise<string[]> {
    try {
      const result = await this.http
        .get<string[]>(`${this.apiUrl}/nomes`)
        .pipe(timeout(this.timeoutDuration), retry(2))
        .toPromise();

      const nomes = result || [];
      return nomes.sort();
    } catch (error) {
      console.error('Erro ao carregar todos os orientadores:', error);
      throw this.processError(error);
    }
  }

  getAllOrientadoresNomesObservable(): Observable<string[]> {
    return this.http
      .get<string[]>(`${this.apiUrl}/nomes`)
      .pipe(
        timeout(this.timeoutDuration),
        retry(2),
        catchError(this.handleError.bind(this))
      );
  }

  async getEstagiosByOrientadorAsync(nomeOrientador: string): Promise<any[]> {
    try {
      if (!nomeOrientador || nomeOrientador.trim().length === 0) {
        throw new Error('Nome do orientador é obrigatório');
      }

      const params = new HttpParams()
        .set('orientador', nomeOrientador.trim())
        .set('includeDetails', 'true')
        .set('orderBy', 'nomeEstudante');

      const result = await this.http
        .get<any[]>(`${this.apiUrl}/${nomeOrientador}/estagios`, { params })
        .toPromise();

      return result || [];
    } catch (error) {
      console.error('Erro ao buscar estágios do orientador:', error);
      throw this.processError(error);
    }
  }

  getEstagiosByOrientador(nomeOrientador: string): Observable<any[]> {
    const params = new HttpParams()
      .set('orientador', nomeOrientador.trim())
      .set('includeDetails', 'true')
      .set('orderBy', 'nomeEstudante');

    return this.http
      .get<any[]>(`${this.apiUrl}/${nomeOrientador}/estagios`, { params })
      .pipe(
        timeout(this.timeoutDuration),
        retry(1),
        catchError(this.handleError.bind(this))
      );
  }

  async getEstatisticasOrientador(
    nomeOrientador: string
  ): Promise<EstatisticasOrientador> {
    try {
      const params = new HttpParams().set('orientador', nomeOrientador.trim());
      const result = await this.http
        .get<EstatisticasOrientador>(
          `${this.apiUrl}/orientadores/estatisticas`,
          { params }
        )
        .toPromise();

      return (
        result || {
          totalEstudantes: 0,
          totalEstagios: 0,
          relatoriosPendentes: 0,
          relatoriosEnviados: 0,
        }
      );
    } catch (error) {
      console.warn('Erro ao buscar estatísticas:', error);
      return {
        totalEstudantes: 0,
        totalEstagios: 0,
        relatoriosPendentes: 0,
        relatoriosEnviados: 0,
      };
    }
  }

  async verificarOrientadorExiste(nomeOrientador: string): Promise<boolean> {
    try {
      const params = new HttpParams().set('nome', nomeOrientador.trim());
      const result = await this.http
        .get<{ existe: boolean }>(`${this.apiUrl}/orientadores/verificar`, {
          params,
        })
        .toPromise();

      return result?.existe || false;
    } catch (error) {
      console.warn('Erro ao verificar orientador:', error);
      return false;
    }
  }

  getAllOrientadores(
    page: number = 0,
    size: number = 20
  ): Observable<PaginatedResponse<any>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'nome,asc')
      .set('ativo', 'true');

    return this.http
      .get<PaginatedResponse<any>>(`${this.apiUrl}/orientadores`, { params })
      .pipe(
        timeout(this.timeoutDuration),
        catchError(this.handleError.bind(this))
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Erro desconhecido ocorreu';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erro: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 400:
          errorMessage = 'Requisição inválida. Verifique os dados enviados.';
          break;
        case 404:
          errorMessage = 'Orientador não encontrado.';
          break;
        case 500:
          errorMessage =
            'Erro interno do servidor. Tente novamente mais tarde.';
          break;
        case 503:
          errorMessage = 'Serviço temporariamente indisponível.';
          break;
        case 0:
          errorMessage = 'Não foi possível conectar com o servidor.';
          break;
        default:
          errorMessage = `Erro ${error.status}: ${error.message}`;
      }
    }

    console.error('Erro no OrientadorService:', error);
    return throwError(errorMessage);
  }

  private processError(error: any): Error {
    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 400:
          return new Error('Dados inválidos fornecidos.');
        case 404:
          return new Error('Orientador não encontrado.');
        case 500:
          return new Error('Erro interno do servidor.');
        case 503:
          return new Error('Serviço temporariamente indisponível.');
        case 0:
          return new Error('Não foi possível conectar com o servidor.');
        default:
          return new Error(`Erro ${error.status}: ${error.message}`);
      }
    }

    if (error instanceof Error) {
      return error;
    }

    return new Error('Erro desconhecido ocorreu.');
  }
}

