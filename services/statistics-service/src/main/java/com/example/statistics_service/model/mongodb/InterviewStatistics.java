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
 * Thống kê phỏng vấn
 */
@Document(collection = "interview_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStatistics {
    
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
     * Tổng số cuộc phỏng vấn
     */
    private Long totalInterviews;
    
    /**
     * Số phỏng vấn theo trạng thái
     * Key: status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, RESCHEDULED), Value: count
     */
    private Map<String, Long> interviewsByStatus;
    
    /**
     * Số phỏng vấn theo loại
     * Key: type (PHONE, VIDEO, IN_PERSON, TECHNICAL, HR), Value: count
     */
    private Map<String, Long> interviewsByType;
    
    /**
     * Số phỏng vấn theo phòng ban
     * Key: departmentId, Value: count
     */
    private Map<Long, Long> interviewsByDepartment;
    
    /**
     * Tỷ lệ hoàn thành phỏng vấn (%)
     */
    private Double completionRate;
    
    /**
     * Tỷ lệ hủy phỏng vấn (%)
     */
    private Double cancellationRate;
    
    /**
     * Đánh giá trung bình (1-5 sao)
     */
    private Double averageRating;
    
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

