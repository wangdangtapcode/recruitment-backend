package com.example.candidate_service.dto.application;

import java.time.LocalDate;
import com.example.candidate_service.utils.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thống kê - chỉ chứa dữ liệu cần thiết cho statistics service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatisticsDTO {
    private Long id;
    private LocalDate appliedDate;
    private ApplicationStatus status;
    private Long jobPositionId;
    private Long departmentId; // Lấy từ job position
    private Long candidateId;
}
