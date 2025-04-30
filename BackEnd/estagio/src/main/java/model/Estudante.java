package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Getter
public class Estudante {

	@JsonProperty("Nome Estagiário")
	private String nomeEstagiario;

	@JsonProperty("Email")
	private String email;

	@JsonProperty("Orientador")
	private String orientador;

	@JsonProperty("Curso")
	private String curso;

	@JsonProperty("Tipo")
	private String tipo;

	@JsonProperty("Unidade de Concedente de Estágio (UCE)")
	private String unidadeConcedente;

	@JsonProperty("Início do Estágio")
	private LocalDate inicioEstagio;

	@JsonProperty("Término do Estágio")
	private LocalDate terminoEstagio;

	@JsonProperty("Dias de estágio")
	private Integer diasEstagio;

	@JsonProperty("Bolsa-auxílio ou salário")
	private String bolsaAuxilio;

	@JsonProperty("Benefícios")
	private String beneficios;

	@JsonProperty("Carga horária semanal")
	private Integer cargaHorariaSemanal;

	@JsonProperty("Carga Horária Total")
	private Integer cargaHorariaTotal;

	@JsonProperty("Obrigatório")
	private String obrigatorio;

	@JsonProperty("Data de conclusão (orientador)")
	private LocalDate dataConclusaoOrientador;

	@JsonProperty("Data de conclusão (PRAE)")
	private LocalDate dataConclusaoPrae;

	@JsonProperty("Processo SEI")
	private String processoSei;
}