package com.utfpr.estagio.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalendarioAcademicoService {

	@Autowired
	private GoogleSheetsService googleSheetsService;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public SemestreAcademico identificarSemestre(LocalDate data) throws IOException {
	    List<List<Object>> valores = googleSheetsService.getCalendarioAcademicoFromSheet();
	    
	    for (int i = 1; i < valores.size(); i++) {
	        List<Object> linha = valores.get(i);
	        
	        
	        try {
	            LocalDate inicio = LocalDate.parse(linha.get(2).toString(), DATE_FORMATTER);
	            LocalDate fim = LocalDate.parse(linha.get(3).toString(), DATE_FORMATTER);
	            
	            if (linha.size() > 4 && !linha.get(4).toString().trim().isEmpty()) {
	                LocalDate inicioSemestreSeguinte = LocalDate.parse(linha.get(4).toString(), DATE_FORMATTER);
	                if (!data.isBefore(inicio) && !data.isAfter(inicioSemestreSeguinte)) {
	                    return new SemestreAcademico(
	                        Integer.parseInt(linha.get(0).toString()),
	                        Integer.parseInt(linha.get(1).toString()),
	                        inicio,
	                        fim,
	                        inicioSemestreSeguinte
	                    );
	                }
	            } else {
	                if (!data.isBefore(inicio) && !data.isAfter(fim)) {
	                    return new SemestreAcademico(
	                        Integer.parseInt(linha.get(0).toString()),
	                        Integer.parseInt(linha.get(1).toString()),
	                        inicio,
	                        fim,
	                        null
	                    );
	                }
	            }
	        } catch (Exception e) {
	            throw e;
	        }
	    }
	    throw new RuntimeException("Data " + data + " não está em nenhum semestre cadastrado");
	}

	public record SemestreAcademico(int ano, int semestre, LocalDate dataInicio, LocalDate dataFinal,
			LocalDate dataInicioProximoSemestre) {
	}
	
	public SemestreInfo identificarPeriodoAvaliacao(LocalDate terminoEstagio) throws IOException {
	    SemestreAcademico semestreAtual = identificarSemestre(terminoEstagio);
	    
	    LocalDate duasSemanasAntesFinal = semestreAtual.dataFinal().minusWeeks(2);
	    
	    if (terminoEstagio.isBefore(duasSemanasAntesFinal)) {
	        return new SemestreInfo(
	            terminoEstagio.plusDays(1),
	            semestreAtual.dataFinal(),
	            false
	        );
	    } else {
	        SemestreAcademico proximoSemestre = getSemestreInfo(
	            semestreAtual.ano() + (semestreAtual.semestre() == 2 ? 1 : 0),
	            semestreAtual.semestre() == 1 ? 2 : 1
	        );
	        
	        return new SemestreInfo(
	            proximoSemestre.dataInicio(),
	            proximoSemestre.dataInicio().plusDays(14),
	            true
	        );
	    }
	}
	
	public SemestreAcademico getSemestreInfo(int ano, int semestre) throws IOException {
	    List<List<Object>> valores = googleSheetsService.getCalendarioAcademicoFromSheet();
	    
	    for (List<Object> linha : valores) {
	        try {
	            if (linha.size() < 5) continue;
	            
	            int linhaAno = Integer.parseInt(linha.get(0).toString().trim());
	            int linhaSemestre = Integer.parseInt(linha.get(1).toString().trim());
	            
	            if (linhaAno == ano && linhaSemestre == semestre) {
	                LocalDate dataInicio = parseDate(linha.get(2).toString().trim());
	                LocalDate dataFinal = parseDate(linha.get(3).toString().trim());
	                LocalDate proximoSemestre = parseDate(linha.get(4).toString().trim());
	                
	                return new SemestreAcademico(
	                    linhaAno,
	                    linhaSemestre,
	                    dataInicio,
	                    dataFinal,
	                    proximoSemestre
	                );
	            }
	        } catch (Exception e) {}
	    }
	    throw new RuntimeException("Semestre " + semestre + "/" + ano + " não encontrado");
	}

	private LocalDate parseDate(String dateStr) {
	    try {
	        return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
	    } catch (Exception e) {
	        throw new RuntimeException("Formato de data inválido na planilha: " + dateStr);
	    }
	}

	public record SemestreInfo(
	    LocalDate inicioAvaliacao,
	    LocalDate fimAvaliacao,
	    boolean periodoEstendido
	) {}
}