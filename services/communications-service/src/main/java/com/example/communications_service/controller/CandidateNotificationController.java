package com.example.communications_service.controller;

import com.example.communications_service.model.Notification;
import com.example.communications_service.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/communications-service/candidate-notifications")
public class CandidateNotificationController {

    private final NotificationService notificationService;

    public CandidateNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/new-candidate")
    public ResponseEntity<Map<String, Object>> notifyNewCandidate(@Valid @RequestBody Map<String, Object> request) {
        try {
            Long candidateId = Long.valueOf(request.get("candidateId").toString());
            String candidateName = request.get("candidateName").toString();
            String candidateEmail = request.get("candidateEmail").toString();

            // Create welcome notification for candidate
            Notification welcomeNotification = notificationService.createNotification(
                    candidateId,
                    "CANDIDATE",
                    "EMAIL",
                    "Chào mừng bạn đến với hệ thống tuyển dụng",
                    "Xin chào " + candidateName
                            + ",\n\nCảm ơn bạn đã đăng ký với chúng tôi. Chúng tôi sẽ liên hệ với bạn sớm nhất có thể.\n\nTrân trọng,\nĐội ngũ tuyển dụng");

            // Send welcome email
            notificationService.sendNotification(welcomeNotification);

            // Notify HR team about new candidate
            Notification hrNotification = notificationService.createNotification(
                    1L, // Assuming HR user ID is 1
                    "USER",
                    "EMAIL",
                    "Ứng viên mới đã đăng ký",
                    "Ứng viên mới: " + candidateName + " (" + candidateEmail + ") đã đăng ký vào hệ thống.");

            notificationService.sendNotification(hrNotification);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Candidate welcome notification sent successfully",
                    "candidateId", candidateId));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send candidate notification: " + e.getMessage()));
        }
    }

    @PostMapping("/application-status")
    public ResponseEntity<Map<String, Object>> notifyApplicationStatusChange(
            @Valid @RequestBody Map<String, Object> request) {
        try {
            Long candidateId = Long.valueOf(request.get("candidateId").toString());
            String candidateName = request.get("candidateName").toString();
            String status = request.get("status").toString();
            String jobTitle = request.get("jobTitle").toString();

            String message = "";
            String title = "";

            switch (status.toUpperCase()) {
                case "APPLIED":
                    title = "Đơn ứng tuyển đã được gửi";
                    message = "Xin chào " + candidateName + ",\n\nĐơn ứng tuyển của bạn cho vị trí " + jobTitle
                            + " đã được gửi thành công. Chúng tôi sẽ xem xét và liên hệ với bạn sớm nhất có thể.";
                    break;
                case "REVIEWED":
                    title = "Hồ sơ đang được xem xét";
                    message = "Xin chào " + candidateName + ",\n\nHồ sơ của bạn cho vị trí " + jobTitle
                            + " đang được xem xét. Chúng tôi sẽ thông báo kết quả sớm nhất có thể.";
                    break;
                case "SHORTLISTED":
                    title = "Bạn đã được chọn vào vòng tiếp theo";
                    message = "Xin chào " + candidateName
                            + ",\n\nChúc mừng! Bạn đã được chọn vào vòng tiếp theo cho vị trí " + jobTitle
                            + ". Chúng tôi sẽ liên hệ để sắp xếp lịch phỏng vấn.";
                    break;
                case "REJECTED":
                    title = "Kết quả ứng tuyển";
                    message = "Xin chào " + candidateName + ",\n\nCảm ơn bạn đã quan tâm đến vị trí " + jobTitle
                            + ". Sau khi xem xét kỹ lưỡng, chúng tôi quyết định không tiếp tục với đơn ứng tuyển của bạn. Chúng tôi sẽ lưu hồ sơ của bạn và có thể liên hệ trong tương lai.";
                    break;
                default:
                    title = "Cập nhật trạng thái ứng tuyển";
                    message = "Xin chào " + candidateName + ",\n\nTrạng thái đơn ứng tuyển của bạn cho vị trí "
                            + jobTitle + " đã được cập nhật thành: " + status;
            }

            Notification notification = notificationService.createNotification(
                    candidateId,
                    "CANDIDATE",
                    "EMAIL",
                    title,
                    message);

            notificationService.sendNotification(notification);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Application status notification sent successfully",
                    "candidateId", candidateId,
                    "status", status));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send application status notification: " + e.getMessage()));
        }
    }

    @PostMapping("/interview-reminder")
    public ResponseEntity<Map<String, Object>> sendInterviewReminder(@Valid @RequestBody Map<String, Object> request) {
        try {
            Long candidateId = Long.valueOf(request.get("candidateId").toString());
            String candidateName = request.get("candidateName").toString();
            String interviewDate = request.get("interviewDate").toString();
            String interviewTime = request.get("interviewTime").toString();
            String jobTitle = request.get("jobTitle").toString();
            String location = request.get("location").toString();
            String meetingLink = request.getOrDefault("meetingLink", "").toString();

            String message = "Xin chào " + candidateName + ",\n\n" +
                    "Đây là lời nhắc nhở về buổi phỏng vấn của bạn:\n\n" +
                    "Vị trí: " + jobTitle + "\n" +
                    "Ngày: " + interviewDate + "\n" +
                    "Giờ: " + interviewTime + "\n" +
                    "Địa điểm: " + location + "\n";

            if (!meetingLink.isEmpty()) {
                message += "Link phòng họp: " + meetingLink + "\n";
            }

            message += "\nVui lòng có mặt đúng giờ. Chúc bạn thành công!";

            Notification notification = notificationService.createNotification(
                    candidateId,
                    "CANDIDATE",
                    "EMAIL",
                    "Nhắc nhở phỏng vấn",
                    message);

            notificationService.sendNotification(notification);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Interview reminder sent successfully",
                    "candidateId", candidateId));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send interview reminder: " + e.getMessage()));
        }
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Notification>> getCandidateNotifications(@PathVariable Long candidateId) {
        List<Notification> notifications = notificationService.getNotificationsByRecipient(candidateId, "CANDIDATE");
        return ResponseEntity.ok(notifications);
    }
}
