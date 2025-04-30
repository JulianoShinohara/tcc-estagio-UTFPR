package com.utfpr.estagio.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class CalendarioAcademicoService {
	
	public int getSemestre(LocalDate terminoEstagio) {
	    int semestre = terminoEstagio.getMonthValue();
	    return (semestre >= 1 && semestre <= 7) ? 1 : 2;
	}
	
    public LocalDate getFinalSemestre(int ano, int semestre) {
        return semestre == 1 
            ? LocalDate.of(ano, 7, 10)   // 10/07 para 1º semestre
            : LocalDate.of(ano, 12, 20); // 20/12 para 2º semestre
    }
    
    public LocalDate getInicioProximoSemestre(int ano, int semestre) {
        return semestre == 1
            ? LocalDate.of(ano, 8, 10)   // 10/08 para 2º semestre
            : LocalDate.of(ano + 1, 3, 9); // 09/03 do próximo ano para 1º semestre
    }
}
