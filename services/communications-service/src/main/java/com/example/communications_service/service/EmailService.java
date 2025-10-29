package com.example.communications_service.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables)
            throws MessagingException {
        Context context = new Context();
        variables.forEach(context::setVariable);

        String htmlContent = templateEngine.process(templateName, context);
        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendBulkEmails(String[] recipients, String subject, String templateName,
            Map<String, Object> variables) {
        for (String recipient : recipients) {
            try {
                sendTemplateEmail(recipient, subject, templateName, variables);
            } catch (MessagingException e) {
                // Log error but continue with other recipients
                System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
            }
        }
    }
}




