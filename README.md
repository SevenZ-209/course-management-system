# 🎓 CourseHub — Hệ thống Quản lý Khóa học & Học tập

CourseHub là hệ thống quản lý khóa học và học tập gồm **Backend REST API** được xây dựng bằng **Spring Boot** và **Frontend SPA** bằng **React**. Hệ thống hỗ trợ nhiều vai trò người dùng, quản lý toàn bộ quy trình từ khóa học, lớp học, đăng ký, thanh toán, điểm danh đến bài tập, chấm điểm và theo dõi lộ trình học tập.

---

## 👤 Tác giả

| STT | Họ tên | Vai trò |
|:---:|---|---|
| 1 | Lê Minh Đăng Khoa | Phát triển hệ thống, Backend API, Database, Security & Frontend |

---

## 💻 Công nghệ sử dụng

- **Backend:** Java 17, Spring Boot, Spring MVC
- **Security:** Spring Security, JWT, BCrypt
- **Database & ORM:** MySQL, JPA, Hibernate
- **Frontend:** React 19, React Router, Axios, Bootstrap, React Bootstrap
- **Admin Portal:** Thymeleaf, Spring Security
- **Cloud Storage:** Cloudinary
- **Build Tools:** Maven, npm
- **Khác:** Lombok, Nimbus JOSE + JWT, Criteria API

---

## 🏗️ Kiến trúc hệ thống

| Thành phần | Mô tả | Port |
|---|---|---|
| `frontend` | React SPA dành cho Student, Teacher, Parent, Manager và Admin | `3000` |
| `backend` | Spring Boot REST API + Spring Security + JWT | `8080` |
| `admin portal` | Giao diện quản trị server-side bằng Thymeleaf | `8080/admin` |
| `database` | MySQL lưu dữ liệu nghiệp vụ | `3306` |
| `cloudinary` | Lưu avatar, hình ảnh khóa học và tài liệu bài học | Cloud |

### Kiến trúc Backend

```text
Client / React
      │
      ▼
REST Controllers
      │
      ▼
Service Layer
      │
      ▼
Repository Layer
      │
      ▼
JPA / Hibernate
      │
      ▼
    MySQL
```

Spring Security + JWT được áp dụng tại tầng API để xác thực và phân quyền truy cập theo từng vai trò.

---

## 👥 Vai trò người dùng

| Vai trò | Chức năng chính |
|---|---|
| `STUDENT` | Xem khóa học, đăng ký lớp, thanh toán, học bài, làm bài tập, xem lịch học, điểm danh và tiến độ |
| `TEACHER` | Quản lý lớp được phân công, điểm danh, giao bài, theo dõi học viên và chấm bài |
| `PARENT` | Liên kết với học viên, theo dõi tiến độ, điểm danh và kết quả bài tập |
| `MANAGER` | Quản lý khóa học, lớp, đăng ký, thanh toán, buổi học, điểm danh, tiến độ và báo cáo |
| `ADMIN` | Quản trị toàn bộ người dùng, khóa học, lớp, lộ trình, bài tập, thanh toán và dữ liệu hệ thống |

---

## 🚀 Chạy project Local

### Yêu cầu

- Java 17+
- MySQL Server
- Node.js + npm
- Maven hoặc Maven Wrapper có sẵn trong project
- Tài khoản Cloudinary
- Git

### Bước 1 — Clone repository

```bash
git clone https://github.com/SevenZ-209/course-management-system.git
cd course-management-system
```

### Bước 2 — Tạo Database

Tạo database MySQL:

```sql
CREATE DATABASE course_management_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### Bước 3 — Cấu hình Backend

Mở file:

```text
src/main/resources/application.properties
```

Cấu hình các thông tin cần thiết:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/course_management_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=<your-mysql-username>
spring.datasource.password=<your-mysql-password>

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=<your-jwt-secret-at-least-32-characters>
jwt.expiration=86400000

# Cloudinary
cloudinary.cloud-name=<your-cloud-name>
cloudinary.api-key=<your-api-key>
cloudinary.api-secret=<your-api-secret>

# CORS
cors.allowed-origins=http://localhost:3000
```

> ⚠️ **Không commit** mật khẩu Database, JWT Secret hoặc Cloudinary Secret thật lên repository public.

### Bước 4 — Chạy Spring Boot Backend

#### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

#### macOS / Linux

```bash
./mvnw spring-boot:run
```

Backend chạy tại:

```text
http://localhost:8080
```

REST API:

```text
http://localhost:8080/api/
```

Admin Portal:

```text
http://localhost:8080/admin/login
```

### Bước 5 — Chạy React Frontend

