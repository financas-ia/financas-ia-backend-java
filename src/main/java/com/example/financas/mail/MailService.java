package com.example.financas.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTwoFactorCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("suporte@financas.com");
        message.setTo(toEmail);
        message.setSubject("Código de Verificação - Finanças");
        message.setText("Seu código de verificação é: " + code);
        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("suporte@financas.com");
        message.setTo(toEmail);
        message.setSubject("Redefinição de Senha - Finanças");
        message.setText("Você solicitou a redefinição de senha. Acesse o link abaixo para redefinir sua senha:\n\n" +
                "http://localhost:8080/auth/reset-password?token=" + token + "\n\n" +
                "Este link é válido por 10 minutos.");
        mailSender.send(message);
    }

}
