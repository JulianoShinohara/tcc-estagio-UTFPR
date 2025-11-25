package com.utfpr.estagio.service;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utfpr.estagio.dto.EstagiosPorSemestreDto;
import com.utfpr.estagio.dto.EmpresaComSupervisoresDto;
import com.utfpr.estagio.dto.EstatisticasDuracaoEstagioDto;
import com.utfpr.estagio.dto.EstatisticasDuracaoPorTipoDto;
import com.utfpr.estagio.dto.EstatisticasDuracaoPorTipoEstagio;
import com.utfpr.estagio.dto.EstatisticasSemestreDto;
import com.utfpr.estagio.dto.OrientadorUceDto;
import com.utfpr.estagio.dto.QtdeEstagiosOrientadorDto;
import com.utfpr.estagio.dto.SupervisorDto;
import com.utfpr.estagio.dto.TiposEstagiosPorSemestreDto;
import com.utfpr.estagio.dto.UnidadeCedenteDto;
import com.utfpr.estagio.service.CalendarioAcademicoService.SemestreAcademico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class PraeService {

	@Autowired
	private GoogleSheetsService googleSheetsService;

	@Autowired
	private CalendarioAcademicoService calendarioService;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final int COL_ORIENTADOR = 2;
	private static final int COL_TIPO_ESTAGIO = 4;
	private static final int COL_EMPRESA = 5;
	private static final int COL_INICIO = 6;
	private static final int COL_TERMINO = 7;
	private static final int COL_BOLSA = 9;
	private static final int COL_BENEFICIO = 10;
	private static final int COL_BOOL_OBRIGATORIO = 13;
	private static final int COL_SEI = 16;
	private static final int COL_SUPERVISOR = 17;

	/**
	 * Retorna estatísticas de orientações por orientador e semestre
	 * Conta cada estágio apenas uma vez por semestre em que está ativo
	 */
	public List<QtdeEstagiosOrientadorDto> getEstatisticasOrientacoesPorSemestre() {
		try {
			List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();

			if (valores == null || valores.isEmpty()) {
				return new ArrayList<>();
			}

			if (valores.size() <= 1) {
				return new ArrayList<>();
			}

			Map<String, Map<String, ContadoresSemestre>> estatisticasPorOrientador = new HashMap<>();

			List<List<Object>> valoresSemCabecalho = valores.subList(1, valores.size());

			for (List<Object> linha : valoresSemCabecalho) {
				try {
					if (linha.size() <= COL_BOOL_OBRIGATORIO) {
						continue;
					}

					if (linha.get(COL_ORIENTADOR) == null || linha.get(COL_INICIO) == null
							|| linha.get(COL_TERMINO) == null || linha.get(COL_BOOL_OBRIGATORIO) == null) {
						continue;
					}

					String nomeOrientador = linha.get(COL_ORIENTADOR).toString().trim();
					if (nomeOrientador.isEmpty()) {
						continue;
					}

					LocalDate inicio = LocalDate.parse(linha.get(COL_INICIO).toString(), DATE_FORMATTER);
					LocalDate termino = LocalDate.parse(linha.get(COL_TERMINO).toString(), DATE_FORMATTER);
					boolean obrigatorio = "sim".equalsIgnoreCase(linha.get(COL_BOOL_OBRIGATORIO).toString());

					Set<String> semestresAtivos = new HashSet<>();

					int anoAtual = inicio.getYear();
					int mesAtual = inicio.getMonthValue();

					while (anoAtual < termino.getYear() ||
					       (anoAtual == termino.getYear() && mesAtual <= termino.getMonthValue())) {

						int semestre = mesAtual <= 6 ? 1 : 2;
						String chaveSemestre = anoAtual + "/" + semestre;
						semestresAtivos.add(chaveSemestre);

						mesAtual++;
						if (mesAtual > 12) {
							mesAtual = 1;
							anoAtual++;
						}
					}

					for (String chaveSemestre : semestresAtivos) {
						String[] partes = chaveSemestre.split("/");
						int ano = Integer.parseInt(partes[0]);
						int semestre = Integer.parseInt(partes[1]);

						estatisticasPorOrientador.putIfAbsent(nomeOrientador, new HashMap<>());
						estatisticasPorOrientador.get(nomeOrientador).putIfAbsent(chaveSemestre,
								new ContadoresSemestre(ano, semestre));

						ContadoresSemestre contadores = estatisticasPorOrientador.get(nomeOrientador).get(chaveSemestre);
						if (obrigatorio) {
							contadores.incrementarObrigatorios();
						} else {
							contadores.incrementarNaoObrigatorios();
						}
					}

				} catch (Exception e) {
					continue;
				}
			}

			List<QtdeEstagiosOrientadorDto> resultado = estatisticasPorOrientador.entrySet().stream()
					.<QtdeEstagiosOrientadorDto>map(entry -> {
						String nomeOrientador = entry.getKey();
						List<EstatisticasSemestreDto> estatisticasSemestres = entry.getValue().values().stream()
								.<EstatisticasSemestreDto>map(contador -> EstatisticasSemestreDto.builder()
										.ano(contador.getAno()).semestre(contador.getSemestre())
										.quantidadeObrigatorios(contador.getObrigatorios())
										.quantidadeNaoObrigatorios(contador.getNaoObrigatorios())
										.total(contador.getObrigatorios() + contador.getNaoObrigatorios()).build())
								.sorted(Comparator.comparing(EstatisticasSemestreDto::getAno)
										.thenComparing(EstatisticasSemestreDto::getSemestre).reversed())
								.collect(Collectors.toList());

						int totalObrigatorios = estatisticasSemestres.stream()
								.mapToInt(EstatisticasSemestreDto::getQuantidadeObrigatorios).sum();
						int totalNaoObrigatorios = estatisticasSemestres.stream()
								.mapToInt(EstatisticasSemestreDto::getQuantidadeNaoObrigatorios).sum();

						return QtdeEstagiosOrientadorDto.builder().nomeOrientador(nomeOrientador)
								.estatisticasPorSemestre(estatisticasSemestres).totalObrigatorios(totalObrigatorios)
								.totalNaoObrigatorios(totalNaoObrigatorios)
								.totalGeral(totalObrigatorios + totalNaoObrigatorios).build();
					}).sorted(Comparator.comparing(QtdeEstagiosOrientadorDto::getNomeOrientador))
					.collect(Collectors.toList());

			return resultado;

		} catch (IOException e) {
			throw new RuntimeException("Erro ao buscar estatísticas de orientações: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException("Erro inesperado ao processar estatísticas: " + e.getMessage(), e);
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class ContadoresSemestre {
		private int ano;
		private int semestre;
		private int obrigatorios = 0;
		private int naoObrigatorios = 0;

		public ContadoresSemestre(int ano, int semestre) {
			this.ano = ano;
			this.semestre = semestre;
		}

		public void incrementarObrigatorios() {
			this.obrigatorios++;
		}

		public void incrementarNaoObrigatorios() {
			this.naoObrigatorios++;
		}
	}

	/**
	 * Retorna quantidade de estágios iniciados e finalizados por mês
	 */
	public List<EstagiosPorSemestreDto> getEstagiosIniciadosFinalizadosPorMes() {
		try {
			List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();

			if (valores == null || valores.isEmpty() || valores.size() <= 1) {
				return new ArrayList<>();
			}

			Map<String, ContadoresMesInicioFim> estatisticasPorMes = new HashMap<>();

			List<List<Object>> valoresSemCabecalho = valores.subList(1, valores.size());

			for (List<Object> linha : valoresSemCabecalho) {
				try {

					if (linha.get(COL_INICIO) == null || linha.get(COL_TERMINO) == null
							|| linha.get(COL_BOOL_OBRIGATORIO) == null) {
						continue;
					}

					LocalDate inicio = LocalDate.parse(linha.get(COL_INICIO).toString(), DATE_FORMATTER);
					LocalDate termino = LocalDate.parse(linha.get(COL_TERMINO).toString(), DATE_FORMATTER);
					boolean obrigatorio = "sim".equalsIgnoreCase(linha.get(COL_BOOL_OBRIGATORIO).toString());

					String chaveInicio = formatarChaveMes(inicio);
					estatisticasPorMes.putIfAbsent(chaveInicio,
							new ContadoresMesInicioFim(inicio.getYear(), inicio.getMonthValue()));

					ContadoresMesInicioFim contadorInicio = estatisticasPorMes.get(chaveInicio);
					if (obrigatorio) {
						contadorInicio.incrementarIniciadosObrigatorios();
					} else {
						contadorInicio.incrementarIniciadosNaoObrigatorios();
					}

					String chaveTermino = formatarChaveMes(termino);
					estatisticasPorMes.putIfAbsent(chaveTermino,
							new ContadoresMesInicioFim(termino.getYear(), termino.getMonthValue()));

					ContadoresMesInicioFim contadorTermino = estatisticasPorMes.get(chaveTermino);
					if (obrigatorio) {
						contadorTermino.incrementarFinalizadosObrigatorios();
					} else {
						contadorTermino.incrementarFinalizadosNaoObrigatorios();
					}

				} catch (Exception e) {
					continue;
				}
			}

			List<EstagiosPorSemestreDto> resultado = estatisticasPorMes.values().stream()
					.<EstagiosPorSemestreDto>map(contador -> EstagiosPorSemestreDto.builder().ano(contador.getAno())
							.semestre(contador.getMes())
							.qtdeIniciados(contador.getIniciadosObrigatorios() + contador.getIniciadosNaoObrigatorios())
							.qtdeFinalizados(
									contador.getFinalizadosObrigatorios() + contador.getFinalizadosNaoObrigatorios())
							.qtdeIniciadosObrigatorios(contador.getIniciadosObrigatorios())
							.qtdeIniciadosNaoObrigatorios(contador.getIniciadosNaoObrigatorios())
							.qtdeFinalizadosObrigatorios(contador.getFinalizadosObrigatorios())
							.qtdeFinalizadosNaoObrigatorios(contador.getFinalizadosNaoObrigatorios()).build())
					.sorted(Comparator.comparing(EstagiosPorSemestreDto::getAno)
							.thenComparing(EstagiosPorSemestreDto::getSemestre).reversed())
					.collect(Collectors.toList());

			return resultado;

		} catch (IOException e) {
			throw new RuntimeException("Erro ao buscar estágios por mês: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException("Erro inesperado ao processar estágios por mês: " + e.getMessage(), e);
		}
	}

	public List<EstagiosPorSemestreDto> getEstagiosIniciadosFinalizadosPorSemestre() {
		try {
			List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();

			if (valores == null || valores.isEmpty() || valores.size() <= 1) {
				return new ArrayList<>();
			}

			Map<String, ContadoresSemestreInicioFim> estatisticasPorSemestre = new HashMap<>();

			List<List<Object>> valoresSemCabecalho = valores.subList(1, valores.size());

			for (List<Object> linha : valoresSemCabecalho) {
				try {

					if (linha.get(COL_INICIO) == null || linha.get(COL_TERMINO) == null
							|| linha.get(COL_BOOL_OBRIGATORIO) == null) {
						continue;
					}

					LocalDate inicio = LocalDate.parse(linha.get(COL_INICIO).toString(), DATE_FORMATTER);
					LocalDate termino = LocalDate.parse(linha.get(COL_TERMINO).toString(), DATE_FORMATTER);
					boolean obrigatorio = "sim".equalsIgnoreCase(linha.get(COL_BOOL_OBRIGATORIO).toString());

					SemestreAcademico semestreInicio = calendarioService.identificarSemestre(inicio);
					SemestreAcademico semestreTermino = calendarioService.identificarSemestre(termino);

					// Contabiliza o semestre de início
					String chaveSemestreInicio = semestreInicio.ano() + "/" + semestreInicio.semestre();
					estatisticasPorSemestre.putIfAbsent(chaveSemestreInicio,
							new ContadoresSemestreInicioFim(semestreInicio.ano(), semestreInicio.semestre()));

					ContadoresSemestreInicioFim contadorInicio = estatisticasPorSemestre.get(chaveSemestreInicio);
					if (obrigatorio) {
						contadorInicio.incrementarIniciadosObrigatorios();
					} else {
						contadorInicio.incrementarIniciadosNaoObrigatorios();
					}

					int anoAtual = semestreInicio.ano();
					int semestreAtual = semestreInicio.semestre();

					while (anoAtual < semestreTermino.ano() ||
						   (anoAtual == semestreTermino.ano() && semestreAtual < semestreTermino.semestre())) {

						if (semestreAtual == 1) {
							semestreAtual = 2;
						} else {
							semestreAtual = 1;
							anoAtual++;
						}

						String chaveSemestreIntermediario = anoAtual + "/" + semestreAtual;
						estatisticasPorSemestre.putIfAbsent(chaveSemestreIntermediario,
								new ContadoresSemestreInicioFim(anoAtual, semestreAtual));

						ContadoresSemestreInicioFim contadorIntermediario = estatisticasPorSemestre
								.get(chaveSemestreIntermediario);
						if (obrigatorio) {
							contadorIntermediario.incrementarIniciadosObrigatorios();
						} else {
							contadorIntermediario.incrementarIniciadosNaoObrigatorios();
						}
					}

					String chaveSemestreTermino = semestreTermino.ano() + "/" + semestreTermino.semestre();
					estatisticasPorSemestre.putIfAbsent(chaveSemestreTermino,
							new ContadoresSemestreInicioFim(semestreTermino.ano(), semestreTermino.semestre()));

					ContadoresSemestreInicioFim contadorTermino = estatisticasPorSemestre.get(chaveSemestreTermino);
					if (obrigatorio) {
						contadorTermino.incrementarFinalizadosObrigatorios();
					} else {
						contadorTermino.incrementarFinalizadosNaoObrigatorios();
					}

				} catch (Exception e) {
					continue;
				}
			}

			List<EstagiosPorSemestreDto> resultado = estatisticasPorSemestre.values().stream()
					.<EstagiosPorSemestreDto>map(contador -> EstagiosPorSemestreDto.builder().ano(contador.getAno())
							.semestre(contador.getSemestre())
							.qtdeIniciados(contador.getIniciadosObrigatorios() + contador.getIniciadosNaoObrigatorios())
							.qtdeFinalizados(
									contador.getFinalizadosObrigatorios() + contador.getFinalizadosNaoObrigatorios())
							.qtdeIniciadosObrigatorios(contador.getIniciadosObrigatorios())
							.qtdeIniciadosNaoObrigatorios(contador.getIniciadosNaoObrigatorios())
							.qtdeFinalizadosObrigatorios(contador.getFinalizadosObrigatorios())
							.qtdeFinalizadosNaoObrigatorios(contador.getFinalizadosNaoObrigatorios()).build())
					.sorted(Comparator.comparing(EstagiosPorSemestreDto::getAno)
							.thenComparing(EstagiosPorSemestreDto::getSemestre).reversed())
					.collect(Collectors.toList());

			return resultado;

		} catch (IOException e) {
			throw new RuntimeException("Erro ao buscar estágios por semestre: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException("Erro inesperado ao processar estágios por semestre: " + e.getMessage(), e);
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class ContadoresSemestreInicioFim {
		private int ano;
		private int semestre;
		private int iniciadosObrigatorios = 0;
		private int iniciadosNaoObrigatorios = 0;
		private int finalizadosObrigatorios = 0;
		private int finalizadosNaoObrigatorios = 0;

		public ContadoresSemestreInicioFim(int ano, int semestre) {
			this.ano = ano;
			this.semestre = semestre;
		}

		public void incrementarIniciadosObrigatorios() {
			this.iniciadosObrigatorios++;
		}

		public void incrementarIniciadosNaoObrigatorios() {
			this.iniciadosNaoObrigatorios++;
		}

		public void incrementarFinalizadosObrigatorios() {
			this.finalizadosObrigatorios++;
		}

		public void incrementarFinalizadosNaoObrigatorios() {
			this.finalizadosNaoObrigatorios++;
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class ContadoresMesInicioFim {
		private int ano;
		private int mes;
		private int iniciadosObrigatorios = 0;
		private int iniciadosNaoObrigatorios = 0;
		private int finalizadosObrigatorios = 0;
		private int finalizadosNaoObrigatorios = 0;

		public ContadoresMesInicioFim(int ano, int mes) {
			this.ano = ano;
			this.mes = mes;
		}

		public void incrementarIniciadosObrigatorios() {
			this.iniciadosObrigatorios++;
		}

		public void incrementarIniciadosNaoObrigatorios() {
			this.iniciadosNaoObrigatorios++;
		}

		public void incrementarFinalizadosObrigatorios() {
			this.finalizadosObrigatorios++;
		}

		public void incrementarFinalizadosNaoObrigatorios() {
			this.finalizadosNaoObrigatorios++;
		}
	}

	/**
	 * Retorna quantidade de cada tipo de estágio agrupado por semestre
	 */
	public List<TiposEstagiosPorSemestreDto> getTiposEstagiosPorSemestre() {
		try {
			List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();

			if (valores == null || valores.isEmpty() || valores.size() <= 1) {
				return new ArrayList<>();
			}

			Map<String, ContadoresTiposSemestre> estatisticasPorSemestre = new HashMap<>();

			List<List<Object>> valoresSemCabecalho = valores.subList(1, valores.size());

			for (List<Object> linha : valoresSemCabecalho) {
				try {

					if (linha.get(COL_INICIO) == null || linha.get(COL_TIPO_ESTAGIO) == null) {
						continue;
					}

					LocalDate inicio = LocalDate.parse(linha.get(COL_INICIO).toString(), DATE_FORMATTER);
					String tipoEstagio = linha.get(COL_TIPO_ESTAGIO).toString().trim();

					if (tipoEstagio.isEmpty()) {
						continue;
					}

					SemestreAcademico semestre = calendarioService.identificarSemestre(inicio);
					String chaveSemestre = semestre.ano() + "/" + semestre.semestre();

					estatisticasPorSemestre.putIfAbsent(chaveSemestre,
							new ContadoresTiposSemestre(semestre.ano(), semestre.semestre()));

					estatisticasPorSemestre.get(chaveSemestre).incrementarTipo(tipoEstagio);

				} catch (Exception e) {
					continue;
				}
			}

			List<TiposEstagiosPorSemestreDto> resultado = estatisticasPorSemestre.values().stream()
					.<TiposEstagiosPorSemestreDto>map(contador -> {
						int total = contador.getQuantidadePorTipo().values().stream().mapToInt(Integer::intValue).sum();

						return TiposEstagiosPorSemestreDto.builder().ano(contador.getAno())
								.semestre(contador.getSemestre()).quantidadePorTipo(contador.getQuantidadePorTipo())
								.totalEstagios(total).build();
					})
					.sorted(Comparator.comparing(TiposEstagiosPorSemestreDto::getAno)
							.thenComparing(TiposEstagiosPorSemestreDto::getSemestre).reversed())
					.collect(Collectors.toList());

			return resultado;

		} catch (IOException e) {
			throw new RuntimeException("Erro ao buscar tipos de estágios por semestre: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException("Erro inesperado ao processar tipos de estágios: " + e.getMessage(), e);
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class ContadoresTiposSemestre {
		private int ano;
		private int semestre;
		private Map<String, Integer> quantidadePorTipo = new HashMap<>();

		public ContadoresTiposSemestre(int ano, int semestre) {
			this.ano = ano;
			this.semestre = semestre;
			this.quantidadePorTipo = new HashMap<>();
		}

		public void incrementarTipo(String tipo) {
			quantidadePorTipo.put(tipo, quantidadePorTipo.getOrDefault(tipo, 0) + 1);
		}
	}

	/**
	 * Retorna lista de unidades cedentes com estatísticas
	 */
	public List<UnidadeCedenteDto> getUnidadesCedentes() {
		try {
			List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();

			if (valores == null || valores.isEmpty() || valores.size() <= 1) {
				return new ArrayList<>();
			}

			Map<String, ContadoresUce> estatisticasPorSupervisor = new HashMap<>();

			List<List<Object>> valoresSemCabecalho = valores.subList(1, valores.size());

			for (List<Object> linha : valoresSemCabecalho) {
				try {
					if (linha.size() <= COL_SUPERVISOR || linha.get(COL_SUPERVISOR) == null) {
						continue;
					}

					String nomeSupervisor = linha.get(COL_SUPERVISOR).toString().trim();

					if (nomeSupervisor.isEmpty()) {
						continue;
					}

					boolean obrigatorio = false;
					if (linha.size() > COL_BOOL_OBRIGATORIO && linha.get(COL_BOOL_OBRIGATORIO) != null) {
						obrigatorio = "sim".equalsIgnoreCase(linha.get(COL_BOOL_OBRIGATORIO).toString());
					}

					double valorBolsa = parseValorMonetario(linha, COL_BOLSA);
					double valorBeneficio = parseValorMonetario(linha, COL_BENEFICIO);

					estatisticasPorSupervisor.putIfAbsent(nomeSupervisor, new ContadoresUce(nomeSupervisor));

					ContadoresUce contador = estatisticasPorSupervisor.get(nomeSupervisor);
					contador.adicionarValorBolsa(valorBolsa);
					contador.adicionarValorBeneficio(valorBeneficio);

					if (obrigatorio) {
						contador.incrementarObrigatorios();
					} else {
						contador.incrementarNaoObrigatorios();
					}

				} catch (Exception e) {
					continue;
				}
			}

			List<UnidadeCedenteDto> resultado = estatisticasPorSupervisor.values().stream()
					.<UnidadeCedenteDto>map(contador -> {
						return UnidadeCedenteDto.builder()
								.nomeEmpresa(contador.getNomeEmpresa())
								.orientadores(new ArrayList<>())
								.valorMedioBolsa(contador.getValorMedioBolsa())
								.valorMedioBeneficio(contador.getValorMedioBeneficio())
								.quantidadeObrigatorios(contador.getObrigatorios())
								.quantidadeNaoObrigatorios(contador.getNaoObrigatorios())
								.totalEstagios(contador.getObrigatorios() + contador.getNaoObrigatorios())
								.build();
					}).sorted(Comparator.comparing(UnidadeCedenteDto::getTotalEstagios).reversed()
								.thenComparing(UnidadeCedenteDto::getNomeEmpresa))
					.collect(Collectors.toList());

			return resultado;

		} catch (IOException e) {
			throw new RuntimeException("Erro ao buscar supervisores: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException("Erro inesperado ao processar unidades cedentes: " + e.getMessage(), e);
		}
	}

	/**
	 * Retorna lista de empresas com supervisores agrupados hierarquicamente
	 */
	public List<EmpresaComSupervisoresDto> getSupervisores() {
		try {
			List<List<Object>> valores = googleSheetsService.getEstudantesFromSheet();

			if (valores == null || valores.isEmpty() || valores.size() <= 1) {
				return new ArrayList<>();
			}

			// Chave: empresa|supervisor|ano|semestre (para agregar dados de um mesmo supervisor por semestre)
			Map<String, ContadoresSupervisorComEmpresa> estatisticasPorSupervisor = new HashMap<>();
			// Mapa para agregar por empresa
			Map<String, ContadoresEmpresa> estatisticasPorEmpresa = new HashMap<>();

			List<List<Object>> valoresSemCabecalho = valores.subList(1, valores.size());

			for (List<Object> linha : valoresSemCabecalho) {
				try {
					if (linha.size() < 18) {
						continue;
					}

					Object supervisorObj = linha.get(COL_SUPERVISOR);
					if (supervisorObj == null || supervisorObj.toString().trim().isEmpty()) {
						continue;
					}

					Object empresaObj = linha.get(COL_EMPRESA);
					if (empresaObj == null || empresaObj.toString().trim().isEmpty()) {
						continue;
					}

					String nomeSupervisor = supervisorObj.toString().trim();
					String nomeEmpresa = empresaObj.toString().trim();

					Integer ano = null;
					Integer semestre = null;
					try {
						if (linha.size() > COL_INICIO && linha.get(COL_INICIO) != null) {
							LocalDate dataInicio = LocalDate.parse(linha.get(COL_INICIO).toString(), DATE_FORMATTER);
							ano = dataInicio.getYear();
							semestre = calcularSemestreFromData(dataInicio);
						}
					} catch (Exception e) {
					}

					// Validar ano e semestre
					if (ano == null || semestre == null) {
						continue;
					}

					String chave = nomeEmpresa + "|" + nomeSupervisor + "|" + ano + "|" + semestre;

					boolean obrigatorio = false;
					if (linha.size() > COL_BOOL_OBRIGATORIO && linha.get(COL_BOOL_OBRIGATORIO) != null) {
						obrigatorio = "sim".equalsIgnoreCase(linha.get(COL_BOOL_OBRIGATORIO).toString());
					}

					double valorBolsa = parseValorMonetario(linha, COL_BOLSA);
					double valorBeneficio = parseValorMonetario(linha, COL_BENEFICIO);

					estatisticasPorSupervisor.putIfAbsent(chave, new ContadoresSupervisorComEmpresa(nomeEmpresa, nomeSupervisor));
					ContadoresSupervisorComEmpresa contadorSupervisor = estatisticasPorSupervisor.get(chave);
					contadorSupervisor.adicionarValorBolsa(valorBolsa);
					contadorSupervisor.adicionarValorBeneficio(valorBeneficio);
					contadorSupervisor.incrementarQuantidadeEstagios();

					contadorSupervisor.definirAnoSemestre(ano, semestre);

					estatisticasPorEmpresa.putIfAbsent(nomeEmpresa, new ContadoresEmpresa(nomeEmpresa));
					ContadoresEmpresa contadorEmpresa = estatisticasPorEmpresa.get(nomeEmpresa);
					contadorEmpresa.adicionarValorBolsa(valorBolsa);
					contadorEmpresa.adicionarValorBeneficio(valorBeneficio);

					if (obrigatorio) {
						contadorSupervisor.incrementarObrigatorios();
						contadorEmpresa.incrementarObrigatorios();
					} else {
						contadorSupervisor.incrementarNaoObrigatorios();
						contadorEmpresa.incrementarNaoObrigatorios();
					}

				} catch (Exception e) {
					continue;
				}
			}

			Map<String, List<SupervisorDto>> supervisoresPorEmpresa = new HashMap<>();

			for (ContadoresSupervisorComEmpresa contador : estatisticasPorSupervisor.values()) {
				SupervisorDto supervisor = SupervisorDto.builder()
						.nomeSupervisor(contador.getNomeSupervisor())
						.quantidadeEstagios(contador.getQuantidadeEstagios())
						.ano(contador.getAno())
						.semestre(contador.getSemestre())
						.build();

				supervisoresPorEmpresa.computeIfAbsent(contador.getNomeEmpresa(), k -> new ArrayList<>()).add(supervisor);
			}

			List<EmpresaComSupervisoresDto> resultado = supervisoresPorEmpresa.entrySet().stream()
					.map(entry -> {
						String nomeEmpresa = entry.getKey();
						List<SupervisorDto> supervisores = entry.getValue().stream()
								.sorted(Comparator.comparing(SupervisorDto::getQuantidadeEstagios).reversed()
										.thenComparing(SupervisorDto::getNomeSupervisor))
								.collect(Collectors.toList());

						ContadoresEmpresa contadoresEmpresa = estatisticasPorEmpresa.get(nomeEmpresa);
						double valorMedioBolsa = contadoresEmpresa != null ? contadoresEmpresa.getValorMedioBolsa() : 0.0;
						double valorMedioBeneficio = contadoresEmpresa != null ? contadoresEmpresa.getValorMedioBeneficio() : 0.0;
						int quantidadeObrigatorios = contadoresEmpresa != null ? contadoresEmpresa.getObrigatorios() : 0;
						int quantidadeNaoObrigatorios = contadoresEmpresa != null ? contadoresEmpresa.getNaoObrigatorios() : 0;

						return EmpresaComSupervisoresDto.builder()
								.nomeEmpresa(nomeEmpresa)
								.supervisores(supervisores)
								.valorMedioBolsa(valorMedioBolsa)
								.valorMedioBeneficio(valorMedioBeneficio)
								.quantidadeObrigatorios(quantidadeObrigatorios)
								.quantidadeNaoObrigatorios(quantidadeNaoObrigatorios)
								.build();
					})
					.sorted(Comparator.comparing(EmpresaComSupervisoresDto::getNomeEmpresa))
					.collect(Collectors.toList());

			return resultado;

		} catch (IOException e) {
			throw new RuntimeException("Erro ao buscar supervisores: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException("Erro inesperado ao processar supervisores: " + e.getMessage(), e);
		}
	}

	private double parseValorMonetario(List<Object> linha, int coluna) {
		if (linha.size() <= coluna || linha.get(coluna) == null) {
			return 0.0;
		}

		try {
			String valor = linha.get(coluna).toString().trim();
			if (valor.isEmpty() || valor.equals("-") || valor.equalsIgnoreCase("N/A")) {
				return 0.0;
			}

			valor = valor.replace("R$", "").replace(".", "").replace(",", ".").trim();

			return Double.parseDouble(valor);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class ContadoresUce {
		private String nomeEmpresa;
		private Map<String, Integer> orientadores = new HashMap<>();
		private List<Double> valoresBolsa = new ArrayList<>();
		private List<Double> valoresBeneficio = new ArrayList<>();
		private int obrigatorios = 0;
		private int naoObrigatorios = 0;

		public ContadoresUce(String nomeEmpresa) {
			this.nomeEmpresa = nomeEmpresa;
			this.orientadores = new HashMap<>();
			this.valoresBolsa = new ArrayList<>();
			this.valoresBeneficio = new ArrayList<>();
		}

		public void incrementarOrientador(String nomeOrientador) {
			orientadores.put(nomeOrientador, orientadores.getOrDefault(nomeOrientador, 0) + 1);
		}

		public void adicionarValorBolsa(double valor) {
			if (valor > 0) {
				valoresBolsa.add(valor);
			}
		}

		public void adicionarValorBeneficio(double valor) {
			if (valor > 0) {
				valoresBeneficio.add(valor);
			}
		}

		public void incrementarObrigatorios() {
			this.obrigatorios++;
		}

		public void incrementarNaoObrigatorios() {
			this.naoObrigatorios++;
		}

		public double getValorMedioBolsa() {
			if (valoresBolsa.isEmpty()) {
				return 0.0;
			}
			return valoresBolsa.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		}

		public double getValorMedioBeneficio() {
			if (valoresBeneficio.isEmpty()) {
				return 0.0;
			}
			return valoresBeneficio.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class ContadoresSupervisor {
		private String nomeSupervisor;
		private List<Double> valoresBolsa = new ArrayList<>();
		private List<Double> valoresBeneficio = new ArrayList<>();
		private int quantidadeEstagios = 0;
		private int obrigatorios = 0;
		private int naoObrigatorios = 0;

		public ContadoresSupervisor(String nomeSupervisor) {
			this.nomeSupervisor = nomeSupervisor;
			this.valoresBolsa = new ArrayList<>();
			this.valoresBeneficio = new ArrayList<>();
		}

		public void adicionarValorBolsa(double valor) {
			if (valor > 0) {
				valoresBolsa.add(valor);
			}
		}

		public void adicionarValorBeneficio(double valor) {
			if (valor > 0) {
				valoresBeneficio.add(valor);
			}
		}

		public void incrementarQuantidadeEstagios() {
			this.quantidadeEstagios++;
		}

		public void incrementarObrigatorios() {
			this.obrigatorios++;
		}

		public void incrementarNaoObrigatorios() {
			this.naoObrigatorios++;
		}

		public double getValorMedioBolsa() {
			if (valoresBolsa.isEmpty()) {
				return 0.0;
			}
			return valoresBolsa.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		}

		public double getValorMedioBeneficio() {
			if (valoresBeneficio.isEmpty()) {
				return 0.0;
			}
			return valoresBeneficio.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class ContadoresSupervisorComEmpresa {
		private String nomeEmpresa;
		private String nomeSupervisor;
		private List<Double> valoresBolsa = new ArrayList<>();
		private List<Double> valoresBeneficio = new ArrayList<>();
		private int quantidadeEstagios = 0;
		private int obrigatorios = 0;
		private int naoObrigatorios = 0;
		private Integer ano;
		private Integer semestre;

		public ContadoresSupervisorComEmpresa(String nomeEmpresa, String nomeSupervisor) {
			this.nomeEmpresa = nomeEmpresa;
			this.nomeSupervisor = nomeSupervisor;
			this.valoresBolsa = new ArrayList<>();
			this.valoresBeneficio = new ArrayList<>();
		}

		public void definirAnoSemestre(Integer ano, Integer semestre) {
			this.ano = ano;
			this.semestre = semestre;
		}

		public void adicionarValorBolsa(double valor) {
			if (valor > 0) {
				valoresBolsa.add(valor);
			}
		}

		public void adicionarValorBeneficio(double valor) {
			if (valor > 0) {
				valoresBeneficio.add(valor);
			}
		}

		public void incrementarQuantidadeEstagios() {
			this.quantidadeEstagios++;
		}

		public void incrementarObrigatorios() {
			this.obrigatorios++;
		}

		public void incrementarNaoObrigatorios() {
			this.naoObrigatorios++;
		}

		public double getValorMedioBolsa() {
			if (valoresBolsa.isEmpty()) {
				return 0.0;
			}
			return valoresBolsa.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		}

		public double getValorMedioBeneficio() {
			if (valoresBeneficio.isEmpty()) {
				return 0.0;
			}
			return valoresBeneficio.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		}
	}

	private String formatarChaveMes(LocalDate data) {
		return data.getYear() + "/" + String.format("%02d", data.getMonthValue());
	}

	/**
	 * Calcula o semestre a partir de uma data (1-6 = semestre 1, 7-12 = semestre 2)
	 */
	private int calcularSemestreFromData(LocalDate data) {
		return data.getMonthValue() <= 6 ? 1 : 2;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class ContadoresEmpresa {
		private String nomeEmpresa;
		private List<Double> valoresBolsa = new ArrayList<>();
		private List<Double> valoresBeneficio = new ArrayList<>();
		private int obrigatorios = 0;
		private int naoObrigatorios = 0;

		public ContadoresEmpresa(String nomeEmpresa) {
			this.nomeEmpresa = nomeEmpresa;
			this.valoresBolsa = new ArrayList<>();
			this.valoresBeneficio = new ArrayList<>();
		}

		public void adicionarValorBolsa(double valor) {
			if (valor > 0) {
				valoresBolsa.add(valor);
			}
		}

		public void adicionarValorBeneficio(double valor) {
			if (valor > 0) {
				valoresBeneficio.add(valor);
			}
		}

		public void incrementarObrigatorios() {
			this.obrigatorios++;
		}

		public void incrementarNaoObrigatorios() {
			this.naoObrigatorios++;
		}

		public double getValorMedioBolsa() {
			if (valoresBolsa.isEmpty()) {
				return 0.0;
			}
			return valoresBolsa.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		}

		public double getValorMedioBeneficio() {
			if (valoresBeneficio.isEmpty()) {
				return 0.0;
			}
			return valoresBeneficio.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		}
	}

	/**
	 * Calcula estatísticas de duração dos estágios incluindo média, mínima, máxima e quartis
	 */
	public EstatisticasDuracaoEstagioDto getEstatisticasDuracaoEstagios() throws IOException {
		List<List<Object>> dados = googleSheetsService.getEstudantesFromSheet();
		List<Long> duracoes = new ArrayList<>();

		for (List<Object> linha : dados) {
			if (linha.size() < 8) {
				continue;
			}

			try {
				String dataInicioStr = linha.get(COL_INICIO).toString().trim();
				String dataTerminoStr = linha.get(COL_TERMINO).toString().trim();

				if (dataInicioStr.isEmpty() || dataTerminoStr.isEmpty()) {
					continue;
				}

				LocalDate dataInicio = LocalDate.parse(dataInicioStr, DATE_FORMATTER);
				LocalDate dataTermino = LocalDate.parse(dataTerminoStr, DATE_FORMATTER);

				// Calcula duração em dias
				long duracao = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataTermino);

				if (duracao > 0) {
					duracoes.add(duracao);
				}
			} catch (Exception e) {
				continue;
			}
		}

		if (duracoes.isEmpty()) {
			return EstatisticasDuracaoEstagioDto.builder()
					.durracaoMedia(0.0)
					.duracaoMinima(0L)
					.duracaoMaxima(0L)
					.primeiroQuartil(0L)
					.mediana(0L)
					.terceiroQuartil(0L)
					.totalEstagios(0)
					.build();
		}

		// Ordena as durações
		duracoes.sort(null);

		// Calcula estatísticas
		double media = duracoes.stream().mapToLong(Long::longValue).average().orElse(0.0);
		long minima = duracoes.get(0);
		long maxima = duracoes.get(duracoes.size() - 1);
		long mediana = calcularPercentil(duracoes, 50);
		long q1 = calcularPercentil(duracoes, 25);
		long q3 = calcularPercentil(duracoes, 75);

		return EstatisticasDuracaoEstagioDto.builder()
				.durracaoMedia(media)
				.duracaoMinima(minima)
				.duracaoMaxima(maxima)
				.primeiroQuartil(q1)
				.mediana(mediana)
				.terceiroQuartil(q3)
				.totalEstagios(duracoes.size())
				.build();
	}

	/**
	 * Calcula estatísticas de duração separadas por tipo (obrigatório/não obrigatório)
	 */
	public com.utfpr.estagio.dto.EstatisticasDuracaoPorTipoDto getEstatisticasDuracaoPorTipo() throws IOException {
		List<List<Object>> dados = googleSheetsService.getEstudantesFromSheet();
		List<Long> duracoesObrigatorios = new ArrayList<>();
		List<Long> duracoesNaoObrigatorios = new ArrayList<>();

		for (List<Object> linha : dados) {
			if (linha.size() < 14) {
				continue;
			}

			try {
				String dataInicioStr = linha.get(COL_INICIO).toString().trim();
				String dataTerminoStr = linha.get(COL_TERMINO).toString().trim();

				if (dataInicioStr.isEmpty() || dataTerminoStr.isEmpty()) {
					continue;
				}

				LocalDate dataInicio = LocalDate.parse(dataInicioStr, DATE_FORMATTER);
				LocalDate dataTermino = LocalDate.parse(dataTerminoStr, DATE_FORMATTER);

				// Calcula duração em dias
				long duracao = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataTermino);

				if (duracao > 0) {
					// Verifica se é obrigatório
					boolean obrigatorio = false;
					if (linha.size() > COL_BOOL_OBRIGATORIO && linha.get(COL_BOOL_OBRIGATORIO) != null) {
						obrigatorio = "sim".equalsIgnoreCase(linha.get(COL_BOOL_OBRIGATORIO).toString());
					}

					if (obrigatorio) {
						duracoesObrigatorios.add(duracao);
					} else {
						duracoesNaoObrigatorios.add(duracao);
					}
				}
			} catch (Exception e) {
				continue;
			}
		}

		EstatisticasDuracaoEstagioDto statsObrigatorios = calcularEstatisticas(duracoesObrigatorios);
		EstatisticasDuracaoEstagioDto statsNaoObrigatorios = calcularEstatisticas(duracoesNaoObrigatorios);

		return com.utfpr.estagio.dto.EstatisticasDuracaoPorTipoDto.builder()
				.obrigatorios(statsObrigatorios)
				.naoObrigatorios(statsNaoObrigatorios)
				.build();
	}

	/**
	 * Método auxiliar para calcular estatísticas de uma lista de durações
	 */
	private EstatisticasDuracaoEstagioDto calcularEstatisticas(List<Long> duracoes) {
		if (duracoes.isEmpty()) {
			return EstatisticasDuracaoEstagioDto.builder()
					.durracaoMedia(0.0)
					.duracaoMinima(0L)
					.duracaoMaxima(0L)
					.primeiroQuartil(0L)
					.mediana(0L)
					.terceiroQuartil(0L)
					.totalEstagios(0)
					.build();
		}

		// Ordena as durações
		duracoes.sort(null);

		// Calcula estatísticas
		double media = duracoes.stream().mapToLong(Long::longValue).average().orElse(0.0);
		long minima = duracoes.get(0);
		long maxima = duracoes.get(duracoes.size() - 1);
		long mediana = calcularPercentil(duracoes, 50);
		long q1 = calcularPercentil(duracoes, 25);
		long q3 = calcularPercentil(duracoes, 75);

		return EstatisticasDuracaoEstagioDto.builder()
				.durracaoMedia(media)
				.duracaoMinima(minima)
				.duracaoMaxima(maxima)
				.primeiroQuartil(q1)
				.mediana(mediana)
				.terceiroQuartil(q3)
				.totalEstagios(duracoes.size())
				.build();
	}

	/**
	 * Calcula um percentil para uma lista ordenada de valores usando interpolação linear
	 */
	private long calcularPercentil(List<Long> valores, int percentil) {
		if (valores.isEmpty()) {
			return 0L;
		}

		if (valores.size() == 1) {
			return valores.get(0);
		}

		double indice = (percentil / 100.0) * (valores.size() - 1);
		int inferior = (int) Math.floor(indice);
		int superior = (int) Math.ceil(indice);

		if (inferior == superior) {
			return valores.get(inferior);
		}

		double fracao = indice - inferior;
		long valor1 = valores.get(inferior);
		long valor2 = valores.get(superior);
		return Math.round(valor1 + fracao * (valor2 - valor1));
	}

	private String normalizarString(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
	}

	/**
	 * Calcula estatísticas de duração separadas por tipo de estágio e depois por obrigatório/não obrigatório
	 */
	public EstatisticasDuracaoPorTipoEstagio getEstatisticasDuracaoPorTipoEstagio() throws IOException {
		List<List<Object>> dados = googleSheetsService.getEstudantesFromSheet();

		Map<String, Map<String, List<Long>>> duracoesAgrupadasPorTipo = new HashMap<>();
		Set<String> tiposEncontrados = new HashSet<>();

		for (List<Object> linha : dados) {
			if (linha.size() < 14) {
				continue;
			}

			try {
				String dataInicioStr = linha.get(COL_INICIO).toString().trim();
				String dataTerminoStr = linha.get(COL_TERMINO).toString().trim();
				String tipoEstagioStr = linha.get(COL_TIPO_ESTAGIO).toString().trim();

				if (dataInicioStr.isEmpty() || dataTerminoStr.isEmpty() || tipoEstagioStr.isEmpty()) {
					continue;
				}

				LocalDate dataInicio = LocalDate.parse(dataInicioStr, DATE_FORMATTER);
				LocalDate dataTermino = LocalDate.parse(dataTerminoStr, DATE_FORMATTER);

				// Calcula duração em dias
				long duracao = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataTermino);

				if (duracao > 0) {
					// Verifica se é obrigatório
					boolean obrigatorio = false;
					if (linha.size() > COL_BOOL_OBRIGATORIO && linha.get(COL_BOOL_OBRIGATORIO) != null) {
						obrigatorio = "sim".equalsIgnoreCase(linha.get(COL_BOOL_OBRIGATORIO).toString());
					}

					duracoesAgrupadasPorTipo.putIfAbsent(tipoEstagioStr, new HashMap<>());
					String chaveObrigatorio = obrigatorio ? "obrigatorios" : "naoObrigatorios";
					duracoesAgrupadasPorTipo.get(tipoEstagioStr)
						.putIfAbsent(chaveObrigatorio, new ArrayList<>());

					duracoesAgrupadasPorTipo.get(tipoEstagioStr).get(chaveObrigatorio).add(duracao);
					tiposEncontrados.add(tipoEstagioStr);
				}
			} catch (Exception e) {
				continue;
			}
		}

		Map<String, EstatisticasDuracaoPorTipoDto> estatisticasPorTipo = new HashMap<>();

		for (String tipo : tiposEncontrados) {
			Map<String, List<Long>> duracoesDoTipo = duracoesAgrupadasPorTipo.get(tipo);

			List<Long> duracoesObrigatorios = duracoesDoTipo.getOrDefault("obrigatorios", new ArrayList<>());
			List<Long> duracoesNaoObrigatorios = duracoesDoTipo.getOrDefault("naoObrigatorios", new ArrayList<>());

			EstatisticasDuracaoEstagioDto statsObrigatorios = calcularEstatisticas(duracoesObrigatorios);
			EstatisticasDuracaoEstagioDto statsNaoObrigatorios = calcularEstatisticas(duracoesNaoObrigatorios);

			EstatisticasDuracaoPorTipoDto estatisticasDoTipo = EstatisticasDuracaoPorTipoDto.builder()
				.obrigatorios(statsObrigatorios)
				.naoObrigatorios(statsNaoObrigatorios)
				.build();

			estatisticasPorTipo.put(tipo, estatisticasDoTipo);
		}

		List<String> tiposOrdenados = new ArrayList<>(tiposEncontrados);
		tiposOrdenados.sort(String::compareTo);

		return EstatisticasDuracaoPorTipoEstagio.builder()
			.estatisticasPorTipo(estatisticasPorTipo)
			.tiposDisponiveis(tiposOrdenados)
			.build();
	}
}
