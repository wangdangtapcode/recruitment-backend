package com.example.job_service.dto.offer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateOfferDTO {
    private Long candidateId;
    private Long positionId;
    private LocalDate probationStartDate;
    private String notes;
}
