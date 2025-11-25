import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, Observable } from 'rxjs';
import { EstudanteDto } from '../models/estudante.dto';

@Injectable({
  providedIn: 'root'
})
export class EstudanteService {
  private apiUrl = 'http://localhost:8080';
  private http: HttpClient = inject(HttpClient);

  constructor() { }

  buscarEstagioPorNome(nome: string): Observable<EstudanteDto> {

    const nomeDecodificado = decodeURIComponent(nome);

    return this.http.get<EstudanteDto>(`${this.apiUrl}/estudante/${encodeURIComponent(nomeDecodificado)}`)
    .pipe(
      catchError(error => {
        throw new Error('Falha ao buscar estudante. Tente novamente.');
      })
    );
  }
}
