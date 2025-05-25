package com.utfpr.estagio.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailSimples(String destinatario, String assunto, String conteudo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject(assunto);
        mensagem.setText(conteudo);
        mailSender.send(mensagem);
    }
}