package com.utfpr.estagio.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de tarefas agendadas para automação de processos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

	private final GoogleSheetsService googleSheetsService;
	private final EmailNotificationService emailNotificationService;

	/**
	 * Baixa as planilhas automaticamente todos os dias às 9 horas da manhã
	 */
	@Scheduled(cron = "0 0 9 * * *", zone = "America/Sao_Paulo")
	public void executarDownloadPlanilhasDiariamente() {
		LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));

		try {
			log.info("Iniciando download automático de planilhas às {}", agora);
			googleSheetsService.baixarPlanilhas();
			log.info("Download de planilhas concluído com sucesso às {}",
					LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
		} catch (IOException e) {
			log.error("Erro ao executar download automático de planilhas às {}: {}", agora, e.getMessage(), e);
		} catch (Exception e) {
			log.error("Erro inesperado ao executar download automático de planilhas: {}", e.getMessage(), e);
		}
	}

	/**
	 * Envia lembretes de relatórios pendentes automaticamente todos os dias às 9 horas da manhã
	 */
	@Scheduled(cron = "0 0 9 * * *", zone = "America/Sao_Paulo")
	public void executarEnvioLembretesDiariamente() {
		LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));

		try {
			log.info("Iniciando envio de lembretes diários às {}", agora);
			emailNotificationService.enviarLembretesDiarios();
			log.info("Envio de lembretes concluído com sucesso às {}",
					LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
		} catch (IOException e) {
			log.error("Erro ao executar envio de lembretes às {}: {}", agora, e.getMessage(), e);
		} catch (Exception e) {
			log.error("Erro inesperado ao executar envio de lembretes: {}", e.getMessage(), e);
		}
	}
}
