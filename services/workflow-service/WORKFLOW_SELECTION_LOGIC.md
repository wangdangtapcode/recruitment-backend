# Logic Nghiệp Vụ: Chọn Workflow Khi Tạo Yêu Cầu

## 🎯 Tổng Quan

Khi tạo một yêu cầu tuyển dụng (hoặc bất kỳ yêu cầu nào cần phê duyệt), hệ thống cần **tự động chọn workflow phù hợp** dựa trên các điều kiện.

## 📊 Kiến Trúc

```
┌─────────────────┐
│  Workflow       │  ← Template (Mẫu) của luồng phê duyệt
│  (Template)     │     Chứa các bước phê duyệt (WorkflowStep)
└────────┬────────┘
         │
         │ 1-n
         │
┌────────▼─────────────────┐
│  WorkflowConfiguration    │  ← Quy tắc chọn workflow
│  (Routing Rules)          │     Dựa trên: departmentId, positionId, 
└───────────────────────────┘     minAmount, conditions (JSON)
```

## 🔄 Luồng Xử Lý

### Bước 1: Tạo Yêu Cầu Tuyển Dụng

```java
// User tạo yêu cầu với thông tin:
{
  "title": "Tuyển Senior Developer",
  "departmentId": 1,        // IT Department
  "requesterId": 50,        // Employee ID của người tạo
  "salaryMax": 40000000,    // 40 triệu
  "positionId": 10,         // Vị trí Senior Developer
  ...
}
```

### Bước 2: Tìm WorkflowConfiguration Phù Hợp

Hệ thống sẽ tìm `WorkflowConfiguration` dựa trên:
1. **departmentId** của yêu cầu
2. **positionId** (nếu có)
3. **minAmount** (nếu có mức lương)
4. **conditions** (JSON - điều kiện phức tạp)

**Logic tìm kiếm (theo độ ưu tiên):**
1. Tìm config có `departmentId` khớp + `positionId` khớp + `minAmount` phù hợp
2. Tìm config có `departmentId` khớp + `minAmount` phù hợp
3. Tìm config có `departmentId` khớp
4. Tìm config có `departmentId = null` (áp dụng cho tất cả) + `minAmount` phù hợp
5. Tìm config có `departmentId = null` (mặc định)

### Bước 3: Lấy Workflow Từ Configuration

Sau khi tìm được `WorkflowConfiguration`, lấy `Workflow` từ đó:
```java
Workflow workflow = workflowConfiguration.getWorkflow();
```

### Bước 4: Tạo ApprovalTracking

Tạo `ApprovalTracking` với:
- `workflow` = Workflow đã chọn
- `entityType` = "RECRUITMENT_REQUEST"
- `entityId` = ID của yêu cầu tuyển dụng
- `requesterEmployeeId` = ID người tạo
- `metadata` = JSON chứa thông tin bổ sung

### Bước 5: Tạo ApprovalStep

Duyệt qua các `WorkflowStep` của workflow và tạo `ApprovalStep` tương ứng.

## 📝 Ví Dụ Cụ Thể

### Setup: Tạo Workflow và Configuration

#### 1. Tạo Workflow cho IT Department (3 bước)
```sql
INSERT INTO workflows (name, type, department_id, is_active) 
VALUES ('RECRUITMENT_APPROVAL_IT', 'RECRUITMENT', 1, true);
-- ID: 1
```

#### 2. Tạo WorkflowConfiguration cho IT - Lương thường
```sql
INSERT INTO workflow_configurations 
  (workflow_id, department_id, min_amount, is_active)
VALUES 
  (1, 1, NULL, true);
-- Áp dụng cho IT Department, không giới hạn mức lương
```

#### 3. Tạo WorkflowConfiguration cho IT - Lương cao (>50 triệu)
```sql
INSERT INTO workflow_configurations 
  (workflow_id, department_id, min_amount, is_active)
VALUES 
  (1, 1, 50000000, true);
-- Áp dụng cho IT Department, mức lương >= 50 triệu
```

#### 4. Tạo Workflow cho Phòng Ban Khác (2 bước)
```sql
INSERT INTO workflows (name, type, department_id, is_active) 
VALUES ('RECRUITMENT_APPROVAL_GENERAL', 'RECRUITMENT', NULL, true);
-- ID: 2, department_id = NULL nghĩa là áp dụng cho tất cả
```

#### 5. Tạo WorkflowConfiguration mặc định
```sql
INSERT INTO workflow_configurations 
  (workflow_id, department_id, min_amount, is_active)
VALUES 
  (2, NULL, NULL, true);
-- Áp dụng cho tất cả phòng ban, không giới hạn mức lương
```

