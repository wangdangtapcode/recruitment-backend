package com.example.statistics_service.repository.mongodb;

import com.example.statistics_service.model.mongodb.ApplicationStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationStatisticsRepository extends MongoRepository<ApplicationStatistics, String> {
    
    /**
     * Tìm thống kê theo loại kỳ, năm, tháng
     */
    Optional<ApplicationStatistics> findByPeriodTypeAndYearAndMonth(
            String periodType, Integer year, Integer month);
    
    /**
     * Tìm thống kê theo loại kỳ, năm, quý
     */
    Optional<ApplicationStatistics> findByPeriodTypeAndYearAndQuarter(
            String periodType, Integer year, Integer quarter);
    
    /**
     * Tìm thống kê theo loại kỳ, năm
     */
    Optional<ApplicationStatistics> findByPeriodTypeAndYear(
            String periodType, Integer year);
    
    /**
     * Tìm thống kê theo loại kỳ, năm, tháng, ngày
     */
    Optional<ApplicationStatistics> findByPeriodTypeAndYearAndMonthAndDay(
            String periodType, Integer year, Integer month, Integer day);
    
    /**
     * Tìm tất cả thống kê trong khoảng thời gian
     */
    @Query("{ 'periodStart': { $gte: ?0 }, 'periodEnd': { $lte: ?1 } }")
    List<ApplicationStatistics> findByPeriodBetween(LocalDate start, LocalDate end);
    
    /**
     * Tìm thống kê theo năm
     */
    List<ApplicationStatistics> findByYear(Integer year);
}

