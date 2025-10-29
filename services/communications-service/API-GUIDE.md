# Communications Service - API Guide

## Tổng quan

Communications Service cung cấp các tính năng quản lý lịch và email cho hệ thống tuyển dụng.

## Các tính năng chính

### 1. Quản lý Lịch (Schedule Management)

#### Tạo lịch mới

```http
POST /api/v1/communications-service/schedules
Content-Type: application/json

{
  "title": "Phỏng vấn ứng viên",
  "description": "Phỏng vấn vị trí Java Developer",
  "format": "ONLINE",
  "meetingType": "INTERVIEW",
  "status": "SCHEDULED",
  "location": "Phòng họp ảo",
  "startTime": "2024-01-15T14:00:00",
  "endTime": "2024-01-15T15:00:00",
  "timezone": "Asia/Ho_Chi_Minh",
  "reminderTime": 15,
  "createdById": 1,
  "participants": [
    {
      "participantType": "CANDIDATE",
      "participantId": 123,
      "responseStatus": "PENDING"
    }
  ]
}
```

#### Xem tất cả lịch (có phân trang và lọc)

```http
GET /api/v1/communications-service/schedules?page=1&limit=10&sortBy=startTime&sortOrder=asc
```

**Các tham số lọc:**

- `date`: Lọc theo ngày cụ thể (format: YYYY-MM-DD)
- `year`: Lọc theo năm
- `month`: Lọc theo tháng (1-12)
- `status`: Lọc theo trạng thái (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)
- `meetingType`: Lọc theo loại cuộc họp (INTERVIEW, MEETING, TRAINING, OTHER)
- `participantId`: Lọc theo ID người tham gia
- `participantType`: Lọc theo loại người tham gia (USER, CANDIDATE)

**Ví dụ sử dụng:**

```http
# Lấy tất cả lịch
GET /api/v1/communications-service/schedules

# Lịch theo ngày cụ thể
GET /api/v1/communications-service/schedules?date=2024-01-15

# Lịch theo tháng
GET /api/v1/communications-service/schedules?year=2024&month=1

# Lịch phỏng vấn
GET /api/v1/communications-service/schedules?meetingType=INTERVIEW

# Lịch của một người tham gia
GET /api/v1/communications-service/schedules?participantId=123&participantType=CANDIDATE

# Kết hợp nhiều bộ lọc
GET /api/v1/communications-service/schedules?year=2024&month=1&status=SCHEDULED&meetingType=INTERVIEW
```

#### Cập nhật lịch

```http
PUT /api/v1/communications-service/schedules/{id}
Content-Type: application/json

{
  "title": "Phỏng vấn ứng viên - Cập nhật",
  "status": "IN_PROGRESS"
}
```

#### Xóa lịch

```http
DELETE /api/v1/communications-service/schedules/{id}
```

### 2. Quản lý Email

#### Gửi email đơn giản

```http
POST /api/v1/communications-service/emails/send
Content-Type: application/json

{
  "to": "candidate@example.com",
  "subject": "Thông báo kết quả phỏng vấn",
  "message": "Xin chào, chúng tôi sẽ thông báo kết quả phỏng vấn trong 2 ngày tới."
}
```

#### Gửi email HTML

```http
POST /api/v1/communications-service/emails/send-html
Content-Type: application/json

{
  "to": "candidate@example.com",
  "subject": "Thư mời phỏng vấn",
  "htmlContent": "<h1>Xin chào!</h1><p>Bạn được mời tham gia phỏng vấn...</p>"
}
```

#### Gửi email template

```http
POST /api/v1/communications-service/emails/send-template
Content-Type: application/json

{
  "to": "candidate@example.com",
  "subject": "Thư mời phỏng vấn",
  "templateName": "interview-invitation",
  "variables": {
    "candidateName": "Nguyễn Văn A",
    "jobTitle": "Java Developer",
    "interviewDate": "2024-01-15 14:00",
    "location": "Tầng 5, Tòa nhà ABC"
  }
}
```

#### Gửi email hàng loạt

```http
POST /api/v1/communications-service/emails/bulk
Content-Type: application/json

{
  "recipients": ["candidate1@example.com", "candidate2@example.com"],
  "subject": "Thông báo chung",
  "message": "Nội dung email",
  "templateName": "candidate-welcome",
  "variables": {
    "candidateName": "Ứng viên"
  }
}
```

#### Xem danh sách email đã gửi

```http
GET /api/v1/communications-service/emails?page=1&limit=10&status=SENT
```

#### Thống kê email

```http
GET /api/v1/communications-service/emails/stats
```

### 3. Templates Email có sẵn

- `candidate-welcome`: Chào mừng ứng viên mới
- `interview-invitation`: Thư mời phỏng vấn
- `application-rejected`: Thông báo từ chối ứng tuyển
- `application-accepted`: Thông báo chấp nhận ứng tuyển
- `onboarding-welcome`: Chào mừng nhân viên mới

### 4. API Test

#### Kiểm tra sức khỏe service

```http
GET /api/v1/communications-service/test/health
```

#### Gửi email test

```http
GET /api/v1/communications-service/test/email/send-test
```

#### Gửi template email test

```http
GET /api/v1/communications-service/test/email/send-template-test
```

#### Tạo lịch test

```http
POST /api/v1/communications-service/test/schedule/create-test
```

#### Xem danh sách lịch test

```http
GET /api/v1/communications-service/test/schedule/list-test
```

#### Test bộ lọc lịch

```http
GET /api/v1/communications-service/test/schedule/filter-test
```

## Cấu hình

### Database

- MySQL database: `communications_db`
- Port: 3306
- Username: root
- Password: 123456

### Email Configuration

- SMTP Host: smtp.gmail.com
- Port: 587
- Username: ${MAIL_USERNAME}
- Password: ${MAIL_PASSWORD}

### Service Port

- Communications Service: 8085

## Biến môi trường

```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_PHONE_NUMBER=+1234567890
```

## Cách sử dụng

1. Khởi động service:

```bash
cd services/communications-service
./mvnw spring-boot:run
```

2. Test API bằng Postman hoặc curl:

```bash
# Test health check
curl http://localhost:8085/api/v1/communications-service/test/health

# Test tạo lịch
curl -X POST http://localhost:8085/api/v1/communications-service/test/schedule/create-test

# Test gửi email
curl http://localhost:8085/api/v1/communications-service/test/email/send-test
```

## Lưu ý

- Đảm bảo MySQL đang chạy và database `communications_db` đã được tạo
- Cấu hình email SMTP đúng để có thể gửi email
- Service sử dụng Flyway để quản lý database migration
- Tất cả API đều hỗ trợ CORS cho phép truy cập từ frontend
