# BKIS Mock Function Design

## Mục tiêu

Tài liệu này thống kê và thiết kế chức năng dựa trên các file giao diện trong `src/main/resources/mock`.
Phạm vi gồm 2 khu vực:

- Giao diện web/public cho học viên và khách truy cập
- Giao diện admin cho vận hành nội bộ

Tài liệu này dùng để:

- Chốt backlog phát triển từ mock
- Đối chiếu giữa mock và implementation hiện tại
- Làm nền cho việc tách module, controller, service và permission sau này

## 1. Tổng quan hiện trạng

### 1.1 Các màn hình web trong mock

| Mock file | Chức năng | Trạng thái hiện tại |
|---|---|---|
| `mock/index.html` | Trang chủ landing page | Đã có bản triển khai tương ứng qua `03-home.html` + `HomeController` |
| `mock/login.html` | Đăng nhập | Đã có bản triển khai tương ứng qua `01-login.html` + `AuthController` |
| `mock/courses.html` | Danh sách khóa học | Chưa có route/template tương ứng hoàn chỉnh |
| `mock/courses_detail.html` | Chi tiết khóa học | Đã có bản triển khai một phần qua `04-course-detail.html` + `CourseController` |
| `mock/course_signup.html` | Form đăng ký khóa học | Chưa có route/controller |
| `mock/course_checkout.html` | Thanh toán khóa học | Chưa có route/controller |
| `mock/course_learn.html` | Dashboard học tập của học viên | Chưa có route/controller |
| `mock/about.html` | Giới thiệu nền tảng | Chưa có route/controller |
| `mock/contact.html` | Liên hệ/tư vấn | Chưa có route/controller |
| `mock/team.html` | Danh sách đội ngũ/giảng viên | Chưa có route/controller |
| `mock/testimonial.html` | Review/cảm nhận học viên | Chưa có route/controller |
| `mock/forgot-password.html` | Quên mật khẩu | Chưa có route/controller |
| `mock/forgot-password-verify.html` | Xác minh quên mật khẩu | Chưa có route/controller |
| `mock/404.html` | Trang lỗi 404 | Chưa cấu hình error page rõ ràng |

### 1.2 Các màn hình admin trong mock

| Mock file | Chức năng | Trạng thái hiện tại |
|---|---|---|
| `mock/admin/dashboard.html` | Dashboard tổng quan | Đã có bản triển khai một phần qua `ad-01-dashboard.html` + `DashboardController` |
| `mock/admin/accounts.html` | Quản lý tài khoản và phân quyền | Đã có route/view khung qua `ad-02-accounts.html` |
| `mock/admin/students.html` | Quản lý học viên | Đã có route/view khung qua `ad-03-students.html` |
| `mock/admin/courses.html` | Quản lý khóa học | Chưa có controller/view runtime tương ứng |
| `mock/admin/payments.html` | Quản lý thanh toán | Chưa có controller/view runtime tương ứng |
| `mock/admin/settings.html` | Cài đặt hệ thống | Chưa có controller/view runtime tương ứng |

## 2. Thiết kế chức năng giao diện web

### 2.1 Trang chủ

- Mock: `mock/index.html`
- Route đề xuất: `GET /`
- View runtime hiện có: `03-home.html`
- Mục tiêu:
  - Giới thiệu nền tảng
  - Hiển thị khóa học nổi bật
  - Điều hướng vào danh sách khóa học và hành trình đăng ký
- Thành phần chức năng:
  - Banner/carousel marketing
  - Danh mục khóa học
  - Danh sách course nổi bật
  - Khối about, team, testimonial
  - CTA đăng ký ngay
- Dữ liệu cần:
  - Danh sách course active
  - Top course theo enrollment
  - Course theo category/tag
  - Số liệu marketing tĩnh hoặc CMS
- Backend hiện có:
  - `HomeController`
  - `HomeService`
  - `CourseRepository`

### 2.2 Đăng nhập

- Mock: `mock/login.html`
- Route đề xuất: `GET /login`, `POST /login`
- View runtime hiện có: `01-login.html`
- Mục tiêu:
  - Xác thực người dùng
  - Hỗ trợ nhớ tài khoản, captcha, quên mật khẩu
