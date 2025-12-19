package com.example.statistics_service.model.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Thống kê ứng viên và đơn ứng tuyển
 */
@Document(collection = "application_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatistics {
    
    @Id
    private String id;
    
    /**
     * Loại thống kê: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
     */
    private String periodType;
    
    /**
     * Năm
     */
    private Integer year;
    
    /**
     * Tháng (1-12)
     */
    private Integer month;
    
    /**
     * Quý (1-4)
     */
    private Integer quarter;
    
    /**
     * Ngày (1-31)
     */
    private Integer day;
    
    /**
     * Tổng số ứng viên mới
     */
    private Long totalCandidates;
    
    /**
     * Tổng số đơn ứng tuyển
     */
    private Long totalApplications;
    
    /**
     * Số đơn ứng tuyển theo trạng thái
     * Key: status, Value: count
     */
    private Map<String, Long> applicationsByStatus;
    
    /**
     * Số ứng viên theo giai đoạn
     * Key: stage, Value: count
     */
    private Map<String, Long> candidatesByStage;
    
    /**
     * Số đơn ứng tuyển theo vị trí
     * Key: jobPositionId, Value: count
     */
    private Map<Long, Long> applicationsByPosition;
    
    /**
     * Số đơn ứng tuyển theo phòng ban
     * Key: departmentId, Value: count
     */
    private Map<Long, Long> applicationsByDepartment;
    
    /**
     * Tỷ lệ chuyển đổi (từ ứng tuyển -> tuyển thành công) (%)
     */
    private Double conversionRate;
    
    /**
     * Thời gian xử lý trung bình (ngày)
     */
    private Double averageProcessingTime;
    
    /**
     * Ngày bắt đầu của kỳ thống kê
     */
    private LocalDate periodStart;
    
    /**
     * Ngày kết thúc của kỳ thống kê
     */
    private LocalDate periodEnd;
    
    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;
}

