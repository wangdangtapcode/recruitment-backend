package com.example.job_service.dto.offer;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateOfferDTO {

    @NotNull(message = "ID ứng viên không được để trống")
    private Long candidateId;

    @NotNull(message = "ID vị trí không được để trống")
    private Long positionId;

    private LocalDate probationStartDate;

    private String notes;

    /**
     * Người tạo có thể chọn workflow cụ thể hoặc để hệ thống tự chọn.
     */
    private Long workflowId;

    @NotNull(message = "ID phòng ban không được để trống")
    private Long departmentId;
}
