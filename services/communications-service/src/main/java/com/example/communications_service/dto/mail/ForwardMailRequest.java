package com.example.communications_service.dto.mail;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ForwardMailRequest {
    private Long fromUserId;
    private List<Long> toUserIds; // Có thể forward cho nhiều người
    private String subject;
    private String content; // Nội dung thêm khi forward
    private Long forwardFromId; // ID của email được forward
}