- Thành phần chức năng:
  - Form username/email + password
  - Remember me
  - Captcha
  - Link quên mật khẩu
- Backend hiện có:
  - `SecurityConfig`
  - `CustomUserDetailsService`
  - `AuthSuccessHandler`
  - `AuthFailureHandler`
- Ghi chú:
  - Mock dùng captcha text ở client
  - Code hiện có `CaptchaService` nhưng filter captcha đang bị comment out

### 2.3 Danh sách khóa học

- Mock: `mock/courses.html`
- Route đề xuất: `GET /courses`
- Trạng thái: chưa implement
- Mục tiêu:
  - Liệt kê toàn bộ khóa học public
  - Cho phép lọc theo category/tag
  - Điều hướng sang course detail hoặc đăng ký
- Thành phần chức năng:
  - Header + breadcrumb
  - Category cards
  - Grid danh sách khóa học
  - CTA `Read More` và `Join Now`
- Dữ liệu cần:
  - Danh sách course active
  - Category/tag
  - Instructor name
  - Price, rating, total student, duration
- Thiết kế backend đề xuất:
  - `CourseCatalogController`
  - `CourseCatalogService`
  - Query phân trang/filter theo `tag`, `keyword`, `sort`

### 2.4 Chi tiết khóa học

- Mock: `mock/courses_detail.html`
- Route hiện có: `GET /courses/{id}`
- View runtime hiện có: `04-course-detail.html`
- Mục tiêu:
  - Cung cấp đầy đủ thông tin để chuyển đổi đăng ký
  - Trình bày syllabus, preview video, review, instructor
- Thành phần chức năng:
  - Hero/course preview
  - Tổng quan khóa học
  - Learning outcomes
  - Curriculum accordion
  - Preview video
  - Review summary
  - Sidebar pricing + instructor
- Backend hiện có:
  - `CourseController`
  - `CourseDetailService`
  - `CourseRepository`, `LessonRepository`, `LessonVideoRepository`, `CourseReviewRepository`
- Nhận xét:
  - Đây là màn hình web được làm sâu nhất ở thời điểm hiện tại

### 2.5 Đăng ký khóa học

- Mock: `mock/course_signup.html`
- Route đề xuất:
  - `GET /courses/{id}/signup`
  - `POST /courses/{id}/signup`
- Trạng thái: chưa implement
- Mục tiêu:
  - Thu lead hoặc tạo hồ sơ đăng ký trước khi thanh toán
- Thành phần chức năng:
  - Form thông tin cá nhân
  - Chọn khóa học
  - Chọn hình thức học
  - Nhập coupon
  - Chấp nhận điều khoản
- Dữ liệu/domain liên quan:
  - `User`
  - `Course`
  - Có thể cần `Lead`, `RegistrationDraft` hoặc `EnrollmentDraft`
- Thiết kế backend đề xuất:
  - `CourseRegistrationController`
  - `CourseRegistrationService`
  - Validate form, lưu draft và chuyển sang checkout

### 2.6 Checkout và thanh toán

- Mock: `mock/course_checkout.html`
- Route đề xuất:
  - `GET /checkout/{registrationId}`
  - `POST /checkout/{registrationId}/pay`
- Trạng thái: chưa implement
- Mục tiêu:
  - Hoàn tất thanh toán và kích hoạt enrollment
- Thành phần chức năng:
  - Chọn phương thức thanh toán
  - Nhập coupon
  - Order summary
  - Kích hoạt khóa học sau khi thanh toán thành công
- Dữ liệu/domain liên quan:
  - `Payment`
  - `Enrollment`
  - `Course`
  - `User`
- Thiết kế backend đề xuất:
  - `CheckoutController`
  - `PaymentService`
  - `EnrollmentService`
  - `PaymentGatewayAdapter`
- Ghi chú:
  - Hiện DB đã có `payments` và `enrollments`, nhưng mới dừng ở mức dashboard/reporting

### 2.7 Học viên học khóa đã mua

