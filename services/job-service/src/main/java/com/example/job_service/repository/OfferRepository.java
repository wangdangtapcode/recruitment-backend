package com.example.job_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.job_service.model.Offer;
import com.example.job_service.utils.enums.OfferStatus;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByDepartmentId(Long departmentId);

    List<Offer> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    Page<Offer> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT o FROM Offer o WHERE " +
            "o.isActive = true AND " +
            "(:departmentId IS NULL OR o.departmentId = :departmentId) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:createdBy IS NULL OR o.requesterId = :createdBy) AND " +
            "(:keyword IS NULL OR LOWER(o.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Offer> findByFilters(@Param("departmentId") Long departmentId,
            @Param("status") OfferStatus status,
            @Param("createdBy") Long createdBy,
            @Param("keyword") String keyword,
            Pageable pageable);
}
