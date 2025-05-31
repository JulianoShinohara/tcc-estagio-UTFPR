package com.utfpr.estagio.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utfpr.estagio.service.GoogleSheetsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {

	private final GoogleSheetsService googleSheetsService;

    @GetMapping("read")
    public List<List<Object>> getRawSheetData() throws IOException {
        return googleSheetsService.getEstudantesFromSheet();
    }
	
    @PostMapping("/baixar")
    public ResponseEntity<String> baixarPlanilhas() {
        try {
        	googleSheetsService.baixarPlanilhas();
            return ResponseEntity.ok("Planilhas baixadas com sucesso!");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body("Erro ao baixar planilhas: " + e.getMessage());
        }
    }
}