- Mock: `mock/course_learn.html`
- Route đề xuất: `GET /my-learning/{courseId}` hoặc `GET /learn/{enrollmentId}`
- Trạng thái: chưa implement
- Mục tiêu:
  - Cung cấp learning dashboard cho học viên sau khi mua khóa
- Thành phần chức năng:
  - Progress tổng
  - Danh sách module/lesson/video
  - Trạng thái locked/unlocked
  - Resource download
  - Modal xem video
  - Theo dõi bài tập/live session
- Dữ liệu/domain liên quan:
  - `Enrollment`
  - `Lesson`
  - `LessonVideo`
  - `Progress`
- Thiết kế backend đề xuất:
  - `LearningController`
  - `LearningService`
  - `ProgressService`
- Nhận xét:
  - Mock này phù hợp để mở rộng từ `CourseDetailService`, vì logic lesson/video đã có nền

### 2.8 Các trang nội dung marketing

- Mock:
  - `mock/about.html`
  - `mock/contact.html`
  - `mock/team.html`
  - `mock/testimonial.html`
- Route đề xuất:
  - `GET /about`
  - `GET /contact`
  - `GET /team`
  - `GET /testimonials`
- Mục tiêu:
  - Hoàn thiện website marketing
  - Tăng chuyển đổi và độ tin cậy
- Dữ liệu cần:
  - Nội dung CMS hoặc dữ liệu cấu hình
  - Team profile
  - Testimonial
  - Contact form submissions
- Thiết kế backend đề xuất:
  - Bắt đầu bằng static page controller
  - Về sau tách `ContactMessage`, `TeacherProfile`, `Testimonial`

### 2.9 Quên mật khẩu

- Mock:
  - `mock/forgot-password.html`
  - `mock/forgot-password-verify.html`
- Route đề xuất:
  - `GET /forgot-password`
  - `POST /forgot-password`
  - `GET /forgot-password/verify`
  - `POST /forgot-password/reset`
- Trạng thái: chưa implement
- Mục tiêu:
  - Khôi phục tài khoản an toàn
- Thiết kế backend đề xuất:
  - `PasswordResetController`
  - `PasswordResetService`
  - Bảng `password_reset_tokens`

### 2.10 Upload file

- Runtime hiện có:
  - `GET /upload/`
  - `POST /upload/init`
  - `POST /upload/chunk`
  - `POST /upload/complete`
- Mục tiêu:
  - Upload file lớn theo chunk
- Ghi chú:
  - Tên controller/service hiện ghi là S3 nhưng implementation đang lưu local
  - Chức năng này hiện đứng độc lập, chưa gắn trực tiếp vào màn `course_learn` hay `admin/courses`

## 3. Thiết kế chức năng giao diện admin

### 3.1 Dashboard admin

- Mock: `mock/admin/dashboard.html`
- Route runtime hiện có: `GET /admin/dashboard/`
- View runtime hiện có: `ad-01-dashboard.html`
- Mục tiêu:
  - Tổng quan hệ thống cho admin
  - Theo dõi tăng trưởng user, course, revenue
  - Hiển thị học viên mới nhất
- Thành phần chức năng:
  - KPI cards
  - Recent students/enrollments
  - Pagination
- Backend hiện có:
  - `DashboardController`
  - `DashboardService`
  - `UserRepository`, `CourseRepository`, `PaymentsRepository`, `EnrollmentRepository`

### 3.2 Quản lý tài khoản

- Mock: `mock/admin/accounts.html`
- Route runtime hiện có: `GET /admin/accounts/`
- View runtime hiện có: `ad-02-accounts.html`
- Mục tiêu:
  - Quản lý user nội bộ và người học
  - Phân role cho admin, instructor, mentor, learner
  - Tạo nhanh tài khoản
- Thành phần chức năng:
  - Stats account
  - Form tạo tài khoản
  - Danh sách hồ sơ user
  - Role catalog / permission matrix
- Dữ liệu/domain liên quan:
  - `User`
  - `UserRole`
- Thiết kế backend đề xuất:
  - `AdminAccountController`
  - `AdminUserService`
  - CRUD user, activate/deactivate, reset password, assign role
