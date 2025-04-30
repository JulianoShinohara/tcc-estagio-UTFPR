package com.utfpr.estagio.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utfpr.estagio.dto.AvaliacaoDto;
import com.utfpr.estagio.dto.EstudanteDto;
import com.utfpr.estagio.dto.EstudanteDto.PeriodoEstagioDto.DatasRelatoriosParciaisAlunoDto;
import com.utfpr.estagio.dto.EstudanteDto.PeriodoEstagioDto.DatasRelatoriosParciaisOrientadorDto;

@Service
public class EstudanteService {

	@Autowired
	private GoogleSheetsService googleSheetsService;

	@Autowired
	private CalendarioAcademicoService calendarioService;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final int COL_NOME = 0; // "Nome Estagiário"
	private static final int COL_INICIO = 6; // "Início do Estágio"
	private static final int COL_TERMINO = 7; // "Término do Estágio"
	private static final int COL_CARGA_HORARIA_SEMANAL = 11; // Horas trabalhadas por semana

	// Quantidade de meses para calcular o periodo de entrega do relatorio parcial
	private static final int QTDE_MES_RELATORIO_PARCIAL = 6; 

	// Caso a pessoa não tenha cadastrado a quantidade de horas semanais trabalhado,
	// é definido "QTDE_DIAS_VISITA_TECNICA_PADRÃO" (30 dias) por padrão.
	// Caso tenha a quantidade de horas semanais é calculado
	// "HORAS_TOTAIS_RELATORIO_VISITA" (100 horas) trabalhado para emitir a data.
	private static final int QTDE_DIAS_VISITA_TECNICA_PADRAO = 30;
	private static final int HORAS_TOTAIS_RELATORIO_VISITA = 100; // Quantidade de horas trabalhada necessaria para o
																	// relatorio da visita.

	/**
	 * 
	 * @param nomeEstudante
	 * @return
	 * @throws IOException
	 */
	public Optional<EstudanteDto> getEstagioPorNome(String nomeEstudante) throws IOException {
		String nomeDecodificado = URLDecoder.decode(nomeEstudante, StandardCharsets.UTF_8);
		List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();

		List<EstudanteDto.PeriodoEstagioDto> periodos = new ArrayList<>();

		for (List<Object> linha : valores) {
			if (nomeDecodificado.equalsIgnoreCase(linha.get(COL_NOME).toString())) {
				LocalDate inicio = LocalDate.parse(linha.get(COL_INICIO).toString(), DATE_FORMATTER);
				LocalDate termino = LocalDate.parse(linha.get(COL_TERMINO).toString(), DATE_FORMATTER);
				int cargaHorariaSemanal = Integer.parseInt((String) linha.get(COL_CARGA_HORARIA_SEMANAL));
				
		        AvaliacaoDto intervalo = calcularIntervaloAvaliacao(
		            termino,
		            calendarioService.getSemestre(termino),
		            termino.getYear()
		        );

				EstudanteDto.PeriodoEstagioDto periodo = EstudanteDto.PeriodoEstagioDto.builder()
						.dataInicioEstagio(inicio).dataTerminoEstagio(termino)
						.datasRelatoriosParciaisAluno(calcularPeriodosRelatoriosAluno(inicio, termino))
						.datasRelatoriosParciaisOrientador(calcularPeriodosRelatoriosOrientador(inicio, termino))
						.dataRelatorioVisita(calcularDataRelatorioVisita(inicio, cargaHorariaSemanal))
						.dataRelatorioFinal(calcularDataRelatorioFinal(termino))
						.intervaloAvaliacao(intervalo)
						.build();

				periodos.add(periodo);
			}
		}

		if (periodos.isEmpty()) {
			return Optional.empty();
		}

		EstudanteDto estudanteDto = EstudanteDto.builder().nomeEstudante(nomeDecodificado).periodosEstagio(periodos)
				.build();

		return Optional.of(estudanteDto);
	}

