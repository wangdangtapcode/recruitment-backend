package com.example.communications_service.controller;

import com.example.communications_service.dto.schedule.ScheduleRequest;
import com.example.communications_service.model.Schedule;
import com.example.communications_service.service.EmailService;
import com.example.communications_service.service.ScheduleService;
import com.example.communications_service.utils.enums.MeetingType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/communications-service/test")
@CrossOrigin(origins = "*")
public class TestController {

    private final EmailService emailService;
    private final ScheduleService scheduleService;

    public TestController(EmailService emailService, ScheduleService scheduleService) {
        this.emailService = emailService;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/email/send-test")
    public ResponseEntity<String> sendTestEmail() {
        try {
            emailService.sendSimpleEmail(
                    "hieugiax145@gmail.com",
                    "Test Email",
                    "Chó hiếu");
            return ResponseEntity.ok("Test email đã được gửi thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi gửi test email: " + e.getMessage());
        }
    }

    @GetMapping("/email/send-template-test")
    public ResponseEntity<String> sendTemplateTestEmail() {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("candidateName", "Nguyễn Văn A");
            variables.put("jobTitle", "Java Developer");
            variables.put("interviewDate", "2024-01-15 14:00");
            variables.put("location", "Tầng 5, Tòa nhà ABC");
            variables.put("interviewerName", "Trần Thị B");
            variables.put("companyName", "Công ty XYZ");

            emailService.sendTemplateEmail(
                    "test@example.com",
                    "Thư mời phỏng vấn - Test",
                    "interview-invitation",
                    variables);
            return ResponseEntity.ok("Test template email đã được gửi thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi gửi template email: " + e.getMessage());
        }
    }

    @PostMapping("/schedule/create-test")
    public ResponseEntity<Map<String, Object>> createTestSchedule() {
        try {
            ScheduleRequest request = new ScheduleRequest();
            request.setTitle("Cuộc họp test");
            request.setDescription("Đây là cuộc họp test được tạo tự động");
            request.setFormat("ONLINE");
            request.setMeetingType(MeetingType.MEETING);
            request.setStatus("SCHEDULED");
            request.setLocation("Phòng họp ảo");
            request.setStartTime(LocalDateTime.now().plusHours(1));
            request.setEndTime(LocalDateTime.now().plusHours(2));
            request.setTimezone("Asia/Ho_Chi_Minh");
            request.setReminderTime(15);
            request.setCreatedById(1L);

            Schedule schedule = scheduleService.createSchedule(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test schedule đã được tạo thành công!");
            response.put("schedule", schedule);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tạo test schedule: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/schedule/list-test")
    public ResponseEntity<Map<String, Object>> listTestSchedules() {
        try {
            var result = scheduleService.getAllSchedules(1, 10, "startTime", "asc",
                    null, null, null, null, null, null, null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh sách lịch đã được tải thành công!");
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tải danh sách lịch: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/schedule/filter-test")
    public ResponseEntity<Map<String, Object>> testScheduleFilters() {
        try {
            // Test filter by date
            var todayResult = scheduleService.getAllSchedules(1, 10, "startTime", "asc",
                    java.time.LocalDate.now(), null, null, null, null, null, null);

            // Test filter by month
            var monthResult = scheduleService.getAllSchedules(1, 10, "startTime", "asc",
                    null, 2024, 1, null, null, null, null);

            // Test filter by status
            var statusResult = scheduleService.getAllSchedules(1, 10, "startTime", "asc",
                    null, null, null, "SCHEDULED", null, null, null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test bộ lọc lịch thành công!");
            response.put("todaySchedules", todayResult);
            response.put("monthSchedules", monthResult);
            response.put("statusSchedules", statusResult);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi test bộ lọc lịch: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "communications-service");
        health.put("timestamp", LocalDateTime.now());
        health.put("features", Map.of(
                "email", "Available",
                "schedule", "Available",
                "notifications", "Available",
                "templates", "Available"));

        return ResponseEntity.ok(health);
    }
}
