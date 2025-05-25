import { Component } from '@angular/core';
import { EstudanteService } from '../../services/estudante.service';
import { EstudanteDto, PeriodoEstagioDto } from '../../models/estudante.dto';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgIf, NgFor, DatePipe } from '@angular/common';

@Component({
  selector: 'app-estudante',
  standalone: true,
  imports: [FormsModule, CommonModule, HttpClientModule, NgIf, NgFor, DatePipe],
  templateUrl: './estudante.component.html',
  styleUrl: './estudante.component.scss'
})
export class EstudanteComponent {
  estudante: EstudanteDto | null = null;
  nomeEstudante: string = '';
  isLoading: boolean = false;
  errorMessage: string | null = null;

  constructor(private estudanteService: EstudanteService) {}

  buscarEstagio() {
    if (!this.nomeEstudante.trim()) return;
    this.isLoading = true;
    this.errorMessage = null;
    this.estudante = null;

    this.estudanteService.buscarEstagioPorNome(this.nomeEstudante)
      .subscribe({
        next: (data: EstudanteDto) => {
          console.log('Dados recebidos do backend:', JSON.parse(JSON.stringify(data)));
          this.estudante = this.parseDates(data);
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Erro ao buscar estágio:', err);
          this.errorMessage = 'Estudante não encontrado ou erro na consulta';
          this.isLoading = false;
        }
      });
  }

  // Método para converter strings de data para objetos Date
  private parseDates(estudante: EstudanteDto): EstudanteDto {
    return {
      ...estudante,
      periodosEstagio: estudante.periodosEstagio.map(periodo => ({
        ...periodo,
        empresa: periodo.empresa,
        obrigatorio: periodo.obrigatorio,
        dataInicioEstagio: this.convertStringToDate(periodo.dataInicioEstagio),
        dataTerminoEstagio: this.convertStringToDate(periodo.dataTerminoEstagio),
        datasRelatoriosParciaisAluno: periodo.datasRelatoriosParciaisAluno.map(rel => ({
          ...rel,
          inicioPeriodoRelatorio: this.convertStringToDate(rel.inicioPeriodoRelatorio),
          fimPeriodoRelatorio: this.convertStringToDate(rel.fimPeriodoRelatorio)
        })),
        datasRelatoriosParciaisOrientador: periodo.datasRelatoriosParciaisOrientador.map(rel => ({
          ...rel,
          inicioPeriodoRelatorio: this.convertStringToDate(rel.inicioPeriodoRelatorio),
          fimPeriodoRelatorio: this.convertStringToDate(rel.fimPeriodoRelatorio)
        })),
        dataRelatorioVisita: periodo.dataRelatorioVisita ? this.convertStringToDate(periodo.dataRelatorioVisita) : undefined,
        dataRelatorioFinal: this.convertStringToDate(periodo.dataRelatorioFinal),
        intervaloAvaliacao: periodo.intervaloAvaliacao ? {
          dataInicio: this.convertStringToDate(periodo.intervaloAvaliacao.dataInicio),
          dataFim: this.convertStringToDate(periodo.intervaloAvaliacao.dataFim)
        } : undefined
        
      }))
    };
    
  }

  // Converte string no formato dd/MM/yyyy para Date
  private convertStringToDate(dateString: string | Date): Date {
    if (dateString instanceof Date) {
      return dateString;
    }
    
    if (!dateString) return new Date();
    
    const [day, month, year] = dateString.split('/');
    return new Date(+year, +month - 1, +day);
  }

  periodoPossuiVisita(periodo: PeriodoEstagioDto): boolean {
    return !!periodo.dataRelatorioVisita;
  }

  periodoPossuiAvaliacao(periodo: PeriodoEstagioDto): boolean {
    return !!periodo.intervaloAvaliacao;
  }

  // Método auxiliar para formatar datas diretamente no componente
  formatarData(data: Date | string | undefined): string {
    if (!data) return '';
    
    const dateObj = data instanceof Date ? data : this.convertStringToDate(data);
    return dateObj.toLocaleDateString('pt-BR');
  }
}