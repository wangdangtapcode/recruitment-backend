package com.example.communications_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String to, String message) {
        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    message).create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    public void sendBulkSms(String[] recipients, String message) {
        for (String recipient : recipients) {
            try {
                sendSms(recipient, message);
            } catch (Exception e) {
                // Log error but continue with other recipients
                System.err.println("Failed to send SMS to " + recipient + ": " + e.getMessage());
            }
        }
    }
}


