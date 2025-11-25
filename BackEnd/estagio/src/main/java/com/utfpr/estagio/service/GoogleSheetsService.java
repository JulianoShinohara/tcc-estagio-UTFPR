package com.utfpr.estagio.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

@Service
public class GoogleSheetsService {

	@Autowired
    private final Sheets sheetsService;
    
    private final String spreadsheetId = "1DwWQaW7U5ap8THvKoed8ozUu4xWyBk-jX3VTfJtTeYc";
    private final String rangeEstagio = "Estágios!A1:Z";
    private final String rangeRelatorio = "Relatórios!A1:Z";
    private final String rangeSemestre = "Semestre!A1:Z";
    private final String orientadores = "Orientadores!A1:Z";
    
    private String diretorioPlanilhas = "src/main/resources/archives";
    
    private boolean usarLocal = true;

    public GoogleSheetsService(Sheets sheetsService) {
        this.sheetsService = sheetsService;
    }

    public List<List<Object>> getEstudantesFromSheet() throws IOException {
        if (usarLocal) {
            return carregarPlanilhaLocal("estagios");
        } else {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, rangeEstagio)
                    .execute();
            
            salvarPlanilhaLocal("estagios", response.getValues());
            
            return response.getValues();
        }
    }

    public List<List<Object>> getRelatoriosEnviadosFromSheet() throws IOException {
        if (usarLocal) {
            return carregarPlanilhaLocal("relatorios");
        } else {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, rangeRelatorio)
                    .execute();
                    
            salvarPlanilhaLocal("relatorios", response.getValues());
            
            return response.getValues();
        }
    }

    public List<List<Object>> getCalendarioAcademicoFromSheet() throws IOException {
        if (usarLocal) {
            return carregarPlanilhaLocal("semestre");
        } else {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, rangeSemestre)
                    .execute();
                    
            salvarPlanilhaLocal("semestre", response.getValues());
            
            return response.getValues();
        }
    }

    public List<List<Object>> getOrientadoresFromSheet() throws IOException {
        if (usarLocal) {
            return carregarPlanilhaLocal("orientadores");
        } else {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, rangeSemestre)
                    .execute();
                    
            salvarPlanilhaLocal("orientadores", response.getValues());
            
            return response.getValues();
        }
    }

    /**
     * Baixa todas as planilhas do Google Sheets e salva localmente
     */
    public void baixarPlanilhas() throws IOException {
        deletarEBaixar("estagios", getEstudantesFromSheetOnline());
        deletarEBaixar("relatorios", getRelatoriosEnviadosFromSheetOnline());
        deletarEBaixar("semestre", getCalendarioAcademicoFromSheetOnline());
        deletarEBaixar("orientadores", getOrientadoresFromSheetOnline());
    }

    private void deletarEBaixar(String nomePlanilha, List<List<Object>> dados) throws IOException {
        Path caminho = Paths.get(diretorioPlanilhas, nomePlanilha + ".csv");
        
        if (Files.exists(caminho)) {
            Files.delete(caminho);
        }
        
        salvarPlanilhaLocal(nomePlanilha, dados);
    }

    public List<List<Object>> getEstudantesFromSheetOnline() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, rangeEstagio)
                .execute();
        return response.getValues();
    }

    public List<List<Object>> getRelatoriosEnviadosFromSheetOnline() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, rangeRelatorio)
                .execute();
        return response.getValues();
    }

    public List<List<Object>> getCalendarioAcademicoFromSheetOnline() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, rangeSemestre)
                .execute();
        return response.getValues();
    }

    public List<List<Object>> getOrientadoresFromSheetOnline() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, orientadores)
                .execute();
        return response.getValues();
    }

    private void salvarPlanilhaLocal(String nomePlanilha, List<List<Object>> dados) throws IOException {
        Path diretorio = Paths.get(diretorioPlanilhas);
        if (!Files.exists(diretorio)) {
            Files.createDirectories(diretorio);
        }
        
        File arquivo = new File(diretorioPlanilhas + File.separator + nomePlanilha + ".csv");
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            for (List<Object> linha : dados) {
                String linhaCSV = linha.stream()
                        .map(Object::toString)
                        .map(s -> s.trim())
                        .collect(Collectors.joining(";")) + "\n";
                fos.write(linhaCSV.getBytes("UTF-8"));
            }
        }
    }

    /**
     * Carrega planilha local em formato CSV com suporte a campos com ponto e vírgula entre aspas
     */
    private List<List<Object>> carregarPlanilhaLocal(String nomePlanilha) throws IOException {
        Path caminho = Paths.get(diretorioPlanilhas, nomePlanilha + ".csv");
        
        if (!Files.exists(caminho)) {
            return atualizarPlanilhaLocal(nomePlanilha);
        }
        
        return Files.readAllLines(caminho).stream()
                .map(linha -> {
                    List<Object> objetos = new ArrayList<>();
                    boolean entreAspas = false;
                    StringBuilder campoAtual = new StringBuilder();
                    
                    for (char c : linha.toCharArray()) {
                        if (c == '"') {
                            entreAspas = !entreAspas;
                        } else if (c == ';' && !entreAspas) {
                            objetos.add(campoAtual.toString());
                            campoAtual = new StringBuilder();
                        } else {
                            campoAtual.append(c);
                        }
                    }
                    objetos.add(campoAtual.toString());
                    return objetos;
                })
                .collect(Collectors.toList());
    }

    private List<List<Object>> atualizarPlanilhaLocal(String nomePlanilha) throws IOException {
        List<List<Object>> dados;
        
        switch (nomePlanilha) {
            case "estagios":
                dados = getEstudantesFromSheetOnline();
                break;
            case "relatorios":
                dados = getRelatoriosEnviadosFromSheetOnline();
                break;
            case "semestre":
                dados = getCalendarioAcademicoFromSheetOnline();
                break;
            default:
                throw new IOException("Planilha desconhecida: " + nomePlanilha);
        }
        
        salvarPlanilhaLocal(nomePlanilha, dados);
        return dados;
    }
}