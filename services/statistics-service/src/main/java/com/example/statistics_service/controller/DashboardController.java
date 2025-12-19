package com.example.statistics_service.controller;

import com.example.statistics_service.dto.Response;
import com.example.statistics_service.dto.dashboard.*;
import com.example.statistics_service.service.StatisticsService;
import com.example.statistics_service.utils.SecurityUtil;
import com.example.statistics_service.utils.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/statistics-service/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StatisticsService statisticsService;

    /**
     * Lấy dashboard cho ADMIN
     * GET /api/v1/statistics-service/dashboard/admin
     */
    @GetMapping("/admin")
    @ApiMessage("Lấy dashboard cho ADMIN - Tổng quan toàn hệ thống")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        AdminDashboardDTO dashboard = statisticsService.getAdminDashboard(token);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Lấy dashboard cho CEO
     * GET /api/v1/statistics-service/dashboard/ceo
     */
    @GetMapping("/ceo")
    @ApiMessage("Lấy dashboard cho CEO - Tổng quan công ty và phê duyệt")
    public ResponseEntity<CEODashboardDTO> getCEODashboard() {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        CEODashboardDTO dashboard = statisticsService.getCEODashboard(token);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Lấy dashboard cho MANAGER
     * GET /api/v1/statistics-service/dashboard/manager
     */
    @GetMapping("/manager")
    @ApiMessage("Lấy dashboard cho MANAGER - Tổng quan phòng ban")
    public ResponseEntity<ManagerDashboardDTO> getManagerDashboard(
            @RequestParam(name = "departmentId", required = false) Long departmentId) {

        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        // Nếu không có departmentId, lấy từ token
        if (departmentId == null) {
            departmentId = SecurityUtil.extractDepartmentId();
        }

        ManagerDashboardDTO dashboard = statisticsService.getManagerDashboard(token, departmentId);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Lấy dashboard cho STAFF
     * GET /api/v1/statistics-service/dashboard/staff
     */
    @GetMapping("/staff")
    @ApiMessage("Lấy dashboard cho STAFF - Công việc cá nhân")
    public ResponseEntity<StaffDashboardDTO> getStaffDashboard() {
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        Long userId = SecurityUtil.extractUserId();
        if (userId == null) {
            Response<StaffDashboardDTO> errorResponse = new Response<>();
            errorResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            errorResponse.setError("Không tìm thấy thông tin người dùng");
            errorResponse.setMessage("Vui lòng đăng nhập lại");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        StaffDashboardDTO dashboard = statisticsService.getStaffDashboard(token, userId);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Lấy dashboard tự động dựa trên role của user
     * GET /api/v1/statistics-service/dashboard/my
     */
    @GetMapping("/my")
    @ApiMessage("Lấy dashboard tự động dựa trên role của người dùng")
    public ResponseEntity<?> getMyDashboard() {
        String role = SecurityUtil.extractUserRole();
        Optional<String> tokenOpt = SecurityUtil.getCurrentUserJWT();
        String token = tokenOpt.orElse(null);

        if (role == null) {
            Response<Object> errorResponse = new Response<>();
            errorResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            errorResponse.setError("Không tìm thấy role của người dùng");
            errorResponse.setMessage("Vui lòng đăng nhập lại");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        switch (role.toUpperCase()) {
            case "ADMIN": {
                AdminDashboardDTO dashboard = statisticsService.getAdminDashboard(token);
                return ResponseEntity.ok(dashboard);
            }
            case "CEO": {
                CEODashboardDTO dashboard = statisticsService.getCEODashboard(token);
                return ResponseEntity.ok(dashboard);
            }
            case "MANAGER": {
                Long departmentId = SecurityUtil.extractDepartmentId();
                ManagerDashboardDTO dashboard = statisticsService.getManagerDashboard(token, departmentId);
                return ResponseEntity.ok(dashboard);
            }
            case "STAFF": {
                Long userId = SecurityUtil.extractUserId();
                if (userId == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }
                StaffDashboardDTO dashboard = statisticsService.getStaffDashboard(token, userId);
                return ResponseEntity.ok(dashboard);
            }
            default: {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
        }
    }
}