Mở terminal khác:

```bash
cd course-management-frontend
npm install
npm start
```

Frontend chạy tại:

```text
http://localhost:3000
```

> Public registration hiện chỉ cho phép tạo tài khoản `STUDENT` hoặc `PARENT`.  
> Các tài khoản `TEACHER`, `MANAGER` và `ADMIN` cần được chuẩn bị trong dữ liệu hệ thống để sử dụng các portal tương ứng.

---

## 🧪 Chạy Test

### Backend

#### Windows

```powershell
.\mvnw.cmd test
```

#### macOS / Linux

```bash
./mvnw test
```

### Frontend

```bash
cd course-management-frontend
npm test
```

---

## 🔄 Luồng Đăng ký & Thanh toán

```text
Học viên chọn khóa học
        │
        ▼
Chọn lớp còn chỗ
        │
        ▼
Tạo Enrollment
        │
        ▼
PENDING_PAYMENT
        │
        ▼
Tạo Payment Transaction
        │
        ▼
Thanh toán SUCCESS
        │
        ▼
Enrollment → ACTIVE
        │
        ▼
Tự động tạo Student Learning Path
        │
        ▼
Mở bước học đầu tiên
```

Hệ thống kiểm tra:

- Tránh đăng ký trùng khóa học.
- Kiểm tra trạng thái học viên và lớp học.
- Kiểm tra số tiền thanh toán theo học phí khóa học.
- Không cho phép một đăng ký có nhiều giao dịch `SUCCESS`.
- Kích hoạt Enrollment sau khi thanh toán thành công.
- Tự động tạo lộ trình học sau khi Enrollment được kích hoạt.

> 💡 Phần thanh toán trong project hiện mô phỏng **luồng nghiệp vụ thanh toán nội bộ**, chưa tích hợp cổng thanh toán thực tế.

---

## 🧠 Luồng Lộ trình học & Bài tập

```text
Student Learning Path
        │
        ▼
Current Learning Path Detail
        │
        ▼
Bài học / Bài tập hiện tại
        │
        ▼
Student bắt đầu làm bài
        │
        ▼
Lưu câu trả lời
        │
        ▼
Nộp bài
        │
        ├───────────────┐
        ▼               ▼
Không có Essay      Có Essay
        │               │
        ▼               ▼
Auto Grading      PENDING_GRADING
        │               │
        │               ▼
        │          Teacher chấm
        │               │
        └───────┬───────┘
                ▼
      Kiểm tra điểm tối thiểu
          + thời gian làm bài
                │
         ┌──────┴──────┐
         ▼             ▼
       FAILED         PASSED
         │             │
         ▼             ▼
   Cho phép làm lại   Hoàn thành bước
                       │
                       ▼
                 Mở bước kế tiếp
                       │
                       ▼
               Hoàn thành lộ trình
```

### Cơ chế chấm điểm

- Câu hỏi không phải tự luận được hệ thống tự động tính điểm.
- Câu hỏi `ESSAY` chuyển bài làm sang trạng thái `PENDING_GRADING`.
- Teacher, Manager hoặc Admin có quyền xử lý luồng chấm tự luận phù hợp.
- Tổng điểm gồm điểm tự động và điểm tự luận.
- Điều kiện hoàn thành một bước dựa trên:
  - Điểm tối thiểu của Learning Path Detail.
  - Thời gian làm bài.
  - Số lần làm bài tối đa.
- Khi học viên đạt yêu cầu, bước tiếp theo trong lộ trình được tự động mở.

---

## 🔐 Transaction & Concurrency Control

Một số luồng nghiệp vụ quan trọng được xử lý trong **Spring Transaction** kết hợp với cơ chế khóa dữ liệu để hạn chế lỗi khi có nhiều request đồng thời.

Các trường hợp tiêu biểu:

- Đăng ký khóa học/lớp học.
- Xử lý giao dịch thanh toán.
- Ngăn nhiều giao dịch thành công cho cùng một Enrollment.
- Cập nhật tiến độ Learning Path.
- Xử lý Assignment Attempt và chấm điểm.
- Kiểm tra xung đột lịch học.
- Cập nhật dữ liệu điểm danh.

Project sử dụng **Pessimistic Locking** tại các luồng cần bảo vệ dữ liệu khỏi race condition và duplicate operation.

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/api/auth/login` | Đăng nhập và nhận JWT |
| `POST` | `/api/auth/register` | Đăng ký tài khoản Student hoặc Parent |

### Public Courses

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/categories` | Danh sách danh mục |
| `GET` | `/api/courses` | Danh sách khóa học |
| `GET` | `/api/courses/{courseId}` | Chi tiết khóa học |
| `GET` | `/api/courses/{courseId}/classes` | Danh sách lớp của khóa học |

