package br.fatec.hobby_hub.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCodigoRecuperacao(String destinatario, String codigo) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("nao-responda@hobbyhub.com");
        email.setTo(destinatario);
        email.setSubject("HobbyHub - Código de Recuperação de Senha");
        email.setText("Olá!\n\nSeu código para redefinir a senha é: " + codigo + "\n\nEle expira em 10 minutos.");

        mailSender.send(email);
    }
}
