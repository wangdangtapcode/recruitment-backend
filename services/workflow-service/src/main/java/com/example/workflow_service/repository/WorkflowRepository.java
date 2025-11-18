package com.example.workflow_service.repository;

import com.example.workflow_service.model.Workflow;
import com.example.workflow_service.utils.enums.WorkflowType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    Optional<Workflow> findByName(String name);

    List<Workflow> findByTypeAndIsActiveTrue(WorkflowType type);

    List<Workflow> findByIsActiveTrue();

    Page<Workflow> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT w FROM Workflow w WHERE " +
            "(:type IS NULL OR w.type = :type) AND " +
            "(:isActive IS NULL OR w.isActive = :isActive) AND " +
            "(:keyword IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Workflow> findByFilters(
            @Param("type") WorkflowType type,
            @Param("isActive") Boolean isActive,
            @Param("keyword") String keyword,
            Pageable pageable);

    // Tìm workflow phù hợp dựa trên điều kiện (department_id, level_id)
    // apply_conditions là JSON string, cần parse để so sánh
    @Query(value = "SELECT * FROM workflows w WHERE " +
            "w.is_active = true AND " +
            "w.apply_conditions IS NOT NULL AND " +
            "JSON_EXTRACT(w.apply_conditions, '$.department_id') = :departmentId AND " +
            "JSON_EXTRACT(w.apply_conditions, '$.level_id') = :levelId " +
            "LIMIT 1", nativeQuery = true)
    Optional<Workflow> findMatchingWorkflow(
            @Param("departmentId") Long departmentId,
            @Param("levelId") Long levelId);
}
