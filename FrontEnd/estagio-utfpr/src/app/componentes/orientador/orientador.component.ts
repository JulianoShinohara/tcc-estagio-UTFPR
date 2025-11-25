import { Component } from '@angular/core';
import { OrientadorService } from '../../services/orientador.service';
import { AutocompleteDropdownComponent } from '../autocomplete-dropdown/autocomplete-dropdown.component';
import { CommonModule } from '@angular/common';

interface EstudanteComEstagios {
  nomeEstudante: string;
  collapsed: boolean;
  periodosEstagio: PeriodoEstagioExtendido[];
}

interface PeriodoEstagioExtendido {
  collapsed: boolean;
  empresa?: string;
  obrigatorio: boolean;
  dataInicioEstagio: string;
  dataTerminoEstagio: string;
  datasRelatoriosParciaisAluno: RelatorioStatus[];
  datasRelatoriosParciaisOrientador: RelatorioStatus[];
  dataRelatorioVisita?: string;
  enviadoRelatorioVisita: boolean;
  dataRelatorioFinal: string;
  enviadoRelatorioFinal: boolean;
  intervaloAvaliacao?: {
    dataInicio: string;
    dataFim: string;
  };
  emAndamento?: boolean;
}

interface RelatorioStatus {
  inicioPeriodoRelatorio: string;
  fimPeriodoRelatorio: string;
  enviado: boolean;
}

@Component({
  selector: 'app-orientador',
  standalone: true,
  imports: [AutocompleteDropdownComponent, CommonModule],
  templateUrl: './orientador.component.html',
  styleUrls: ['./orientador.component.scss']
})
export class OrientadorComponent {
  orientadorSelecionado: string | null = null;
  estudantesComEstagios: EstudanteComEstagios[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';
  collapseEstagiosAndamento: { [key: string]: boolean } = {};

  constructor(private orientadorService: OrientadorService) {}

  loadAllOrientadores = async (): Promise<string[]> => {
    try {
      return await this.orientadorService.getAllOrientadoresNomes();
    } catch (error) {
      console.error('Erro ao carregar orientadores:', error);
      throw error;
    }
  };

  onOrientadorSelected(nomeOrientador: string): void {
    this.orientadorSelecionado = nomeOrientador;
    this.errorMessage = '';
    this.buscarEstagiosDoOrientador(nomeOrientador);
  }

  onInputCleared(): void {
    this.orientadorSelecionado = null;
    this.estudantesComEstagios = [];
    this.errorMessage = '';
  }

  private async buscarEstagiosDoOrientador(nomeOrientador: string): Promise<void> {
    this.isLoading = true;
    this.estudantesComEstagios = [];

    try {
      const dados = await this.orientadorService.getEstagiosByOrientadorAsync(nomeOrientador);

      this.estudantesComEstagios = dados.map(estudante => ({
        ...estudante,
        collapsed: true,
        periodosEstagio: estudante.periodosEstagio
          .map((periodo: any) => ({
            ...periodo,
            collapsed: true,
            emAndamento: this.verificarSeEmAndamento(periodo.dataInicioEstagio, periodo.dataTerminoEstagio)
          }))
          .sort((a: PeriodoEstagioExtendido, b: PeriodoEstagioExtendido) => {
            if (a.emAndamento && !b.emAndamento) return -1;
            if (!a.emAndamento && b.emAndamento) return 1;
            return 0;
          })
      }));

    } catch (error) {
      console.error('Erro ao buscar estágios:', error);
      this.errorMessage = 'Erro ao buscar estágios do orientador. Tente novamente.';
    } finally {
      this.isLoading = false;
    }
  }

  /**
   * Verifica se um estágio está em andamento
   */
  private verificarSeEmAndamento(dataInicio: string, dataTermino: string): boolean {
    try {
      const partes = dataInicio.split('/');
      const inicio = new Date(parseInt(partes[2]), parseInt(partes[1]) - 1, parseInt(partes[0]));

      const partesTermino = dataTermino.split('/');
      const termino = new Date(parseInt(partesTermino[2]), parseInt(partesTermino[1]) - 1, parseInt(partesTermino[0]));

      const hoje = new Date();
      hoje.setHours(0, 0, 0, 0);

      return inicio <= hoje && hoje <= termino;
    } catch (error) {
      console.error('Erro ao verificar se estágio está em andamento:', error);
      return false;
    }
  }

  toggleEstudante(estudanteIndex: number): void {
    if (this.estudantesComEstagios[estudanteIndex]) {
      this.estudantesComEstagios[estudanteIndex].collapsed =
        !this.estudantesComEstagios[estudanteIndex].collapsed;
    }
  }

  toggleEstagio(estudanteIndex: number, estagioIndex: number): void {
    if (this.estudantesComEstagios[estudanteIndex]?.periodosEstagio[estagioIndex]) {
      this.estudantesComEstagios[estudanteIndex].periodosEstagio[estagioIndex].collapsed =
        !this.estudantesComEstagios[estudanteIndex].periodosEstagio[estagioIndex].collapsed;
    }
  }

  /**
   * Retorna todos os estágios em andamento com informações do estudante
   */
  obterEstagiosEmAndamento(): Array<{
    nomeEstudante: string;
    periodo: PeriodoEstagioExtendido;
    estudanteIndex: number;
    periodoIndex: number;
    collapsed: boolean;
  }> {
    const estagiosEmAndamento: Array<{
      nomeEstudante: string;
      periodo: PeriodoEstagioExtendido;
      estudanteIndex: number;
      periodoIndex: number;
      collapsed: boolean;
    }> = [];

    this.estudantesComEstagios.forEach((estudante, estudanteIndex) => {
      estudante.periodosEstagio.forEach((periodo, periodoIndex) => {
        if (periodo.emAndamento) {
          const key = `${estudanteIndex}-${periodoIndex}`;
          estagiosEmAndamento.push({
            nomeEstudante: estudante.nomeEstudante,
            periodo: periodo,
            estudanteIndex: estudanteIndex,
            periodoIndex: periodoIndex,
            collapsed: this.collapseEstagiosAndamento[key] !== false
          });
        }
      });
    });

    return estagiosEmAndamento;
  }

  /**
   * Alterna o estado de collapse de um estágio em andamento
   */
  toggleCollapseEstagioAndamento(estudanteIndex: number, periodoIndex: number): void {
    const key = `${estudanteIndex}-${periodoIndex}`;
    this.collapseEstagiosAndamento[key] = !this.collapseEstagiosAndamento[key];
  }
}