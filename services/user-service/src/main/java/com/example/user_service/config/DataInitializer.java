package com.example.user_service.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.user_service.model.Department;
import com.example.user_service.model.Employee;
import com.example.user_service.model.Position;
import com.example.user_service.model.Role;
import com.example.user_service.model.Permission;
import com.example.user_service.model.User;
import com.example.user_service.repository.DepartmentRepository;
import com.example.user_service.repository.EmployeeRepository;
import com.example.user_service.repository.PositionRepository;
import com.example.user_service.repository.RoleRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.repository.PermissionRepository;

@Component
public class DataInitializer implements CommandLineRunner {
        private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

        // Mật khẩu '123456' đã được mã hóa bằng BCrypt
        private static final String BCRYPTED_PASSWORD = "$2a$10$iM2Qd9bsZBZxHwmutzqyLOA1u7cGCK92xE2XG6zejUyDCWuZZbgLu";

        private final DepartmentRepository departmentRepository;
        private final PositionRepository positionRepository;
        private final EmployeeRepository employeeRepository;
        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final PermissionRepository permissionRepository;

        public DataInitializer(DepartmentRepository departmentRepository, PositionRepository positionRepository,
                        EmployeeRepository employeeRepository, RoleRepository roleRepository,
                        UserRepository userRepository, PermissionRepository permissionRepository) {
                this.departmentRepository = departmentRepository;
                this.positionRepository = positionRepository;
                this.employeeRepository = employeeRepository;
                this.roleRepository = roleRepository;
                this.userRepository = userRepository;
                this.permissionRepository = permissionRepository;
        }