- Ghi chú:
  - Code hiện tại mới chỉ render trang, chưa có chức năng CRUD thật

### 3.3 Quản lý học viên

- Mock: `mock/admin/students.html`
- Route runtime hiện có: `GET /admin/students/`
- View runtime hiện có: `ad-03-students.html`
- Mục tiêu:
  - Theo dõi tiến độ học tập
  - Lọc học viên theo cohort, mentor, progress, trạng thái
  - Hành động nhắc học, chuyển lớp, kích hoạt lại
- Thành phần chức năng:
  - KPI học viên
  - Filter toolbar nâng cao
  - Search + cohort + mentor + progress
  - Bảng học viên với progress bar
  - Quick action / modal hồ sơ nhanh
- Dữ liệu/domain liên quan:
  - `User`
  - `Enrollment`
  - `Progress`
  - Có thể cần `MentorAssignment`, `Cohort`, `SupportTicket`
- Backend hiện có:
  - Chỉ có `StudentsController`
  - `StudentService` hiện vẫn là mock in-memory, không đủ cho admin thật
- Thiết kế backend đề xuất:
  - `AdminStudentController`
  - `AdminStudentService`
  - DTO cho student list, student detail, progress summary

### 3.4 Quản lý khóa học

- Mock: `mock/admin/courses.html`
- Route đề xuất: `GET /admin/courses/`
- Trạng thái: chưa có controller runtime
- Mục tiêu:
  - Quản lý vòng đời khóa học từ draft đến published
  - Soạn giáo trình/module
  - Upload tài nguyên
  - Bật/tắt hiển thị khóa học
- Thành phần chức năng:
  - Course status board: Draft, Review, Published
  - Reorder curriculum/module
  - Upload resource
  - Danh sách khóa học + visibility toggle
- Dữ liệu/domain liên quan:
  - `Course`
  - `Lesson`
  - `LessonVideo`
  - `UploadSession`
- Thiết kế backend đề xuất:
  - `AdminCourseController`
  - `AdminCourseService`
  - `CoursePublishWorkflowService`
  - Tái sử dụng `UploadService`

### 3.5 Quản lý thanh toán

- Mock: `mock/admin/payments.html`
- Route đề xuất: `GET /admin/payments/`
- Trạng thái: chưa có controller runtime
- Mục tiêu:
  - Theo dõi giao dịch, đối soát, hoàn tiền, payout giảng viên
- Thành phần chức năng:
  - Revenue cards
  - Bộ lọc giao dịch
  - Transaction table
  - Reconciliation action
  - Payout schedule cho instructor
- Dữ liệu/domain liên quan:
  - `Payment`
  - `Enrollment`
  - `User`
  - Có thể cần thêm `Invoice`, `Refund`, `InstructorPayout`
- Backend hiện có:
  - Chưa có module vận hành payment thật
  - Chỉ có repo phục vụ dashboard tổng hợp
- Thiết kế backend đề xuất:
  - `AdminPaymentController`
  - `AdminPaymentService`
  - `ReconciliationService`
  - `RefundService`

### 3.6 Cài đặt hệ thống

- Mock: `mock/admin/settings.html`
- Route đề xuất: `GET /admin/settings/`
- Trạng thái: chưa có controller runtime
- Mục tiêu:
  - Quản trị cấu hình hệ thống ở mức vận hành
- Thành phần chức năng:
  - Branding
  - Security policy
  - Email templates
  - External integrations/webhooks
  - Backup/incident alerts
- Dữ liệu/domain liên quan:
  - Chưa có entity tương ứng
  - Nên thêm `SystemSetting`, `EmailTemplate`, `WebhookConfig`, `AuditLog`
- Thiết kế backend đề xuất:
  - `AdminSettingsController`
  - `SystemSettingService`
  - `NotificationTemplateService`
  - `IntegrationConfigService`

## 4. Mapping mock với code hiện tại

### 4.1 Các route đang có thật

