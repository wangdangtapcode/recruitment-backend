package com.example.statistics_service.repository.mongodb;

import com.example.statistics_service.model.mongodb.RecruitmentStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentStatisticsRepository extends MongoRepository<RecruitmentStatistics, String> {
    
    /**
     * Tìm thống kê theo loại kỳ, năm, tháng
     */
    Optional<RecruitmentStatistics> findByPeriodTypeAndYearAndMonth(
            String periodType, Integer year, Integer month);
    
    /**
     * Tìm thống kê theo loại kỳ, năm, quý
     */
    Optional<RecruitmentStatistics> findByPeriodTypeAndYearAndQuarter(
            String periodType, Integer year, Integer quarter);
    
    /**
     * Tìm thống kê theo loại kỳ, năm
     */
    Optional<RecruitmentStatistics> findByPeriodTypeAndYear(
            String periodType, Integer year);
    
    /**
     * Tìm thống kê theo loại kỳ, năm, tháng, ngày
     */
    Optional<RecruitmentStatistics> findByPeriodTypeAndYearAndMonthAndDay(
            String periodType, Integer year, Integer month, Integer day);
    
    /**
     * Tìm tất cả thống kê trong khoảng thời gian
     */
    @Query("{ 'periodStart': { $gte: ?0 }, 'periodEnd': { $lte: ?1 } }")
    List<RecruitmentStatistics> findByPeriodBetween(LocalDate start, LocalDate end);
    
    /**
     * Tìm thống kê theo năm
     */
    List<RecruitmentStatistics> findByYear(Integer year);
}