        @Override
        public void run(String... args) throws Exception {
                if (userRepository.count() > 0) {
                        logger.info("Dữ liệu đã tồn tại. Bỏ qua quá trình khởi tạo.");
                        return;
                }

                logger.info("Bắt đầu quá trình khởi tạo dữ liệu mẫu...");

                // =====================================================================================
                // 1. TẠO PHÒNG BAN (DEPARTMENTS)
                // =====================================================================================
                Department directorBoard = new Department();
                directorBoard.setName("Ban Giám đốc");
                directorBoard.setDescription("Chịu trách nhiệm quản lý và điều hành chung toàn bộ công ty.");
                directorBoard.set_active(true);

                Department hrDepartment = new Department();
                hrDepartment.setName("Phòng Nhân sự");
                hrDepartment.setDescription(
                                "Chịu trách nhiệm về các vấn đề liên quan đến con người, bao gồm tuyển dụng, đào tạo và chính sách.");
                hrDepartment.set_active(true);

                Department salesDepartment = new Department();
                salesDepartment.setName("Phòng Kinh doanh");
                salesDepartment
                                .setDescription("Phụ trách các hoạt động bán hàng, tìm kiếm và duy trì mối quan hệ với khách hàng.");
                salesDepartment.set_active(true);

                Department techDepartment = new Department();
                techDepartment.setName("Phòng Kỹ thuật");
                techDepartment.setDescription(
                                "Phát triển, bảo trì và vận hành các sản phẩm, hệ thống công nghệ của công ty.");
                techDepartment.set_active(true);

                Department marketingDepartment = new Department();
                marketingDepartment.setName("Phòng Marketing");
                marketingDepartment
                                .setDescription("Xây dựng và thực hiện các chiến lược tiếp thị, quảng bá thương hiệu và sản phẩm.");
                marketingDepartment.set_active(true);

                Department accountingDepartment = new Department();
                accountingDepartment.setName("Phòng Kế toán");
                accountingDepartment.setDescription("Quản lý các vấn đề tài chính, kế toán và thuế của công ty.");
                accountingDepartment.set_active(true);

                departmentRepository.saveAll(Arrays.asList(directorBoard, hrDepartment, salesDepartment, techDepartment,
                                marketingDepartment, accountingDepartment));
                logger.info("Đã tạo " + departmentRepository.count() + " phòng ban.");

                // =====================================================================================
                // 1.1 TẠO VỊ TRÍ (POSITIONS)
                // =====================================================================================
                Position ceoPosition = new Position();
                ceoPosition.setName("Giám đốc điều hành");
                ceoPosition.setLevel("C-Level");
                ceoPosition.setActive(true);

                Position managerPosition = new Position();
                managerPosition.setName("Trưởng phòng");
                managerPosition.setLevel("Manager");
                managerPosition.setActive(true);

                Position seniorStaffPosition = new Position();
                seniorStaffPosition.setName("Nhân viên cấp cao");
                seniorStaffPosition.setLevel("Senior");
                seniorStaffPosition.setActive(true);

                Position staffPosition = new Position();
                staffPosition.setName("Nhân viên");
                staffPosition.setLevel("Staff");
                staffPosition.setActive(true);

                Position adminPosition = new Position();
                adminPosition.setName("Quản trị viên");
                adminPosition.setLevel("Admin");
                adminPosition.setActive(true);

                positionRepository.saveAll(Arrays.asList(ceoPosition, managerPosition, seniorStaffPosition,
                                staffPosition, adminPosition));
                logger.info("Đã tạo " + positionRepository.count() + " vị trí.");

                // =====================================================================================
                // 2. TẠO VAI TRÒ (ROLES)
                // =====================================================================================
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                adminRole.setDescription("Quản trị viên hệ thống, có quyền cao nhất.");
                adminRole.set_active(true);

                Role ceoRole = new Role();
                ceoRole.setName("CEO");
                ceoRole.setDescription(
                                "Giám đốc điều hành, người duyệt cuối cùng các yêu cầu và kế hoạch tuyển dụng quan trọng.");
                ceoRole.set_active(true);

                Role managerRole = new Role();
                managerRole.setName("MANAGER");
                managerRole.setDescription("Trưởng phòng, người đề xuất nhu cầu tuyển dụng và duyệt hồ sơ ứng viên.");
                managerRole.set_active(true);

                Role staffRole = new Role();
                staffRole.setName("STAFF");
                staffRole.setDescription("Nhân viên, người thực hiện các tác vụ chuyên môn.");
                staffRole.set_active(true);

                logger.info("Đã tạo " + 4 + " vai trò.");

                // =====================================================================================
                // 2.1 TẠO QUYỀN (PERMISSIONS) THEO QUY ƯỚC <service>:<resource>:<action>
                // Cho các controller trong user-service, job-service, candidate-service,
                // communications-service
                // =====================================================================================
                List<String> permissionNames = Arrays.asList(
                                // user-service permissions
                                "user-service:permissions:read", "user-service:permissions:create",
                                "user-service:permissions:update", "user-service:permissions:delete",
                                "user-service:roles:read", "user-service:roles:create", "user-service:roles:update",
                                "user-service:roles:delete",
                                "user-service:departments:read", "user-service:departments:create",
                                "user-service:departments:update", "user-service:departments:delete",
                                "user-service:employees:read", "user-service:employees:create",
                                "user-service:employees:update", "user-service:employees:delete",
                                "user-service:positions:read", "user-service:positions:create",
                                "user-service:positions:update", "user-service:positions:delete",
                                "user-service:users:read", "user-service:users:create", "user-service:users:update",
                                "user-service:users:delete",
                                // job-service permissions
                                "job-service:job-positions:read", "job-service:job-positions:create",
                                "job-service:job-positions:update", "job-service:job-positions:delete",
                                "job-service:job-positions:publish", "job-service:job-positions:close",
                                "job-service:job-positions:reopen",
                                "job-service:recruitment-requests:read", "job-service:recruitment-requests:create",
                                "job-service:recruitment-requests:update", "job-service:recruitment-requests:delete",
                                "job-service:recruitment-requests:approve", "job-service:recruitment-requests:reject",
                                "job-service:job-skills:read",
                                "job-service:job-categories:read",
                                // candidate-service permissions
                                "candidate-service:applications:read", "candidate-service:applications:create",
                                "candidate-service:applications:update", "candidate-service:applications:delete",
                                "candidate-service:applications:status", "candidate-service:applications:accept",
                                "candidate-service:applications:reject",
                                "candidate-service:candidates:read", "candidate-service:candidates:create",
                                "candidate-service:candidates:update", "candidate-service:candidates:delete",
                                "candidate-service:candidates:change-stage",
                                "candidate-service:comments:read", "candidate-service:comments:create",
                                "candidate-service:comments:update", "candidate-service:comments:delete",
                                // communications-service permissions
                                "communications-service:schedules:read", "communications-service:schedules:create",
                                "communications-service:schedules:update", "communications-service:schedules:delete",
                                "communications-service:schedules:update-status",
                                "communications-service:schedules:calendar");

                List<Permission> savedPermissions = permissionRepository
                                .saveAll(permissionNames.stream().map(name -> {
                                        Permission p = new Permission();
                                        p.setName(name);
                                        p.setActive(true);
                                        return p;
                                }).toList());

                Map<String, Permission> permissionMap = savedPermissions.stream()
                                .collect(Collectors.toMap(Permission::getName, p -> p));
                Function<String, Permission> requirePermission = name -> {
                        Permission p = permissionMap.get(name);
                        if (p == null) {
                                throw new IllegalStateException("Permission not found: " + name);
                        }
                        return p;
                };

                // Gán quyền cho từng vai trò
                adminRole.setPermissions(new ArrayList<>(savedPermissions));

                ceoRole.setPermissions(new ArrayList<>(List.of(
                                requirePermission.apply("user-service:departments:read"),
                                requirePermission.apply("user-service:employees:read"),
                                requirePermission.apply("user-service:positions:read"),
                                requirePermission.apply("user-service:users:read"),
                                requirePermission.apply("job-service:job-positions:read"),
                                requirePermission.apply("job-service:job-positions:publish"),
                                requirePermission.apply("job-service:job-positions:close"),
                                requirePermission.apply("job-service:job-positions:reopen"),
                                requirePermission.apply("job-service:recruitment-requests:read"),
                                requirePermission.apply("job-service:recruitment-requests:approve"),
                                requirePermission.apply("job-service:recruitment-requests:reject"),
                                requirePermission.apply("candidate-service:applications:read"),
                                requirePermission.apply("candidate-service:applications:accept"),
                                requirePermission.apply("candidate-service:applications:reject"),
                                requirePermission.apply("candidate-service:candidates:read"),
                                requirePermission.apply("candidate-service:comments:read"),
                                requirePermission.apply("communications-service:schedules:read"),
                                requirePermission.apply("communications-service:schedules:update-status"),
                                requirePermission.apply("communications-service:schedules:calendar"))));

                managerRole.setPermissions(new ArrayList<>(List.of(
                                requirePermission.apply("user-service:departments:read"),
                                requirePermission.apply("user-service:employees:read"),
                                requirePermission.apply("user-service:employees:create"),
                                requirePermission.apply("user-service:employees:update"),
                                requirePermission.apply("user-service:positions:read"),
                                requirePermission.apply("user-service:users:read"),
                                requirePermission.apply("job-service:job-positions:read"),
                                requirePermission.apply("job-service:job-positions:create"),
                                requirePermission.apply("job-service:job-positions:update"),
                                requirePermission.apply("job-service:recruitment-requests:read"),
                                requirePermission.apply("job-service:recruitment-requests:create"),
                                requirePermission.apply("job-service:recruitment-requests:update"),
                                requirePermission.apply("job-service:job-skills:read"),
                                requirePermission.apply("job-service:job-categories:read"),
                                requirePermission.apply("candidate-service:applications:read"),
                                requirePermission.apply("candidate-service:applications:create"),
                                requirePermission.apply("candidate-service:applications:update"),
                                requirePermission.apply("candidate-service:applications:status"),
                                requirePermission.apply("candidate-service:candidates:read"),
                                requirePermission.apply("candidate-service:candidates:create"),
                                requirePermission.apply("candidate-service:candidates:update"),
                                requirePermission.apply("candidate-service:candidates:change-stage"),
                                requirePermission.apply("candidate-service:comments:read"),
                                requirePermission.apply("candidate-service:comments:create"),
                                requirePermission.apply("candidate-service:comments:update"),
                                requirePermission.apply("communications-service:schedules:read"),
                                requirePermission.apply("communications-service:schedules:create"),
                                requirePermission.apply("communications-service:schedules:update"))));

                staffRole.setPermissions(new ArrayList<>(List.of(
                                requirePermission.apply("user-service:departments:read"),
                                requirePermission.apply("user-service:employees:read"),
                                requirePermission.apply("user-service:positions:read"),
                                requirePermission.apply("job-service:job-positions:read"),
                                requirePermission.apply("job-service:job-skills:read"),
                                requirePermission.apply("job-service:job-categories:read"),
                                requirePermission.apply("job-service:recruitment-requests:read"),
                                requirePermission.apply("candidate-service:applications:create"),
                                requirePermission.apply("candidate-service:applications:read"),
                                requirePermission.apply("candidate-service:applications:update"),
                                requirePermission.apply("candidate-service:candidates:read"),
                                requirePermission.apply("candidate-service:comments:create"),
                                requirePermission.apply("candidate-service:comments:read"),
                                requirePermission.apply("communications-service:schedules:read"))));

                roleRepository.saveAll(Arrays.asList(adminRole, ceoRole, managerRole, staffRole));
                logger.info("Đã gán quyền cho các vai trò.");

                // =====================================================================================
                // 3. TẠO NHÂN VIÊN (EMPLOYEES) - TẠO TRƯỚC
                // =====================================================================================
                Employee ceoEmployee = buildEmployee("Nguyễn Văn An", "an.nguyen@company.com", "0901234567",
                                directorBoard, ceoPosition, true);

                Employee adminEmployee = buildEmployee("Admin", "admin@gmail.com", "0987654321", null, adminPosition,
                                true);

                // Nhân sự
                Employee hrManagerEmployee = buildEmployee("Trần Thị Bích", "bich.tran@company.com", "0912345678",
                                hrDepartment, managerPosition, true);

                Employee hrStaff1Employee = buildEmployee("Lê Văn Cường", "cuong.le@company.com", "0918765432",
                                hrDepartment, staffPosition, true);

                // Kinh doanh
                Employee salesManagerEmployee = buildEmployee("Võ Minh Long", "long.vo@company.com", "0933445566",
                                salesDepartment, managerPosition, true);

                // Kỹ thuật
                Employee techManagerEmployee = buildEmployee("Bùi Đức Huy", "huy.bui@company.com", "0945678901",
                                techDepartment, managerPosition, true);

                List<Employee> employees = Arrays.asList(ceoEmployee, adminEmployee, hrManagerEmployee,
                                hrStaff1Employee, salesManagerEmployee, techManagerEmployee);

                employeeRepository.saveAll(employees);
                logger.info("Đã tạo " + employeeRepository.count() + " nhân viên.");

                // Refresh employees từ database để tránh detached entity
                ceoEmployee = employeeRepository.findById(ceoEmployee.getId()).orElse(ceoEmployee);
                adminEmployee = employeeRepository.findById(adminEmployee.getId()).orElse(adminEmployee);
                hrManagerEmployee = employeeRepository.findById(hrManagerEmployee.getId()).orElse(hrManagerEmployee);
                hrStaff1Employee = employeeRepository.findById(hrStaff1Employee.getId()).orElse(hrStaff1Employee);
                salesManagerEmployee = employeeRepository.findById(salesManagerEmployee.getId())
                                .orElse(salesManagerEmployee);
                techManagerEmployee = employeeRepository.findById(techManagerEmployee.getId())
                                .orElse(techManagerEmployee);

                // =====================================================================================
                // 4. TẠO NGƯỜI DÙNG (USERS) - TẠO SAU VÀ LINK VỚI EMPLOYEE ĐÃ TỒN TẠI
                // =====================================================================================
                User ceo = buildUser("Nguyễn Văn An", "an.nguyen@company.com", ceoRole, ceoEmployee, true);
                User admin = buildUser("Admin", "admin@gmail.com", adminRole, adminEmployee, true);
                User hrManager = buildUser("Trần Thị Bích", "bich.tran@company.com", managerRole, hrManagerEmployee,
                                true);
                User hrStaff1 = buildUser("Lê Văn Cường", "cuong.le@company.com", staffRole, hrStaff1Employee, true);
                User salesManager = buildUser("Võ Minh Long", "long.vo@company.com", managerRole, salesManagerEmployee,
                                true);
                User techManager = buildUser("Bùi Đức Huy", "huy.bui@company.com", managerRole, techManagerEmployee,
                                true);

                List<User> users = Arrays.asList(ceo, admin, hrManager, hrStaff1, salesManager, techManager);

                userRepository.saveAll(users);
                logger.info("Đã tạo " + userRepository.count() + " người dùng.");
                logger.info("Hoàn tất quá trình khởi tạo dữ liệu.");
        }

        private Employee buildEmployee(String name, String email, String phone, Department department,
                        Position position, boolean active) {
                Employee employee = new Employee();
                employee.setName(name);
                employee.setEmail(email);
                employee.setPhone(phone);
                employee.setDepartment(department);
                employee.setPosition(position);
                employee.setStatus(active ? "ACTIVE" : "INACTIVE");
                return employee;
        }

        private User buildUser(String name, String email, Role role, Employee employee, boolean active) {
                User user = new User();
                // User không có field name, name lấy từ Employee
                user.setEmail(email);
                user.setPassword(BCRYPTED_PASSWORD);
                user.setRole(role);
                user.set_active(active);

                // Link với Employee đã tồn tại
                user.setEmployee(employee);
                employee.setUser(user);

                return user;
        }
}