| Route | Controller | Ghi chú |
|---|---|---|
| `/` | `HomeController` | Trang chủ |
| `/login` | `AuthController` + Spring Security | Đăng nhập |
| `/courses/{id}` | `CourseController` | Chi tiết khóa học |
| `/students` | `StudentController` | Demo danh sách student |
| `/upload/` | `UploadFileS3Controller` | Màn upload file |
| `/upload/init` | `UploadFileS3RestController` | Init chunk upload |
| `/upload/chunk` | `UploadFileS3RestController` | Upload chunk |
| `/upload/complete` | `UploadFileS3RestController` | Merge file |
| `/admin/dashboard/` | `DashboardController` | Dashboard admin |
| `/admin/accounts/` | `AccountsController` | Khung accounts |
| `/admin/students/` | `StudentsController` | Khung students |

### 4.2 Các route nên bổ sung từ mock

#### Web

- `GET /courses`
- `GET /courses/{id}/signup`
- `POST /courses/{id}/signup`
- `GET /checkout/{registrationId}`
- `POST /checkout/{registrationId}/pay`
- `GET /learn/{enrollmentId}`
- `GET /about`
- `GET /contact`
- `POST /contact`
- `GET /team`
- `GET /testimonials`
- `GET /forgot-password`
- `POST /forgot-password`
- `GET /forgot-password/verify`
- `POST /forgot-password/reset`

#### Admin

- `GET /admin/courses/`
- `GET /admin/payments/`
- `GET /admin/settings/`
- `POST /admin/accounts/`
- `PUT /admin/accounts/{id}`
- `GET /admin/students/{id}`
- `POST /admin/students/{id}/remind`
- `POST /admin/students/{id}/transfer`
- `POST /admin/courses/{id}/publish`
- `POST /admin/courses/{id}/hide`
- `POST /admin/payments/{id}/reconcile`
- `POST /admin/payments/{id}/refund`

## 5. Đề xuất module backend theo mock

### 5.1 Web module

- `auth`
  - login
  - forgot password
- `catalog`
  - home
  - courses list
  - course detail
- `registration`
  - signup
  - checkout
  - coupon
- `learning`
  - my course
  - progress
  - lesson playback
- `content`
  - about
  - team
  - testimonial
  - contact

### 5.2 Admin module

- `admin-dashboard`
- `admin-users`
- `admin-students`
- `admin-courses`
- `admin-payments`
- `admin-settings`

## 6. Ưu tiên phát triển đề xuất

### Phase 1

- Hoàn thiện public flow end-to-end:
  - `/`
  - `/courses`
  - `/courses/{id}`
  - `/courses/{id}/signup`
  - `/checkout/{registrationId}`
- Hoàn thiện admin menu:
  - `/admin/dashboard/`
  - `/admin/accounts/`
  - `/admin/students/`
  - `/admin/courses/`
  - `/admin/payments/`
  - `/admin/settings/`

### Phase 2

- Học viên học khóa đã mua
- Theo dõi progress thật từ bảng `progress`
- CRUD course/admin thật thay vì mock page
- Gắn upload file vào admin course content

### Phase 3

- Forgot password
- Contact flow
- Email template
- Payment integration thật
- Refund/reconciliation/payout

## 7. Nhận xét kiến trúc

- Mock hiện mô tả một hệ thống LMS tương đối đầy đủ, không còn là website giới thiệu đơn thuần.
- Code runtime hiện mới cover tốt phần:
  - home
  - login
  - course detail
  - admin dashboard basic
  - upload file chunked
- Khoảng trống lớn nhất là:
  - course catalog
  - checkout flow
  - learning dashboard
  - admin course/payment/settings
- Nếu đi tiếp theo mock, nên xem hệ thống theo 3 domain rõ ràng:
  - acquisition: marketing, course catalog, signup
  - delivery: enrollment, lesson, progress, learning
  - operations: admin, payment, settings, reporting

## 8. File/code tham chiếu chính

- `src/main/resources/mock`
- `src/main/resources/templates`
- `src/main/java/vn/edu/bkis/controller`
- `src/main/java/vn/edu/bkis/controller/admin`
- `src/main/java/vn/edu/bkis/service`
- `src/main/java/vn/edu/bkis/service/admin`
- `src/main/java/vn/edu/bkis/model`
- `src/main/java/vn/edu/bkis/repository`

