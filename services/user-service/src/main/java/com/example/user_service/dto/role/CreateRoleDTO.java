package com.example.user_service.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoleDTO {
    @NotBlank(message = "Tên vai trò không được để trống")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

}