### Student

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/student/dashboard` | Dashboard học viên |
| `GET` | `/api/student/courses` | Khóa học đã đăng ký |
| `POST` | `/api/student/enrollments` | Đăng ký lớp học |
| `GET` | `/api/student/schedule` | Xem lịch học |
| `GET` | `/api/student/lessons/{lessonId}` | Xem chi tiết bài học |
| `GET` | `/api/student/assignments` | Danh sách bài tập |
| `GET` | `/api/student/assignments/current` | Bài tập hiện tại |
| `POST` | `/api/student/assignments/{assignedAssignmentId}/start` | Bắt đầu làm bài |
| `PUT` | `/api/student/assignments/attempts/{attemptId}/answers` | Lưu câu trả lời |
| `POST` | `/api/student/assignments/attempts/{attemptId}/submit` | Nộp bài |
| `GET` | `/api/student/assignments/attempts/{attemptId}/result` | Xem kết quả |
| `POST` | `/api/payment-transactions` | Tạo giao dịch thanh toán |
| `GET` | `/api/payment-transactions/me` | Lịch sử thanh toán |

### Teacher

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/teacher/classes` | Danh sách lớp được phân công |
| `GET` | `/api/teacher/classes/{classId}` | Chi tiết lớp |
| `GET` | `/api/teacher/classes/{classId}/students` | Danh sách học viên |
| `GET` | `/api/teacher/classes/{classId}/progress` | Tiến độ học viên |
| `GET` | `/api/teacher/classes/{classId}/assignments` | Bài tập của lớp |
| `GET` | `/api/teacher/classes/{classId}/sessions/{sessionId}/attendance` | Danh sách điểm danh |
| `PUT` | `/api/teacher/classes/{classId}/sessions/{sessionId}/attendance` | Cập nhật điểm danh |
| `GET` | `/api/teacher/grading/pending` | Bài đang chờ chấm |
| `GET` | `/api/teacher/grading/{attemptId}` | Chi tiết bài cần chấm |
| `POST` | `/api/teacher/grading/answers/{studentAnswerId}` | Chấm câu tự luận |
| `POST` | `/api/teacher/grading/{attemptId}/finalize` | Hoàn tất chấm bài |

### Parent

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/api/parent/links` | Liên kết với học viên |
| `GET` | `/api/parent/students` | Danh sách học viên được liên kết |
| `GET` | `/api/parent/students/{studentId}/dashboard` | Dashboard học viên |
| `GET` | `/api/parent/students/{studentId}/progress` | Xem tiến độ học tập |
| `GET` | `/api/parent/students/{studentId}/attendance` | Xem điểm danh |
| `GET` | `/api/parent/students/{studentId}/assignments` | Xem bài tập |

### Manager

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/manager/dashboard` | Dashboard quản lý |
| `GET` / `POST` | `/api/manager/courses` | Quản lý khóa học |
| `GET` / `POST` | `/api/manager/classes` | Quản lý lớp học |
| `GET` | `/api/manager/enrollments` | Quản lý đăng ký |
| `GET` | `/api/manager/payment-transactions` | Theo dõi thanh toán |
| `GET` | `/api/manager/progress` | Theo dõi tiến độ học viên |
| `GET` | `/api/manager/attendances` | Quản lý điểm danh |
| `GET` | `/api/manager/reports` | Báo cáo hệ thống |

