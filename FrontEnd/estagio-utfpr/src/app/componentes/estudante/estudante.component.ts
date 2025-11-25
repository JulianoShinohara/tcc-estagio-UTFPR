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
          this.estudante = data;
          this.isLoading = false;
        },
        error: (err: any) => {
          this.errorMessage = 'Estudante não encontrado ou erro na consulta';
          this.isLoading = false;
        }
      });
  }
}