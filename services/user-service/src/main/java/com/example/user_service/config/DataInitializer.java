package com.example.user_service.config;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.user_service.model.Department;
import com.example.user_service.model.Role;
import com.example.user_service.model.User;
import com.example.user_service.repository.DepartmentRepository;
import com.example.user_service.repository.RoleRepository;
import com.example.user_service.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {
        private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

        // Mật khẩu '123456' đã được mã hóa bằng BCrypt
        private static final String BCRYPTED_PASSWORD = "$2a$10$iM2Qd9bsZBZxHwmutzqyLOA1u7cGCK92xE2XG6zejUyDCWuZZbgLu";

        private final DepartmentRepository departmentRepository;
        private final RoleRepository roleRepository;
        private final UserRepository userRepository;

        public DataInitializer(DepartmentRepository departmentRepository, RoleRepository roleRepository,
                        UserRepository userRepository) {
                this.departmentRepository = departmentRepository;
                this.roleRepository = roleRepository;
                this.userRepository = userRepository;
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

                roleRepository.saveAll(Arrays.asList(adminRole, ceoRole, managerRole, staffRole));
                logger.info("Đã tạo " + roleRepository.count() + " vai trò.");

                // =====================================================================================
                // 3. TẠO NGƯỜI DÙNG (USERS)
                // =====================================================================================

                User ceo = new User();
                ceo.setName("Nguyễn Văn An");
                ceo.setEmail("an.nguyen@company.com");
                ceo.setPassword(BCRYPTED_PASSWORD);
                ceo.setPhone("0901234567");
                ceo.setAvataUrl("https://placehold.co/600x400/000000/FFFFFF?text=NVA");
                ceo.set_active(true);
                ceo.setDepartment(directorBoard);
                ceo.setRole(ceoRole);

                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(BCRYPTED_PASSWORD);
                admin.setPhone("0987654321");
                admin.setAvataUrl("https://placehold.co/600x400/000000/FFFFFF?text=A");
                admin.set_active(true);
                admin.setRole(adminRole);

                // Nhân sự
                User hrManager = new User();
                hrManager.setName("Trần Thị Bích");
                hrManager.setEmail("bich.tran@company.com");
                hrManager.setPassword(BCRYPTED_PASSWORD);
                hrManager.setPhone("0912345678");
                hrManager.setDepartment(hrDepartment);
                hrManager.setRole(managerRole);
                hrManager.set_active(true);

                User hrStaff1 = new User();
                hrStaff1.setName("Lê Văn Cường");
                hrStaff1.setEmail("cuong.le@company.com");
                hrStaff1.setPassword(BCRYPTED_PASSWORD);
                hrStaff1.setPhone("0918765432");
                hrStaff1.setDepartment(hrDepartment);
                hrStaff1.setRole(staffRole);
                hrStaff1.set_active(true);

                // Kinh doanh
                User salesManager = new User();
                salesManager.setName("Võ Minh Long");
                salesManager.setEmail("long.vo@company.com");
                salesManager.setPassword(BCRYPTED_PASSWORD);
                salesManager.setPhone("0933445566");
                salesManager.setDepartment(salesDepartment);
                salesManager.setRole(managerRole);
                salesManager.set_active(true);

                // Kỹ thuật
                User techManager = new User();
                techManager.setName("Bùi Đức Huy");
                techManager.setEmail("huy.bui@company.com");
                techManager.setPassword(BCRYPTED_PASSWORD);
                techManager.setPhone("0945678901");
                techManager.setDepartment(techDepartment);
                techManager.setRole(managerRole);
                techManager.set_active(true);

                List<User> users = Arrays.asList(
                                ceo, admin, hrManager, hrStaff1, salesManager, techManager);

                userRepository.saveAll(users);
                logger.info("Đã tạo " + userRepository.count() + " người dùng.");
                logger.info("Hoàn tất quá trình khởi tạo dữ liệu.");
        }
}
