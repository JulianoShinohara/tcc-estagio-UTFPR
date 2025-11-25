package com.utfpr.estagio.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utfpr.estagio.dto.EstudanteDto;
import com.utfpr.estagio.service.OrientadorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orientador")
@RequiredArgsConstructor
public class OrientadorController {

	@Autowired
	private final OrientadorService orientadorService;

	@GetMapping("/nomes")
	public ResponseEntity<List<String>> getAllOrientadoresNomes() {
		try {
			List<String> orientadores = orientadorService.getAllOrientadoresNomes();
			return ResponseEntity.ok(orientadores);

		} catch (Exception e) {
			System.err.println("Erro ao buscar nomes dos orientadores: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GetMapping("/{nomeOrientador}/estagios")
    public ResponseEntity<List<EstudanteDto>> getEstagiosByOrientador(
            @PathVariable String nomeOrientador) {
        try {
            // Decodificar o nome do orientador (caso tenha caracteres especiais)
            String nomeDecodificado = java.net.URLDecoder.decode(nomeOrientador, "UTF-8");
            
            List<EstudanteDto> estagios = orientadorService.getEstagiosByOrientador(nomeDecodificado);
            return ResponseEntity.ok(estagios);
            
        } catch (IllegalArgumentException e) {
            // Nome do orientador inválido
            System.err.println("Nome do orientador inválido: " + e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (IOException e) {
            // Erro de I/O ao acessar planilhas
            System.err.println("Erro de I/O ao buscar estágios: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            
        } catch (Exception e) {
            // Log do erro (use um logger apropriado em produção)
            System.err.println("Erro ao buscar estágios para orientador '" + nomeOrientador + "': " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
