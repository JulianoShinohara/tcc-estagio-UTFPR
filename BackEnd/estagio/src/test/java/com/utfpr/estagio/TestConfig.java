package com.utfpr.estagio;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

@Configuration
public class TestConfig {

	@Bean
	public GreenMail greenMail() {
		return new GreenMail(ServerSetup.SMTP.dynamicPort());
	}

	@Bean
	public JavaMailSenderImpl mailSender(GreenMail greenMail) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("localhost");
		mailSender.setPort(greenMail.getSmtp().getPort());

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.starttls.enable", "false");

		return mailSender;
	}
}
