import { Component } from '@angular/core';
import { EstudanteService } from '../../services/estudante.service';
import { EstudanteDto } from '../../models/estudante.dto';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-estudante',
  standalone: true,
  imports: [FormsModule, CommonModule, HttpClientModule],
  templateUrl: './estudante.component.html',
  styleUrl: './estudante.component.scss'
})
export class EstudanteComponent {
  estudante: EstudanteDto | any = null;
  nomeEstudante: string = '';
  

  constructor(private estudanteService: EstudanteService) {}

  buscarEstagio() {
    if (this.nomeEstudante) {
      this.estudanteService.buscarEstagioPorNome(this.nomeEstudante)
        .subscribe({
          next: (data: any) => this.estudante = data,
          error: (err: any) => console.error('Erro ao buscar estágio:', err)
        });
    }
  }

  formatarData(data: Date): string {
    return new Date(data).toLocaleDateString('pt-BR');
  }
}
