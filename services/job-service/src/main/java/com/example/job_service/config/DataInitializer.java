package com.example.job_service.config;

import java.util.Arrays;
import java.util.logging.Logger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.job_service.model.JobCategory;
import com.example.job_service.model.JobSkill;
import com.example.job_service.repository.JobCategoryRepository;
import com.example.job_service.repository.JobSkillRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

    private final JobCategoryRepository jobCategoryRepository;
    private final JobSkillRepository jobSkillRepository;

    public DataInitializer(JobCategoryRepository jobCategoryRepository, JobSkillRepository jobSkillRepository) {
        this.jobCategoryRepository = jobCategoryRepository;
        this.jobSkillRepository = jobSkillRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (jobCategoryRepository.count() > 0 || jobSkillRepository.count() > 0) {
            logger.info("Dữ liệu đã tồn tài. Bỏ qua khởi tạo");
            return;
        }

        logger.info("Tạo dữ liệu...");

        // 1. Job Categories per department
        JobCategory techBackend = new JobCategory();
        techBackend.setName("Kỹ sư Backend");
        techBackend.setDescription("Nhóm kỹ thuật backend");
        techBackend.setDepartmentId(4L); // Phòng Kỹ thuật from user-service seed
        techBackend.setActive(true);

        JobCategory techFrontend = new JobCategory();
        techFrontend.setName("Kỹ sư Frontend");
        techFrontend.setDescription("Nhóm kỹ thuật frontend");
        techFrontend.setDepartmentId(4L);
        techFrontend.setActive(true);

        JobCategory saleExecutive = new JobCategory();
        saleExecutive.setName("Chuyên viên Kinh doanh");
        saleExecutive.setDescription("Nhóm kinh doanh");
        saleExecutive.setDepartmentId(3L); // Phòng Kinh doanh
        saleExecutive.setActive(true);

        JobCategory devOps = new JobCategory();
        devOps.setName("Kỹ sư DevOps");
        devOps.setDescription("Vận hành và CI/CD");
        devOps.setDepartmentId(4L);
        devOps.setActive(true);

        JobCategory qaEngineer = new JobCategory();
        qaEngineer.setName("Kỹ sư QA/QC");
        qaEngineer.setDescription("Kiểm thử đảm bảo chất lượng");
        qaEngineer.setDepartmentId(4L);
        qaEngineer.setActive(true);

        JobCategory dataEngineer = new JobCategory();
        dataEngineer.setName("Kỹ sư Dữ liệu");
        dataEngineer.setDescription("Xử lý pipeline dữ liệu");
        dataEngineer.setDepartmentId(4L);
        dataEngineer.setActive(true);

        JobCategory mobileDev = new JobCategory();
        mobileDev.setName("Lập trình viên Mobile");
        mobileDev.setDescription("Android/iOS");
        mobileDev.setDepartmentId(4L);
        mobileDev.setActive(true);

        JobCategory hrRecruiter = new JobCategory();
        hrRecruiter.setName("Chuyên viên Tuyển dụng");
        hrRecruiter.setDescription("Tuyển dụng nhân sự");
        hrRecruiter.setDepartmentId(2L); // Phòng Nhân sự
        hrRecruiter.setActive(true);

        JobCategory hrGeneralist = new JobCategory();
        hrGeneralist.setName("HR Generalist");
        hrGeneralist.setDescription("Tổng hợp nhân sự");
        hrGeneralist.setDepartmentId(2L);
        hrGeneralist.setActive(true);

        JobCategory marketingExec = new JobCategory();
        marketingExec.setName("Chuyên viên Marketing");
        marketingExec.setDescription("Thực thi chiến dịch marketing");
        marketingExec.setDepartmentId(5L); // Marketing
        marketingExec.setActive(true);

        JobCategory contentSpecialist = new JobCategory();
        contentSpecialist.setName("Content Specialist");
        contentSpecialist.setDescription("Sáng tạo nội dung");
        contentSpecialist.setDepartmentId(5L);
        contentSpecialist.setActive(true);

        JobCategory seoSpecialist = new JobCategory();
        seoSpecialist.setName("SEO Specialist");
        seoSpecialist.setDescription("Tối ưu hóa tìm kiếm");
        seoSpecialist.setDepartmentId(5L);
        seoSpecialist.setActive(true);

        JobCategory accountant = new JobCategory();
        accountant.setName("Kế toán viên");
        accountant.setDescription("Hạch toán kế toán");
        accountant.setDepartmentId(6L); // Kế toán
        accountant.setActive(true);

        JobCategory generalAccountant = new JobCategory();
        generalAccountant.setName("Kế toán tổng hợp");
        generalAccountant.setDescription("Báo cáo tài chính, tổng hợp số liệu");
        generalAccountant.setDepartmentId(6L);
        generalAccountant.setActive(true);

        jobCategoryRepository.saveAll(Arrays.asList(
                techBackend, techFrontend, saleExecutive,
                devOps, qaEngineer, dataEngineer, mobileDev,
                hrRecruiter, hrGeneralist,
                marketingExec, contentSpecialist, seoSpecialist,
                accountant, generalAccountant));

        // 2. Job Skills
        JobSkill java = new JobSkill();
        java.setName("Java");
        java.setDescription("Ngôn ngữ Java");
        java.setActive(true);

        JobSkill spring = new JobSkill();
        spring.setName("Spring Boot");
        spring.setDescription("Framework Spring Boot");
        spring.setActive(true);

        JobSkill react = new JobSkill();
        react.setName("React");
        react.setDescription("Thư viện React");
        react.setActive(true);

        JobSkill sql = new JobSkill();
        sql.setName("SQL");
        sql.setDescription("Cơ sở dữ liệu quan hệ");
        sql.setActive(true);

        JobSkill docker = new JobSkill();
        docker.setName("Docker");
        docker.setDescription("Containerization");
        docker.setActive(true);

        JobSkill kubernetes = new JobSkill();
        kubernetes.setName("Kubernetes");
        kubernetes.setDescription("Orchestration");
        kubernetes.setActive(true);

        JobSkill cicd = new JobSkill();
        cicd.setName("CI/CD");
        cicd.setDescription("Tự động hóa build & deploy");
        cicd.setActive(true);

        JobSkill git = new JobSkill();
        git.setName("Git");
        git.setDescription("Quản lý mã nguồn");
        git.setActive(true);

        JobSkill aws = new JobSkill();
        aws.setName("AWS");
        aws.setDescription("Dịch vụ đám mây AWS");
        aws.setActive(true);

        JobSkill gcp = new JobSkill();
        gcp.setName("GCP");
        gcp.setDescription("Dịch vụ đám mây GCP");
        gcp.setActive(true);

        JobSkill azure = new JobSkill();
        azure.setName("Azure");
        azure.setDescription("Dịch vụ đám mây Azure");
        azure.setActive(true);

        JobSkill python = new JobSkill();
        python.setName("Python");
        python.setDescription("Ngôn ngữ Python");
        python.setActive(true);

        JobSkill node = new JobSkill();
        node.setName("Node.js");
        node.setDescription("Nền tảng Node.js");
        node.setActive(true);

        JobSkill angular = new JobSkill();
        angular.setName("Angular");
        angular.setDescription("Framework Angular");
        angular.setActive(true);

        JobSkill vue = new JobSkill();
        vue.setName("Vue.js");
        vue.setDescription("Framework Vue");
        vue.setActive(true);

        JobSkill htmlcss = new JobSkill();
        htmlcss.setName("HTML/CSS");
        htmlcss.setDescription("Nền tảng web cơ bản");
        htmlcss.setActive(true);

        JobSkill rest = new JobSkill();
        rest.setName("REST API");
        rest.setDescription("Thiết kế và tích hợp API");
        rest.setActive(true);

        JobSkill micro = new JobSkill();
        micro.setName("Microservices");
        micro.setDescription("Thiết kế dịch vụ vi mô");
        micro.setActive(true);

        JobSkill kafka = new JobSkill();
        kafka.setName("Kafka");
        kafka.setDescription("Hàng đợi sự kiện/stream");
        kafka.setActive(true);

        JobSkill redis = new JobSkill();
        redis.setName("Redis");
        redis.setDescription("Cache/Message broker");
        redis.setActive(true);

        JobSkill elastic = new JobSkill();
        elastic.setName("Elasticsearch");
        elastic.setDescription("Tìm kiếm và phân tích");
        elastic.setActive(true);

        JobSkill communication = new JobSkill();
        communication.setName("Communication");
        communication.setDescription("Giao tiếp và phối hợp");
        communication.setActive(true);

        JobSkill problemSolving = new JobSkill();
        problemSolving.setName("Problem Solving");
        problemSolving.setDescription("Giải quyết vấn đề");
        problemSolving.setActive(true);

        JobSkill scrum = new JobSkill();
        scrum.setName("Scrum/Agile");
        scrum.setDescription("Quy trình làm việc linh hoạt");
        scrum.setActive(true);

        JobSkill jira = new JobSkill();
        jira.setName("Jira");
        jira.setDescription("Quản lý công việc/dự án");
        jira.setActive(true);

        JobSkill excel = new JobSkill();
        excel.setName("Excel");
        excel.setDescription("Phân tích/ báo cáo số liệu");
        excel.setActive(true);

        JobSkill seo = new JobSkill();
        seo.setName("SEO");
        seo.setDescription("Tối ưu công cụ tìm kiếm");
        seo.setActive(true);

        JobSkill content = new JobSkill();
        content.setName("Content Writing");
        content.setDescription("Viết nội dung/PR");
        content.setActive(true);

        JobSkill googleAds = new JobSkill();
        googleAds.setName("Google Ads");
        googleAds.setDescription("Quảng cáo Google");
        googleAds.setActive(true);

        JobSkill facebookAds = new JobSkill();
        facebookAds.setName("Facebook Ads");
        facebookAds.setDescription("Quảng cáo Facebook");
        facebookAds.setActive(true);

        JobSkill accounting = new JobSkill();
        accounting.setName("Accounting");
        accounting.setDescription("Nghiệp vụ kế toán");
        accounting.setActive(true);

        JobSkill financial = new JobSkill();
        financial.setName("Financial Reporting");
        financial.setDescription("Lập báo cáo tài chính");
        financial.setActive(true);

        jobSkillRepository.saveAll(Arrays.asList(
                java, spring, react,
                sql, docker, kubernetes, cicd, git,
                aws, gcp, azure,
                python, node, angular, vue, htmlcss,
                rest, micro, kafka, redis, elastic,
                communication, problemSolving, scrum, jira,
                excel, seo, content, googleAds, facebookAds,
                accounting, financial));

        logger.info("Tạo dữ liệu thành công.");
    }
}
