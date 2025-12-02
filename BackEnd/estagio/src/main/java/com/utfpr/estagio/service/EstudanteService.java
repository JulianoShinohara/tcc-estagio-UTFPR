package com.utfpr.estagio.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utfpr.estagio.dto.AvaliacaoDto;
import com.utfpr.estagio.dto.DatasRelatoriosParciaisDto;
import com.utfpr.estagio.dto.EstudanteDto;
import com.utfpr.estagio.dto.EstudanteDto.PeriodoEstagioDto;
import com.utfpr.estagio.service.CalendarioAcademicoService.SemestreAcademico;

@Service
public class EstudanteService {

	@Autowired
	private GoogleSheetsService googleSheetsService;

	@Autowired
	private CalendarioAcademicoService calendarioService;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final int COL_NOME = 0;
	private static final int COL_EMPRESA = 5;
	private static final int COL_INICIO = 6;
	private static final int COL_TERMINO = 7;
	private static final int COL_CARGA_HORARIA_SEMANAL = 11;
	private static final int COL_BOOL_OBRIGATORIO = 13;

	private static final int COL_NOME_RELATORIO = 0;
	private static final int COL_TIPO_RELATORIO = 2;
	private static final int COL_DATA_INICIO_RELATORIO = 3;
	private static final int COL_DATA_FIM_RELATORIO = 4;

	private static final int QTDE_MES_RELATORIO_PARCIAL = 6;

	/**
	 * Lógica para cálculo de data do relatório de visita:
	 * - Se não informado carga horária: usa 30 dias padrão
	 * - Se informado: calcula baseado em 100 horas trabalhadas
	 */
	private static final int QTDE_DIAS_VISITA_TECNICA_PADRAO = 30;
	private static final int HORAS_TOTAIS_RELATORIO_VISITA = 100;

