package com.example.communications_service.controller;

import com.example.communications_service.dto.PaginationDTO;
import com.example.communications_service.model.MailMessage;
import com.example.communications_service.service.MailboxService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/communications-service/mail")
@CrossOrigin(origins = "*")
public class MailController {

    private final MailboxService mailboxService;

    public MailController(MailboxService mailboxService) {
        this.mailboxService = mailboxService;
    }

    @PostMapping("/send/internal")
    public ResponseEntity<MailMessage> sendInternal(
            @RequestBody Map<String, Object> req) {
        Long fromUserId = Long.valueOf(req.get("fromUserId").toString());
        Long toUserId = Long.valueOf(req.get("toUserId").toString());
        String subject = String.valueOf(req.getOrDefault("subject", ""));
        String content = String.valueOf(req.getOrDefault("content", ""));
        String threadId = (String) req.getOrDefault("threadId", null);
        return ResponseEntity.ok(mailboxService.sendInternal(fromUserId, toUserId, subject, content, threadId));
    }

    @GetMapping("/inbox/{userId}")
    public ResponseEntity<PaginationDTO> inbox(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mailboxService.getInbox(userId, page, limit));
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<PaginationDTO> sent(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mailboxService.getSent(userId, page, limit));
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<PaginationDTO> thread(
            @PathVariable String threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(mailboxService.getThread(threadId, page, limit));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        mailboxService.markRead(id);
        return ResponseEntity.ok().build();
    }
}
