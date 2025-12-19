package com.example.statistics_service.model.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Thống kê doanh thu theo tháng/quý/năm
 */
@Document(collection = "revenue_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatistics {
    
    @Id
    private String id;
    
    /**
     * Loại thống kê: MONTHLY, QUARTERLY, YEARLY
     */
    private String periodType;
    
    /**
     * Năm
     */
    private Integer year;
    
    /**
     * Tháng (1-12) - chỉ có khi periodType = MONTHLY
     */
    private Integer month;
    
    /**
     * Quý (1-4) - chỉ có khi periodType = QUARTERLY
     */
    private Integer quarter;
    
    /**
     * Tổng doanh thu (tổng lương của các vị trí đã tuyển)
     */
    private BigDecimal totalRevenue;
    
    /**
     * Số lượng nhân viên đã tuyển
     */
    private Long hiredCount;
    
    /**
     * Doanh thu trung bình mỗi nhân viên
     */
    private BigDecimal averageRevenuePerEmployee;
    
    /**
     * Doanh thu theo phòng ban
     * Key: departmentId, Value: totalRevenue
     */
    private Map<Long, BigDecimal> revenueByDepartment;
    
    /**
     * Doanh thu theo vị trí công việc
     * Key: jobPositionId, Value: totalRevenue
     */
    private Map<Long, BigDecimal> revenueByPosition;
    
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

