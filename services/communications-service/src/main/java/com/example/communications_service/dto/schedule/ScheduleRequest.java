package com.example.communications_service.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduleRequest {

    @NotBlank(message = "tiêu đề là bắt buộc")
    private String title;

    @NotNull(message = "mô tả là bắt buộc")
    private String description;

    @NotNull(message = "format là bắt buộc")
    private String format; // "ONLINE", "OFFLINE", "HYBRID"

    @NotNull(message = "loại cuộc họp là bắt buộc")
    private String meetingType; // "INTERVIEW", "MEETING", "TRAINING", "OTHER"

    private String status; // "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"

    @NotNull(message = "vị trí là bắt buộc")
    private String location;

    @NotNull(message = "thời gian bắt đầu là bắt buộc")
    private LocalDateTime startTime;

    @NotNull(message = "End t   ime is required")
    private LocalDateTime endTime;

    private String timezone;

    private Integer reminderTime; // minutes before start

    private Long createdById;

    private List<ParticipantRequest> participants;

    @Data
    public static class ParticipantRequest {
        @NotBlank(message = "loại người tham gia là bắt buộc")
        private String participantType; // "USER", "CANDIDATE"

        @NotNull(message = "ID người tham gia là bắt buộc")
        private Long participantId;

        private String responseStatus; // "PENDING", "ACCEPTED", "DECLINED"
    }
}
