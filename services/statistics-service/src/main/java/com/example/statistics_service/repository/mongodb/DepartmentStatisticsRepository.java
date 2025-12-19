package com.example.statistics_service.repository.mongodb;

import com.example.statistics_service.model.mongodb.DepartmentStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentStatisticsRepository extends MongoRepository<DepartmentStatistics, String> {
    
    /**
     * Tìm thống kê theo phòng ban, loại kỳ, năm, tháng
     */
    Optional<DepartmentStatistics> findByDepartmentIdAndPeriodTypeAndYearAndMonth(
            Long departmentId, String periodType, Integer year, Integer month);
    
    /**
     * Tìm thống kê theo phòng ban, loại kỳ, năm, quý
     */
    Optional<DepartmentStatistics> findByDepartmentIdAndPeriodTypeAndYearAndQuarter(
            Long departmentId, String periodType, Integer year, Integer quarter);
    
    /**
     * Tìm thống kê theo phòng ban, loại kỳ, năm
     */
    Optional<DepartmentStatistics> findByDepartmentIdAndPeriodTypeAndYear(
            Long departmentId, String periodType, Integer year);
    
    /**
     * Tìm tất cả thống kê của một phòng ban trong khoảng thời gian
     */
    @Query("{ 'departmentId': ?0, 'periodStart': { $gte: ?1 }, 'periodEnd': { $lte: ?2 } }")
    List<DepartmentStatistics> findByDepartmentIdAndPeriodBetween(
            Long departmentId, LocalDate start, LocalDate end);
    
    /**
     * Tìm thống kê theo phòng ban và năm
     */
    List<DepartmentStatistics> findByDepartmentIdAndYear(Long departmentId, Integer year);
    
    /**
     * Tìm tất cả thống kê trong khoảng thời gian
     */
    @Query("{ 'periodStart': { $gte: ?0 }, 'periodEnd': { $lte: ?1 } }")
    List<DepartmentStatistics> findByPeriodBetween(LocalDate start, LocalDate end);
}

