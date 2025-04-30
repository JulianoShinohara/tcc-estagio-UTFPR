package com.utfpr.estagio.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utfpr.estagio.service.GoogleSheetsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {

	private final GoogleSheetsService sheetsService;

    @GetMapping("read")
    public List<List<Object>> getRawSheetData() throws IOException {
        return sheetsService.getEstudantesFromSheet();
    }
	
}