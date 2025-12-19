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
 * Thống kê theo phòng ban
 */
@Document(collection = "department_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatistics {
    
    @Id
    private String id;
    
    /**
     * ID phòng ban
     */
    private Long departmentId;
    
    /**
     * Tên phòng ban
     */
    private String departmentName;
    
    /**
     * Loại thống kê: MONTHLY, QUARTERLY, YEARLY
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
     * Tổng số nhân viên
     */
    private Long totalEmployees;
    
    /**
     * Số yêu cầu tuyển dụng
     */
    private Long totalRecruitmentRequests;
    
    /**
     * Số vị trí đã mở
     */
    private Long totalJobPositions;
    
    /**
     * Số đơn ứng tuyển
     */
    private Long totalApplications;
    
    /**
     * Số ứng viên đã tuyển
     */
    private Long hiredCount;
    
    /**
     * Tổng doanh thu (lương)
     */
    private java.math.BigDecimal totalRevenue;
    
    /**
     * Số phỏng vấn
     */
    private Long totalInterviews;
    
    /**
     * Tỷ lệ tuyển thành công (%)
     */
    private Double hireRate;
    
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

