package com.utfpr.estagio.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utfpr.estagio.dto.EstudanteDto;
import com.utfpr.estagio.service.EmailNotificationService;
import com.utfpr.estagio.service.EstudanteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/estudante")
@RequiredArgsConstructor
public class EstudanteController {

	@Autowired
	private final EstudanteService estudanteService;
	
	@Autowired
	private final EmailNotificationService emailNotificationService;
	

	@GetMapping("/{nome}")
	public ResponseEntity<EstudanteDto> getEstagio(@PathVariable String nome) throws Exception {
		try {
			String nomeDecodificado = URLDecoder.decode(nome, StandardCharsets.UTF_8.name());
			return estudanteService.getEstagioPorNome(nomeDecodificado).map(ResponseEntity::ok)
					.orElseGet(() -> ResponseEntity.notFound().build());
		} catch (IOException e) {
			throw e;
		}
	}
	
	@PostMapping("/notifica")
	public ResponseEntity<String> notificação() throws Exception {
		try {
			emailNotificationService.enviarLembretesDiarios();
			return ResponseEntity.ok("ENVIOU");
		} catch (Exception e) {
			throw e;
		}
	}
	
}