	/**
	 * Busca informações de estágios de um estudante pelo nome
	 */
	public Optional<EstudanteDto> getEstagioPorNome(String nomeEstudante) throws IOException {
		String nomeDecodificado = URLDecoder.decode(nomeEstudante, StandardCharsets.UTF_8);
		String nomeNormalizado = normalizarString(nomeDecodificado);
		List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();
		List<List<Object>> relatoriosEnviados = googleSheetsService.getRelatoriosEnviadosFromSheet();

		List<EstudanteDto.PeriodoEstagioDto> periodos = new ArrayList<>();

		List<List<Object>> valoresSemCabecalho = valores.subList(1, valores.size());
		for (List<Object> linha : valoresSemCabecalho) {
			
			if (nomeNormalizado.equalsIgnoreCase(normalizarString(linha.get(COL_NOME).toString()))) {

				LocalDate inicio = LocalDate.parse(linha.get(COL_INICIO).toString(), DATE_FORMATTER);
				LocalDate termino = LocalDate.parse(linha.get(COL_TERMINO).toString(), DATE_FORMATTER);
				String nomeEmpresa = linha.get(COL_EMPRESA).toString();
				int cargaHorariaSemanal = Integer.parseInt((String) linha.get(COL_CARGA_HORARIA_SEMANAL));

				boolean obrigatorio = "sim".equalsIgnoreCase(linha.get(COL_BOOL_OBRIGATORIO).toString());
				
				
				List<DatasRelatoriosParciaisDto> relatoriosEnviadosAluno = relatoriosEnviados.stream()
		                .filter(r -> nomeNormalizado.equalsIgnoreCase(normalizarString(r.get(COL_NOME_RELATORIO).toString())))
		                .filter(r -> "Relatório parcial do estagiário".equalsIgnoreCase(r.get(COL_TIPO_RELATORIO).toString()))
		                .map(r -> {
		                    try {
		                        return DatasRelatoriosParciaisDto.builder()
		                            .inicioPeriodoRelatorio(LocalDate.parse(r.get(COL_DATA_INICIO_RELATORIO).toString(), DATE_FORMATTER))
		                            .fimPeriodoRelatorio(LocalDate.parse(r.get(COL_DATA_FIM_RELATORIO).toString(), DATE_FORMATTER))
		                            .enviado(true)
		                            .build();
		                    } catch (Exception e) {
		                    	throw e;
		                    }
		                })
		                .filter(Objects::nonNull)
		                .sorted(Comparator.comparing(DatasRelatoriosParciaisDto::getInicioPeriodoRelatorio))
		                .collect(Collectors.toList());

				List<DatasRelatoriosParciaisDto> relatoriosAluno = calcularPeriodosComRelatorios(inicio, termino, relatoriosEnviadosAluno);
				
				List<DatasRelatoriosParciaisDto> relatoriosEnviadosOrientador = relatoriosEnviados.stream()
		                .filter(r -> nomeNormalizado.equalsIgnoreCase(normalizarString(r.get(COL_NOME_RELATORIO).toString())))
		                .filter(r -> "Relatório parcial do supervisor".equalsIgnoreCase(r.get(COL_TIPO_RELATORIO).toString()))
		                .map(r -> {
		                    try {
		                        return DatasRelatoriosParciaisDto.builder()
		                            .inicioPeriodoRelatorio(LocalDate.parse(r.get(COL_DATA_INICIO_RELATORIO).toString(), DATE_FORMATTER))
		                            .fimPeriodoRelatorio(LocalDate.parse(r.get(COL_DATA_FIM_RELATORIO).toString(), DATE_FORMATTER))
		                            .enviado(true)
		                            .build();
		                    } catch (Exception e) {
		                        throw e;
		                    }
		                })
		                .filter(Objects::nonNull)
		                .sorted(Comparator.comparing(DatasRelatoriosParciaisDto::getInicioPeriodoRelatorio))
		                .collect(Collectors.toList());
				
				
				List<DatasRelatoriosParciaisDto> relatoriosOrientador = calcularPeriodosComRelatorios(
						inicio, termino, relatoriosEnviadosOrientador);

				EstudanteDto.PeriodoEstagioDto.PeriodoEstagioDtoBuilder periodoBuilder = EstudanteDto.PeriodoEstagioDto
						.builder()
						.dataInicioEstagio(inicio)
						.dataTerminoEstagio(termino)
						.datasRelatoriosParciaisAluno(relatoriosAluno)
						.datasRelatoriosParciaisOrientador(relatoriosOrientador)
						.dataRelatorioFinal(calcularDataRelatorioFinal(termino))
						.empresa(nomeEmpresa)
						.obrigatorio(obrigatorio);
				
				if (obrigatorio) {
					LocalDate dataRelatorioVisita = calcularDataRelatorioVisita(inicio, cargaHorariaSemanal);

					SemestreAcademico semestreAtual = calendarioService.identificarSemestre(termino);
					AvaliacaoDto intervaloAvaliacao = calcularIntervaloAvaliacao(termino, semestreAtual);

					periodoBuilder.dataRelatorioVisita(dataRelatorioVisita).intervaloAvaliacao(intervaloAvaliacao).obrigatorio(true);

				}

				EstudanteDto.PeriodoEstagioDto periodo = periodoBuilder.build();

				verificarRelatoriosEnviados(nomeNormalizado, relatoriosEnviados, relatoriosAluno, relatoriosOrientador,
						obrigatorio ? periodo.getDataRelatorioVisita() : null,
						periodo.getDataRelatorioFinal(), periodo);

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
	 * Verifica quais relatórios foram enviados e marca como enviados no período
	 */
	private void verificarRelatoriosEnviados(String nomeEstudante, List<List<Object>> relatoriosEnviados,
			List<DatasRelatoriosParciaisDto> relatoriosAluno,
			List<DatasRelatoriosParciaisDto> relatoriosOrientador, 
			LocalDate dataRelatorioVisita,
			LocalDate dataRelatorioFinal, PeriodoEstagioDto periodoDto) {

		for (List<Object> relatorioEnviado : relatoriosEnviados) {
			String nomeRelatorio = relatorioEnviado.get(COL_NOME_RELATORIO).toString();
			String nomeRelatorioDecodificado = URLDecoder.decode(nomeRelatorio, StandardCharsets.UTF_8);
			String nomeRelatorioNormalizado = normalizarString(nomeRelatorioDecodificado);

			if (!nomeEstudante.equalsIgnoreCase(nomeRelatorioNormalizado)) {
				continue;
			}

			String tipoRelatorio = relatorioEnviado.get(COL_TIPO_RELATORIO).toString();
			LocalDate dataInicio = LocalDate.parse(relatorioEnviado.get(COL_DATA_INICIO_RELATORIO).toString(),
					DATE_FORMATTER);
			LocalDate dataFim = LocalDate.parse(relatorioEnviado.get(COL_DATA_FIM_RELATORIO).toString(),
					DATE_FORMATTER);

			if (tipoRelatorio.equalsIgnoreCase("Relatório parcial do estagiário")) {
				for (DatasRelatoriosParciaisDto relatorio : relatoriosAluno) {
					if (relatorio.getInicioPeriodoRelatorio().equals(dataInicio)
							&& relatorio.getFimPeriodoRelatorio().equals(dataFim)) {
						relatorio.setEnviado(true);
					}
				}
			} else if (tipoRelatorio.equalsIgnoreCase("Relatório parcial do supervisor")) {
				for (DatasRelatoriosParciaisDto relatorio : relatoriosOrientador) {
					if (relatorio.getInicioPeriodoRelatorio().equals(dataInicio)
							&& relatorio.getFimPeriodoRelatorio().equals(dataFim)) {
						relatorio.setEnviado(true);
					}
				}
			}
			if (tipoRelatorio.equalsIgnoreCase("Relatório de visita à UCE") && dataRelatorioVisita != null
					&& dataRelatorioVisita.equals(dataFim)) {
				periodoDto.setEnviadoRelatorioVisita(true);
			}

			if (tipoRelatorio.equalsIgnoreCase("Relatório final") && dataRelatorioFinal != null
					&& dataRelatorioFinal.equals(dataFim)) {
				periodoDto.setEnviadoRelatorioFinal(true);
			}
		}
	}

	/**
	 * Calcula os períodos de relatórios parciais e combina com os já enviados
	 */
	private List<DatasRelatoriosParciaisDto> calcularPeriodosComRelatorios(
		    LocalDate inicio, 
		    LocalDate termino,
		    List<DatasRelatoriosParciaisDto> relatoriosEnviados) {
		    
		    List<DatasRelatoriosParciaisDto> periodos = new ArrayList<>();
		    LocalDate dataAtual = inicio;
		    int indexRelatorio = 0;

		    while (dataAtual.isBefore(termino)) {
		        LocalDate fimPeriodo = dataAtual.plusMonths(QTDE_MES_RELATORIO_PARCIAL).minusDays(1);
		        if (fimPeriodo.isAfter(termino)) {
		            fimPeriodo = termino;
		        }

		        if (indexRelatorio < relatoriosEnviados.size()) {
		            DatasRelatoriosParciaisDto relatorio = relatoriosEnviados.get(indexRelatorio);
		            
		            if (!relatorio.getInicioPeriodoRelatorio().isAfter(fimPeriodo.plusDays(7))) {
		                periodos.add(relatorio);
		                dataAtual = relatorio.getFimPeriodoRelatorio().plusDays(1);
		                indexRelatorio++;
		                continue;
		            }
		        }

		        periodos.add(DatasRelatoriosParciaisDto.builder()
		            .inicioPeriodoRelatorio(dataAtual)
		            .fimPeriodoRelatorio(fimPeriodo)
		            .enviado(false)
		            .build());
		        
		        dataAtual = fimPeriodo.plusDays(1);
		    }

		    return periodos;
		}

	/**
	 * Calcula a data do relatório de visita baseado na carga horária semanal
	 */
	private LocalDate calcularDataRelatorioVisita(LocalDate dataInicio, int cargaHorariaSemanal) {
		if (cargaHorariaSemanal <= 0) {
			return dataInicio.plusDays(QTDE_DIAS_VISITA_TECNICA_PADRAO);
		}

		double semanasNecessarias = (double) HORAS_TOTAIS_RELATORIO_VISITA / cargaHorariaSemanal;
		int diasNecessarios = (int) Math.ceil(semanasNecessarias * 7);

		return dataInicio.plusDays(diasNecessarios);
	}

	private LocalDate calcularDataRelatorioFinal(LocalDate dataTermino) {
		return dataTermino.plusDays(7);
	}

	/**
	 * Calcula o intervalo de avaliação baseado no término do estágio e semestre acadêmico
	 */
	private AvaliacaoDto calcularIntervaloAvaliacao(LocalDate termino, SemestreAcademico semestreAtual)
			throws IOException {
		LocalDate duasSemanasAntesFinal = semestreAtual.dataFinal().minusWeeks(2);

		if (termino.isAfter(duasSemanasAntesFinal)) {
			SemestreAcademico proximoSemestre = calendarioService.getSemestreInfo(
					semestreAtual.semestre() == 1 ? semestreAtual.ano() : semestreAtual.ano() + 1,
					semestreAtual.semestre() == 1 ? 2 : 1);
			if (termino.isBefore(proximoSemestre.dataInicio())) {
				return new AvaliacaoDto(proximoSemestre.dataInicio(), proximoSemestre.dataInicio().plusDays(14));

			}
			return new AvaliacaoDto(proximoSemestre.dataInicio(), proximoSemestre.dataInicio().plusDays(14));
		}

		return new AvaliacaoDto(termino.plusDays(1), semestreAtual.dataFinal());

	}

	private String normalizarString(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
	}
	
	
}
