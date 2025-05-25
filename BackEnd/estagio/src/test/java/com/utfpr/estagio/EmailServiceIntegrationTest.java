package com.utfpr.estagio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.icegreen.greenmail.util.GreenMail;
import com.utfpr.estagio.service.EmailService;

import jakarta.mail.internet.MimeMessage;

@SpringBootTest
@Import(TestConfig.class)
class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private GreenMail greenMail;

    @BeforeEach
    void setUp() {
        greenMail.start();
    }

    @AfterEach
    void tearDown() {
        greenMail.stop();
    }

    @Test
    void deveEnviarEmailComSucesso() throws Exception {
        // Dados de teste
        String destinatario = "teste@utfpr.edu.br";
        String assunto = "Teste de Integração";
        String conteudo = "Este é um e-mail de teste";

        // Ação
        emailService.enviarEmailSimples(destinatario, assunto, conteudo);

        // Verificação
        assertTrue(greenMail.waitForIncomingEmail(1000, 1));
        
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        
        MimeMessage mensagem = receivedMessages[0];
        assertEquals(assunto, mensagem.getSubject());
        assertTrue(mensagem.getContent().toString().contains(conteudo));
        assertEquals(destinatario, mensagem.getAllRecipients()[0].toString());
    }
}