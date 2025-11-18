package com.example.communications_service.service;

import com.example.communications_service.model.MailMessage;
import com.example.communications_service.repository.MailMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailMessageRepository mailRepo;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
                       MailMessageRepository mailRepo, UserService userService) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.mailRepo = mailRepo;
        this.userService = userService;
    }

    /**
     * Gửi email đơn giản và lưu vào database
     */
    @Transactional
    public MailMessage sendSimpleEmailAndSave(String to, String subject, String text, Long fromUserId) {
        try {
            // Gửi email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);

            // Lưu vào database
            MailMessage mailMessage = new MailMessage();
            mailMessage.setFromUserId(fromUserId);
            mailMessage.setFromEmail(fromEmail);
            mailMessage.setToEmail(to);
            
            // Tìm userId từ email
            Long toUserId = userService.getUserIdByEmail(to);
            mailMessage.setToUserId(toUserId);
            
            if (toUserId != null) {
                mailMessage.setToType("USER");
                mailMessage.setExternal(false);
            } else {
                mailMessage.setToType("CANDIDATE");
                mailMessage.setExternal(true);
            }
            
            mailMessage.setSubject(subject);
            mailMessage.setContent(text);
            mailMessage.setThreadId(UUID.randomUUID().toString());

            return mailRepo.save(mailMessage);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
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




