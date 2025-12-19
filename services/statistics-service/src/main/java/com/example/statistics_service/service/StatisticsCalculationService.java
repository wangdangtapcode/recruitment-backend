package com.example.statistics_service.service;

import com.example.statistics_service.model.mongodb.*;
import com.example.statistics_service.repository.mongodb.*;
import com.example.statistics_service.service.client.*;
import com.example.statistics_service.dto.PaginationDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Service tính toán và lưu thống kê vào MongoDB
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsCalculationService {

    private final RevenueStatisticsRepository revenueStatisticsRepository;
    private final RecruitmentStatisticsRepository recruitmentStatisticsRepository;
    private final ApplicationStatisticsRepository applicationStatisticsRepository;
    private final InterviewStatisticsRepository interviewStatisticsRepository;
    private final DepartmentStatisticsRepository departmentStatisticsRepository;
    
    private final UserServiceClient userServiceClient;
    private final JobServiceClient jobServiceClient;
    private final CandidateServiceClient candidateServiceClient;
    private final CommunicationServiceClient communicationServiceClient;

    /**
     * Tính toán và lưu thống kê doanh thu theo tháng/quý/năm
     */
    @Transactional
    public RevenueStatistics calculateAndSaveRevenueStatistics(String token, String periodType, Integer year, Integer month, Integer quarter) {
        log.info("Calculating revenue statistics: periodType={}, year={}, month={}, quarter={}", 
                periodType, year, month, quarter);
        
        LocalDate periodStart;
        LocalDate periodEnd;
        
        if ("MONTHLY".equals(periodType)) {
            YearMonth yearMonth = YearMonth.of(year, month);
            periodStart = yearMonth.atDay(1);
            periodEnd = yearMonth.atEndOfMonth();
        } else if ("QUARTERLY".equals(periodType)) {
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            periodStart = YearMonth.of(year, startMonth).atDay(1);
            periodEnd = YearMonth.of(year, endMonth).atEndOfMonth();
        } else { // YEARLY
            periodStart = LocalDate.of(year, 1, 1);
            periodEnd = LocalDate.of(year, 12, 31);
        }
        
        // Lấy tất cả offers đã được approve (ACCEPTED) trong kỳ
        PaginationDTO offers = jobServiceClient.getOffers(token, null, "ACCEPTED", 1, 1000);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long hiredCount = 0L;
        Map<Long, BigDecimal> revenueByDepartment = new HashMap<>();
        Map<Long, BigDecimal> revenueByPosition = new HashMap<>();
        
        if (offers != null && offers.getResult() != null) {
            @SuppressWarnings("unchecked")
            List<JsonNode> offerList = (List<JsonNode>) offers.getResult();
            
            for (JsonNode offer : offerList) {
                // Lấy thông tin từ offer
                Long positionId = offer.has("positionId") ? offer.get("positionId").asLong() : null;
                Long departmentId = offer.has("departmentId") ? offer.get("departmentId").asLong() : null;
                
                // Lấy lương từ job position
                if (positionId != null) {
                    PaginationDTO positions = jobServiceClient.getJobPositions(token, 1, 1000);
                    if (positions != null && positions.getResult() != null) {
                        @SuppressWarnings("unchecked")
                        List<JsonNode> positionList = (List<JsonNode>) positions.getResult();
                        for (JsonNode position : positionList) {
                            if (position.has("id") && position.get("id").asLong() == positionId) {
                                BigDecimal salaryMin = position.has("salaryMin") && !position.get("salaryMin").isNull()
                                        ? new BigDecimal(position.get("salaryMin").asText()) : null;
                                BigDecimal salaryMax = position.has("salaryMax") && !position.get("salaryMax").isNull()
                                        ? new BigDecimal(position.get("salaryMax").asText()) : null;
                                
                                // Tính lương trung bình
                                BigDecimal salary = BigDecimal.ZERO;
                                if (salaryMin != null && salaryMax != null) {
                                    salary = salaryMin.add(salaryMax).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                                } else if (salaryMin != null) {
                                    salary = salaryMin;
                                } else if (salaryMax != null) {
                                    salary = salaryMax;
                                }
                                
                                totalRevenue = totalRevenue.add(salary);
                                hiredCount++;
                                
                                if (departmentId != null) {
                                    revenueByDepartment.merge(departmentId, salary, BigDecimal::add);
                                }
                                if (positionId != null) {
                                    revenueByPosition.merge(positionId, salary, BigDecimal::add);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        BigDecimal averageRevenuePerEmployee = hiredCount > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(hiredCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Tìm hoặc tạo mới thống kê
        RevenueStatistics statistics = revenueStatisticsRepository
                .findByPeriodTypeAndYearAndMonth(periodType, year, month)
                .orElse(RevenueStatistics.builder()
                        .periodType(periodType)
                        .year(year)
                        .month(month)
                        .quarter(quarter)
                        .createdAt(LocalDateTime.now())
                        .build());
        
        statistics.setTotalRevenue(totalRevenue);
        statistics.setHiredCount(hiredCount);
        statistics.setAverageRevenuePerEmployee(averageRevenuePerEmployee);
        statistics.setRevenueByDepartment(revenueByDepartment);
        statistics.setRevenueByPosition(revenueByPosition);
        statistics.setPeriodStart(periodStart);
        statistics.setPeriodEnd(periodEnd);
        statistics.setUpdatedAt(LocalDateTime.now());
        
        return revenueStatisticsRepository.save(statistics);
    }

    /**
     * Tính toán và lưu thống kê tuyển dụng
     */
    @Transactional
    public RecruitmentStatistics calculateAndSaveRecruitmentStatistics(String token, String periodType, 
            Integer year, Integer month, Integer quarter, Integer day) {
        log.info("Calculating recruitment statistics: periodType={}, year={}, month={}, quarter={}, day={}", 
                periodType, year, month, quarter, day);
        
        LocalDate periodStart;
        LocalDate periodEnd;
        
        if ("DAILY".equals(periodType)) {
            periodStart = LocalDate.of(year, month, day);
            periodEnd = periodStart;
        } else if ("MONTHLY".equals(periodType)) {
            YearMonth yearMonth = YearMonth.of(year, month);
            periodStart = yearMonth.atDay(1);
            periodEnd = yearMonth.atEndOfMonth();
        } else if ("QUARTERLY".equals(periodType)) {
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            periodStart = YearMonth.of(year, startMonth).atDay(1);
            periodEnd = YearMonth.of(year, endMonth).atEndOfMonth();
        } else { // YEARLY
            periodStart = LocalDate.of(year, 1, 1);
            periodEnd = LocalDate.of(year, 12, 31);
        }
        
        // Lấy recruitment requests
        PaginationDTO allRequests = jobServiceClient.getRecruitmentRequests(token, null, 1, 1000);
        PaginationDTO pendingRequests = jobServiceClient.getRecruitmentRequests(token, "PENDING", 1, 1000);
        PaginationDTO approvedRequests = jobServiceClient.getRecruitmentRequests(token, "APPROVED", 1, 1000);
        PaginationDTO rejectedRequests = jobServiceClient.getRecruitmentRequests(token, "REJECTED", 1, 1000);
        
        // Lấy applications
        PaginationDTO allApplications = candidateServiceClient.getApplications(token, null, null, null, 1, 1000);
        PaginationDTO hiredApplications = candidateServiceClient.getApplications(token, "HIRED", null, null, 1, 1000);
        
        Map<String, Long> requestsByStatus = new HashMap<>();
        requestsByStatus.put("PENDING", getTotalFromPagination(pendingRequests));
        requestsByStatus.put("APPROVED", getTotalFromPagination(approvedRequests));
        requestsByStatus.put("REJECTED", getTotalFromPagination(rejectedRequests));
        
        Map<String, Long> applicationsByStatus = new HashMap<>();
        applicationsByStatus.put("HIRED", getTotalFromPagination(hiredApplications));
        
        Long totalRequests = getTotalFromPagination(allRequests);
        Long totalApplications = getTotalFromPagination(allApplications);
        Long hiredCount = getTotalFromPagination(hiredApplications);
        Long totalJobPositions = getTotalFromPagination(jobServiceClient.getJobPositions(token, 1, 1));
        
        Double hireRate = totalApplications > 0 
                ? (hiredCount.doubleValue() / totalApplications.doubleValue()) * 100 
                : 0.0;
        
        // Tính toán theo phòng ban
        Map<Long, Long> requestsByDepartment = new HashMap<>();
        
        RecruitmentStatistics statistics = recruitmentStatisticsRepository
                .findByPeriodTypeAndYearAndMonth(periodType, year, month)
                .orElse(RecruitmentStatistics.builder()
                        .periodType(periodType)
                        .year(year)
                        .month(month)
                        .quarter(quarter)
                        .day(day)
                        .createdAt(LocalDateTime.now())
                        .build());
        
        statistics.setTotalRequests(totalRequests);
        statistics.setRequestsByStatus(requestsByStatus);
        statistics.setTotalJobPositions(totalJobPositions);
        statistics.setTotalApplications(totalApplications);
        statistics.setApplicationsByStatus(applicationsByStatus);
        statistics.setHiredCount(hiredCount);
        statistics.setHireRate(hireRate);
        statistics.setRequestsByDepartment(requestsByDepartment);
        statistics.setPeriodStart(periodStart);
        statistics.setPeriodEnd(periodEnd);
        statistics.setUpdatedAt(LocalDateTime.now());
        
        return recruitmentStatisticsRepository.save(statistics);
    }

    /**
     * Tính toán và lưu thống kê ứng viên
     */
    @Transactional
    public ApplicationStatistics calculateAndSaveApplicationStatistics(String token, String periodType,
            Integer year, Integer month, Integer quarter, Integer day) {
        log.info("Calculating application statistics: periodType={}, year={}, month={}, quarter={}, day={}", 
                periodType, year, month, quarter, day);
        
        LocalDate periodStart;
        LocalDate periodEnd;
        
        if ("DAILY".equals(periodType)) {
            periodStart = LocalDate.of(year, month, day);
            periodEnd = periodStart;
        } else if ("MONTHLY".equals(periodType)) {
            YearMonth yearMonth = YearMonth.of(year, month);
            periodStart = yearMonth.atDay(1);
            periodEnd = yearMonth.atEndOfMonth();
        } else if ("QUARTERLY".equals(periodType)) {
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            periodStart = YearMonth.of(year, startMonth).atDay(1);
            periodEnd = YearMonth.of(year, endMonth).atEndOfMonth();
        } else { // YEARLY
            periodStart = LocalDate.of(year, 1, 1);
            periodEnd = LocalDate.of(year, 12, 31);
        }
        
        PaginationDTO candidates = candidateServiceClient.getCandidates(token, 1, 1000);
        PaginationDTO applications = candidateServiceClient.getApplications(token, null, null, null, 1, 1000);
        PaginationDTO hiredApplications = candidateServiceClient.getApplications(token, "HIRED", null, null, 1, 1000);
        
        Long totalCandidates = getTotalFromPagination(candidates);
        Long totalApplications = getTotalFromPagination(applications);
        Long hiredCount = getTotalFromPagination(hiredApplications);
        
        Map<String, Long> applicationsByStatus = new HashMap<>();
        applicationsByStatus.put("HIRED", hiredCount);
        
        Double conversionRate = totalApplications > 0 
                ? (hiredCount.doubleValue() / totalApplications.doubleValue()) * 100 
                : 0.0;
        
        ApplicationStatistics statistics = applicationStatisticsRepository
                .findByPeriodTypeAndYearAndMonth(periodType, year, month)
                .orElse(ApplicationStatistics.builder()
                        .periodType(periodType)
                        .year(year)
                        .month(month)
                        .quarter(quarter)
                        .day(day)
                        .createdAt(LocalDateTime.now())
                        .build());
        
        statistics.setTotalCandidates(totalCandidates);
        statistics.setTotalApplications(totalApplications);
        statistics.setApplicationsByStatus(applicationsByStatus);
        statistics.setConversionRate(conversionRate);
        statistics.setPeriodStart(periodStart);
        statistics.setPeriodEnd(periodEnd);
        statistics.setUpdatedAt(LocalDateTime.now());
        
        return applicationStatisticsRepository.save(statistics);
    }

    /**
     * Lấy revenue statistics repository (để sử dụng trong StatisticsService)
     */
    public RevenueStatisticsRepository getRevenueStatisticsRepository() {
        return revenueStatisticsRepository;
    }

    private Long getTotalFromPagination(PaginationDTO pagination) {
        if (pagination == null || pagination.getMeta() == null) {
            return 0L;
        }
        return pagination.getMeta().getTotal();
    }
}

