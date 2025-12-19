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
 * Thống kê tuyển dụng theo thời gian
 */
@Document(collection = "recruitment_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentStatistics {
    
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
     * Tổng số yêu cầu tuyển dụng
     */
    private Long totalRequests;
    
    /**
     * Số yêu cầu theo trạng thái
     * Key: status (PENDING, APPROVED, REJECTED), Value: count
     */
    private Map<String, Long> requestsByStatus;
    
    /**
     * Tổng số vị trí đã mở
     */
    private Long totalJobPositions;
    
    /**
     * Tổng số ứng viên đã ứng tuyển
     */
    private Long totalApplications;
    
    /**
     * Số ứng viên theo trạng thái
     * Key: status (SUBMITTED, REVIEWING, INTERVIEW, OFFER, HIRED, REJECTED), Value: count
     */
    private Map<String, Long> applicationsByStatus;
    
    /**
     * Số ứng viên đã tuyển thành công
     */
    private Long hiredCount;
    
    /**
     * Tỷ lệ tuyển thành công (%)
     */
    private Double hireRate;
    
    /**
     * Thống kê theo phòng ban
     * Key: departmentId, Value: count
     */
    private Map<Long, Long> requestsByDepartment;
    
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

