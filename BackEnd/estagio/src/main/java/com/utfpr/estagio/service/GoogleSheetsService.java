package com.utfpr.estagio.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

	@Autowired
	private final Sheets sheetsService;
	
	String spreadsheetId = "1DwWQaW7U5ap8THvKoed8ozUu4xWyBk-jX3VTfJtTeYc";
	String rangeEstagio = "Estágios!A1:Z";
	String rangeRelatorio = "Relatórios!A1:Z";
	String rangeSemestre = "Semestre!A1:Z";

	public List<List<Object>> getEstudantesFromSheet() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, rangeEstagio)
                .execute();
        
        return response.getValues();
    }
	
	
	public List<List<Object>> getRelatoriosEnviadosFromSheet() throws IOException {
	    ValueRange response = sheetsService.spreadsheets().values()
	            .get(spreadsheetId, rangeRelatorio)
	            .execute();
	    return response.getValues();
	}
	
	public List<List<Object>> getCalendarioAcademicoFromSheet() throws IOException {
	    ValueRange response = sheetsService.spreadsheets().values()
	            .get(spreadsheetId, rangeSemestre)
	            .execute();
	    return response.getValues();
	}
}