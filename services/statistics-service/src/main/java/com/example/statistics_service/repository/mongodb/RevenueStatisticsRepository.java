package com.example.statistics_service.repository.mongodb;

import com.example.statistics_service.model.mongodb.RevenueStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RevenueStatisticsRepository extends MongoRepository<RevenueStatistics, String> {
    
    /**
     * Tìm thống kê theo loại kỳ, năm, tháng
     */
    Optional<RevenueStatistics> findByPeriodTypeAndYearAndMonth(
            String periodType, Integer year, Integer month);
    
    /**
     * Tìm thống kê theo loại kỳ, năm, quý
     */
    Optional<RevenueStatistics> findByPeriodTypeAndYearAndQuarter(
            String periodType, Integer year, Integer quarter);
    
    /**
     * Tìm thống kê theo loại kỳ, năm
     */
    Optional<RevenueStatistics> findByPeriodTypeAndYear(
            String periodType, Integer year);
    
    /**
     * Tìm tất cả thống kê trong khoảng thời gian
     */
    @Query("{ 'periodStart': { $gte: ?0 }, 'periodEnd': { $lte: ?1 } }")
    List<RevenueStatistics> findByPeriodBetween(LocalDate start, LocalDate end);
    
    /**
     * Tìm thống kê theo năm
     */
    List<RevenueStatistics> findByYear(Integer year);
    
    /**
     * Tìm thống kê theo năm và loại kỳ
     */
    List<RevenueStatistics> findByYearAndPeriodType(Integer year, String periodType);
}