### Scenario 1: Yêu Cầu IT - Lương 40 Triệu

**Input:**
- `departmentId` = 1 (IT)
- `salaryMax` = 40000000

**Logic tìm kiếm:**
1. Tìm config có `departmentId = 1` và `minAmount <= 40000000` → Tìm thấy config ID 1
2. Lấy `workflow_id = 1` (RECRUITMENT_APPROVAL_IT)
3. Tạo ApprovalTracking với workflow ID 1

**Kết quả:** Sử dụng workflow 3 bước (Trưởng phòng IT → HR Manager → CEO)

### Scenario 2: Yêu Cầu IT - Lương 60 Triệu

**Input:**
- `departmentId` = 1 (IT)
- `salaryMax` = 60000000

**Logic tìm kiếm:**
1. Tìm config có `departmentId = 1` và `minAmount <= 60000000` → Tìm thấy config ID 2 (minAmount = 50000000)
2. Lấy `workflow_id = 1` (RECRUITMENT_APPROVAL_IT)
3. Tạo ApprovalTracking với workflow ID 1

**Kết quả:** Vẫn sử dụng workflow 3 bước (vì cùng workflow, chỉ khác điều kiện)

### Scenario 3: Yêu Cầu Sales - Lương 30 Triệu

**Input:**
- `departmentId` = 2 (Sales)
- `salaryMax` = 30000000

**Logic tìm kiếm:**
1. Tìm config có `departmentId = 2` → Không tìm thấy
2. Tìm config có `departmentId = NULL` (mặc định) → Tìm thấy config ID 3
3. Lấy `workflow_id = 2` (RECRUITMENT_APPROVAL_GENERAL)
4. Tạo ApprovalTracking với workflow ID 2

**Kết quả:** Sử dụng workflow 2 bước (Trưởng phòng Sales → HR Manager)

## 💡 Tại Sao Cần WorkflowConfiguration?

### Lý do 1: Tách biệt Template và Routing
- **Workflow** = Template (có thể tái sử dụng)
- **WorkflowConfiguration** = Quy tắc routing (linh hoạt, dễ thay đổi)

### Lý do 2: Một Workflow có thể có nhiều Configuration
```
Workflow: RECRUITMENT_APPROVAL_IT
├── Config 1: IT Department, lương < 50 triệu
├── Config 2: IT Department, lương >= 50 triệu
└── Config 3: IT Department, vị trí Manager
```

### Lý do 3: Dễ quản lý và mở rộng
- Thay đổi điều kiện routing không cần sửa workflow
- Thêm điều kiện mới chỉ cần thêm configuration mới

## 🔍 Logic Tìm Kiếm Chi Tiết

### Thuật toán tìm WorkflowConfiguration

```java
public WorkflowConfiguration findMatchingConfiguration(
    Long departmentId, 
    Long positionId, 
    BigDecimal amount
) {
    // 1. Tìm config khớp nhất (department + position + amount)
    if (positionId != null && amount != null) {
        config = findConfig(departmentId, positionId, amount);
        if (config != null) return config;
    }
    
    // 2. Tìm config khớp (department + amount)
    if (amount != null) {
        config = findConfig(departmentId, null, amount);
        if (config != null) return config;
    }
    
    // 3. Tìm config khớp (department)
    config = findConfig(departmentId, null, null);
    if (config != null) return config;
    
    // 4. Tìm config mặc định (department = null)
    config = findConfig(null, null, amount);
    if (config != null) return config;
    
    // 5. Tìm config mặc định hoàn toàn
    config = findConfig(null, null, null);
    return config;
}
```

### Độ ưu tiên của Configuration

1. **Cụ thể nhất**: `departmentId` + `positionId` + `minAmount`
2. **Cụ thể**: `departmentId` + `minAmount`
3. **Chung**: `departmentId`
4. **Mặc định có điều kiện**: `departmentId = null` + `minAmount`
5. **Mặc định hoàn toàn**: `departmentId = null` + `minAmount = null`

## ✅ Kết Luận

**Workflow** = Template của luồng phê duyệt (các bước)
**WorkflowConfiguration** = Quy tắc chọn workflow nào được áp dụng

Khi tạo yêu cầu:
1. Tìm `WorkflowConfiguration` phù hợp dựa trên điều kiện
2. Lấy `Workflow` từ configuration đó
3. Tạo `ApprovalTracking` với workflow đã chọn
4. Tạo các `ApprovalStep` dựa trên `WorkflowStep`

