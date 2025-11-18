package com.example.user_service.dto.position;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePositionDTO {
    @NotBlank(message = "Tên không được để trống")
    private String name;

    private String level;

    private Boolean isActive;
}
