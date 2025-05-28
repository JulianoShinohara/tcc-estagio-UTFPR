package com.utfpr.estagio.service;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.utfpr.estagio.dto.AvaliacaoDto;
import com.utfpr.estagio.dto.DatasRelatoriosParciaisDto;
import com.utfpr.estagio.dto.EstudanteDto;

@Service
public class EmailNotificationService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private GoogleSheetsService googleSheetsService;

	@Autowired
	private EstudanteService estudanteService;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final int COL_NOME = 0; // "Nome Estagiário"
	private static final int COL_EMAIL = 1; // "Email"

//	@Scheduled(cron = "0 0 9 * * ?") // Executa todos os dias às 9h
	public void enviarLembretesDiarios() throws IOException {
		LocalDate semanaSeguinte = LocalDate.now().plusDays(7);

		List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();

		for (List<Object> linha : valores) {
			String nome = normalizarString(linha.get(COL_NOME).toString());
			String email = linha.get(COL_EMAIL).toString();

			Optional<EstudanteDto> estudanteDtoOpt = estudanteService.getEstagioPorNome(nome);

			if (estudanteDtoOpt.isPresent()) {
				EstudanteDto estudanteDto = estudanteDtoOpt.get();

				for (EstudanteDto.PeriodoEstagioDto periodo : estudanteDto.getPeriodosEstagio()) {
					// relatórios parciais do aluno
					for (DatasRelatoriosParciaisDto relatorio : periodo
							.getDatasRelatoriosParciaisAluno()) {
						if (relatorio.getFimPeriodoRelatorio().equals(semanaSeguinte) && !relatorio.isEnviado()) {
							enviarEmailRelatorioParcial(email, nome, relatorio.getFimPeriodoRelatorio(), "aluno");
						}
					}

					// relatórios parciais do orientador
					for (DatasRelatoriosParciaisDto relatorio : periodo
							.getDatasRelatoriosParciaisOrientador()) {
						if (relatorio.getFimPeriodoRelatorio().equals(semanaSeguinte) && !relatorio.isEnviado()) {
							enviarEmailRelatorioParcial(email, nome, relatorio.getFimPeriodoRelatorio(), "orientador");
						}
					}

					// relatório de visita
					if (periodo.getObrigatorio() && periodo.getDataRelatorioVisita() != null
							&& periodo.getDataRelatorioVisita().equals(semanaSeguinte)
							&& !periodo.isEnviadoRelatorioVisita()) {
						enviarEmailRelatorioVisita(email, nome, periodo.getDataRelatorioVisita());
					}

					// relatório final
					if (periodo.getDataRelatorioFinal() != null
							&& periodo.getDataRelatorioFinal().equals(semanaSeguinte)
							&& !periodo.isEnviadoRelatorioFinal()) {
						enviarEmailRelatorioFinal(email, nome, periodo.getDataRelatorioFinal());
					}

					// intervalo de avaliação
					if (periodo.getObrigatorio() && periodo.getIntervaloAvaliacao() != null) {
						AvaliacaoDto avaliacao = periodo.getIntervaloAvaliacao();
						if (avaliacao.getDataInicio().minusDays(7).equals(LocalDate.now())) {
							enviarEmailAvaliacao(email, nome, avaliacao.getDataInicio(), avaliacao.getDataFim());
						}
					}
				}
			}
		}
	}

	private void enviarEmailRelatorioParcial(String email, String nome, LocalDate dataEntrega, String tipo) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Lembrete: Relatório Parcial de Estágio");

		String texto = String.format("Olá %s,\n\n"
				+ "Este é um lembrete amigável de que seu relatório parcial de estágio (%s) deve ser entregue em uma semana, no dia %s.\n\n"
				+ "Por favor, não deixe para a última hora!\n\n" + "Atenciosamente,\n" + "Coordenação de Estágios",
				nome, tipo, dataEntrega.format(DATE_FORMATTER));

		message.setText(texto);
		mailSender.send(message);
	}

	private void enviarEmailRelatorioVisita(String email, String nome, LocalDate dataEntrega) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Lembrete: Relatório de Visita à UCE");

		String texto = String.format("Olá %s,\n\n"
				+ "Este é um lembrete de que seu relatório de visita à UCE deve ser entregue em uma semana, no dia %s.\n\n"
				+ "Por favor, prepare-se com antecedência!\n\n" + "Atenciosamente,\n" + "Coordenação de Estágios", nome,
				dataEntrega.format(DATE_FORMATTER));

		message.setText(texto);
		mailSender.send(message);
	}

	private void enviarEmailRelatorioFinal(String email, String nome, LocalDate dataEntrega) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Lembrete: Relatório Final de Estágio");

		String texto = String.format("Olá %s,\n\n"
				+ "Este é um lembrete importante de que seu relatório final de estágio deve ser entregue em uma semana, no dia %s.\n\n"
				+ "Este é um documento crucial para a conclusão do seu estágio.\n\n" + "Atenciosamente,\n"
				+ "Coordenação de Estágios", nome, dataEntrega.format(DATE_FORMATTER));

		message.setText(texto);
		mailSender.send(message);
	}

	private void enviarEmailAvaliacao(String email, String nome, LocalDate inicio, LocalDate fim) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Lembrete: Período de Avaliação de Estágio");

		String texto = String.format("Olá %s,\n\n"
				+ "Este é um lembrete de que o período de avaliação do seu estágio ocorrerá entre %s e %s.\n\n"
				+ "Por favor, prepare-se para esta etapa importante.\n\n" + "Atenciosamente,\n"
				+ "Coordenação de Estágios", nome, inicio.format(DATE_FORMATTER), fim.format(DATE_FORMATTER));

		message.setText(texto);
		mailSender.send(message);
	}

	private String normalizarString(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
	}
}