### Admin

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/admin/users` | Quản lý người dùng |
| `GET` / `POST` | `/api/admin/categories` | Quản lý danh mục |
| `GET` / `POST` | `/api/admin/courses` | Quản lý khóa học |
| `GET` / `POST` | `/api/admin/course-modules` | Quản lý module |
| `GET` / `POST` | `/api/admin/lessons` | Quản lý bài học |
| `GET` / `POST` | `/api/admin/classes` | Quản lý lớp học |
| `GET` / `POST` | `/api/admin/online-sessions` | Quản lý buổi học |
| `GET` / `POST` | `/api/admin/assignments` | Quản lý bài tập |
| `GET` / `POST` | `/api/admin/questions` | Quản lý câu hỏi |
| `GET` / `POST` | `/api/admin/answers` | Quản lý đáp án |
| `GET` / `POST` | `/api/admin/learning-paths` | Quản lý lộ trình |
| `GET` | `/api/admin/student-learning-paths` | Theo dõi lộ trình học viên |
| `GET` | `/api/admin/payment-transactions` | Quản lý thanh toán |
| `GET` | `/api/admin/reports` | Báo cáo hệ thống |

---

## 📁 Cấu trúc dự án

```text
course-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/lmdk/course_management_system/
│   │   │   ├── configs/              # Spring Security, CORS, Cloudinary
│   │   │   ├── controllers/          # MVC Controllers
│   │   │   │   └── api/              # REST API theo từng role
│   │   │   ├── dto/                  # Request / Response DTO
│   │   │   ├── exceptions/           # Custom Exceptions
│   │   │   ├── filters/              # JWT Authentication Filter
│   │   │   ├── helpers/              # Helper xử lý nghiệp vụ
│   │   │   ├── mappers/              # Mapping Entity ↔ DTO
│   │   │   ├── pojo/                 # JPA Entities
│   │   │   ├── repository/           # Repository Layer
│   │   │   │   └── impl/             # Custom Repository Implementations
│   │   │   ├── services/             # Service Interfaces
│   │   │   │   └── impl/             # Business Logic
│   │   │   └── utils/                # JWT Utilities
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── templates/             # Thymeleaf Admin Portal
│   │       └── static/
│   └── test/                          # Backend Tests
│
├── course-management-frontend/
│   ├── public/
│   └── src/
│       ├── components/                # UI Components
│       ├── configs/                   # API & Context Config
│       ├── hooks/                     # Custom Hooks
│       ├── layouts/                   # Admin / Manager Layout
│       ├── reducers/                  # State Reducer
│       └── screens/
│           ├── Auth/
│           ├── Home/
│           ├── Student/
│           ├── Teacher/
│           ├── Parent/
│           ├── Manager/
│           └── Admin/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---


## ⚙️ Tính năng nổi bật

- 🔐 **JWT Authentication & RBAC** — Xác thực bằng JWT và phân quyền cho 5 vai trò người dùng
- 🛡️ **Ownership-based Authorization** — Bảo vệ dữ liệu theo quyền sở hữu và phạm vi người dùng
- 🎓 **Course & Class Management** — Quản lý danh mục, khóa học, module, bài học và lớp học
- 💳 **Enrollment & Payment Workflow** — Đăng ký lớp, thanh toán và tự động kích hoạt Enrollment
- 🧭 **Learning Path** — Quản lý lộ trình học theo từng bước và tự động mở nội dung tiếp theo
- 📝 **Assessment Workflow** — Làm bài có giới hạn thời gian, số lần làm và điều kiện đạt
- 🤖 **Automatic Grading** — Tự động chấm các câu hỏi không phải tự luận
- 👨‍🏫 **Manual Essay Grading** — Giáo viên chấm câu tự luận và hoàn tất kết quả
- 🔄 **Transaction Management** — Bảo vệ các nghiệp vụ quan trọng bằng Spring Transaction
- 🔒 **Pessimistic Locking** — Hạn chế race condition và duplicate operation khi xử lý đồng thời
- 🔍 **Dynamic Filtering** — Tìm kiếm/lọc động bằng Criteria API
- 📅 **Attendance & Schedule** — Quản lý buổi học, lịch học và điểm danh
- 👨‍👩‍👧 **Parent Monitoring** — Phụ huynh liên kết và theo dõi quá trình học của học viên
- 📊 **Dashboard & Reports** — Theo dõi tiến độ, thanh toán, vận hành và doanh thu
- ☁️ **Cloudinary Storage** — Lưu avatar, hình ảnh khóa học và tài liệu bài học trên Cloudinary
- 🎨 **Multi-role Frontend** — Giao diện React riêng cho Student, Teacher, Parent, Manager và Admin

---

## 📚 Tài liệu tham khảo

- Repository: https://github.com/SevenZ-209/course-management-system
- REST API Base URL: `http://localhost:8080/api/`
- Admin Portal: `http://localhost:8080/admin/login`
- Mã nguồn Backend: thư mục `src/main/java/`
- Mã nguồn Frontend: thư mục `course-management-frontend/`

---

## 📝 Ghi chú

Project được xây dựng phục vụ mục đích học tập và portfolio. Một số phần như thanh toán đang mô phỏng luồng nghiệp vụ và chưa tích hợp cổng thanh toán thực tế.

Trước khi triển khai production cần:

- Di chuyển toàn bộ Database credentials, JWT secret và Cloudinary credentials sang biến môi trường hoặc Secret Manager.
- Bật HTTPS.
- Tách cấu hình Development / Production.
- Giới hạn CORS theo domain thực tế.
- Bổ sung cơ chế refresh/revoke JWT nếu cần.
- Tích hợp payment gateway thực tế cho chức năng thanh toán.
