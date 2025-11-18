package com.example.communications_service.dto.mail;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SendMailRequest {
    private Long fromUserId;
    private Long toUserId;
    private String toEmail; // Email thực của người nhận (dùng khi gửi email thực qua Gmail)
    private String subject;
    private String content;
    private List<String> links; // Danh sách các link
    private String threadId; // Optional, dùng khi reply
    private Long replyToId; // Optional, ID của email được reply
    private boolean sendViaGmail = false; // true nếu muốn gửi email thực qua Gmail
}
