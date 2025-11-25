package com.utfpr.estagio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principal da aplicação Spring Boot para o sistema de estágios UTFPR
 * @EnableScheduling habilita a execução de tarefas agendadas
 */
@SpringBootApplication
@EnableScheduling
public class EstagioApplication {

	public static void main(String[] args) {
		SpringApplication.run(EstagioApplication.class, args);
	}

}