	/**
	 * 
	 * @param inicio
	 * @param termino
	 * @return
	 */
	private List<DatasRelatoriosParciaisAlunoDto> calcularPeriodosRelatoriosAluno(LocalDate inicio, LocalDate termino) {
		List<DatasRelatoriosParciaisAlunoDto> periodos = new ArrayList<>();
		LocalDate inicioPeriodo = inicio;
		LocalDate fimPeriodo = inicio.plusMonths(QTDE_MES_RELATORIO_PARCIAL).minusDays(1);

		while (inicioPeriodo.isBefore(termino)) {
			if (fimPeriodo.isAfter(termino)) {
				fimPeriodo = termino;
			}

			periodos.add(DatasRelatoriosParciaisAlunoDto.builder().inicioPeriodoRelatorio(inicioPeriodo)
					.fimPeriodoRelatorio(fimPeriodo).build());

			inicioPeriodo = fimPeriodo.plusDays(1);
			fimPeriodo = inicioPeriodo.plusMonths(QTDE_MES_RELATORIO_PARCIAL).minusDays(1);
		}

		return periodos;
	}

	/**
	 * 
	 * @param inicio
	 * @param termino
	 * @return
	 */
	private List<DatasRelatoriosParciaisOrientadorDto> calcularPeriodosRelatoriosOrientador(LocalDate inicio,
			LocalDate termino) {
		List<DatasRelatoriosParciaisOrientadorDto> periodos = new ArrayList<>();
		LocalDate inicioPeriodo = inicio;
		LocalDate fimPeriodo = inicio.plusMonths(QTDE_MES_RELATORIO_PARCIAL).minusDays(1);

		while (inicioPeriodo.isBefore(termino)) {
			if (fimPeriodo.isAfter(termino)) {
				fimPeriodo = termino;
			}

			periodos.add(DatasRelatoriosParciaisOrientadorDto.builder().inicioPeriodoRelatorio(inicioPeriodo)
					.fimPeriodoRelatorio(fimPeriodo).build());

			inicioPeriodo = fimPeriodo.plusDays(1);
			fimPeriodo = inicioPeriodo.plusMonths(QTDE_MES_RELATORIO_PARCIAL).minusDays(1);
		}

		return periodos;
	}

	/**
	 * 
	 * @param dataInicio
	 * @return
	 */
	private LocalDate calcularDataRelatorioVisita(LocalDate dataInicio, int cargaHorariaSemanal) {
		if (cargaHorariaSemanal <= 0) {
			return dataInicio.plusDays(QTDE_DIAS_VISITA_TECNICA_PADRAO);
		}

		// Calcula quantas semanas são necessárias para atingir 100 horas
		double semanasNecessarias = (double) HORAS_TOTAIS_RELATORIO_VISITA / cargaHorariaSemanal;
		int diasNecessarios = (int) Math.ceil(semanasNecessarias * 7);

		return dataInicio.plusDays(diasNecessarios);
	}

	/**
	 * 
	 * @param dataTermino
	 * @return
	 */
	private LocalDate calcularDataRelatorioFinal(LocalDate dataTermino) {
		return dataTermino.plusDays(7);
	}

	public AvaliacaoDto calcularIntervaloAvaliacao(LocalDate terminoEstagio, int semestre, int ano) {
		LocalDate finalSemestre = calendarioService.getFinalSemestre(ano, semestre);
		LocalDate duasSemanasAntes = finalSemestre.minusWeeks(2);

		// Caso Normal: Pós-término até final do semestre
		if (terminoEstagio.isBefore(duasSemanasAntes)) {
			return new AvaliacaoDto(terminoEstagio.plusDays(1), finalSemestre);
		} else {
			// Caso Especial: 1º ao 15º dia do próximo semestre
			int proximoSemestre = semestre == 1 ? 2 : 1;
			int proximoAno = semestre == 1 ? ano : ano + 1;
			LocalDate inicioProximoSemestre = calendarioService.getInicioProximoSemestre(proximoAno, proximoSemestre);

			return new AvaliacaoDto(inicioProximoSemestre, inicioProximoSemestre.plusDays(14));
		}
	}
}
