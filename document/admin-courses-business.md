# Nghiệp vụ trang quản trị khóa học

## Mục tiêu

Trang quản trị khóa học giúp admin theo dõi danh mục khóa học ở cấp tổng quan, lọc theo năm/trạng thái/từ khóa và đi vào từng khóa để cập nhật nội dung chi tiết.

## Trang danh sách khóa học

Đường dẫn đề xuất: `/admin/courses/`

Trang danh sách chỉ xử lý nghiệp vụ tổng quan:

- Xem số liệu tổng quan khóa học.
- Tìm kiếm khóa học theo tên, giảng viên, tag hoặc danh mục.
- Lọc khóa học theo năm.
- Lọc khóa học theo trạng thái.
- Xem danh sách khóa học có phân trang.
- Mở trang chi tiết của một khóa cụ thể.

Các thông tin trên mỗi dòng khóa học:

- Tên khóa học.
- Tag hoặc danh mục.
- Năm cập nhật/tạo khóa.
- Giảng viên.
- Số học viên đã ghi danh.
- Trạng thái.
- Trạng thái hiển thị public.
- Ngày cập nhật gần nhất.
- Hành động xem chi tiết.

Trang danh sách không nên chứa các thao tác phụ thuộc vào một khóa cụ thể như upload tài nguyên, chỉnh module, bật/tắt rule chi tiết, hoặc xóa khóa.

## Trang chi tiết khóa học

Đường dẫn đề xuất: `/admin/courses/{id}`

Trang chi tiết xử lý nghiệp vụ của một khóa cụ thể:

- Xem thông tin khóa học.
- Cập nhật tên, mô tả, highlights, giá, tag, ảnh bìa, giảng viên.
- Cập nhật trạng thái hiển thị.
- Xem thống kê học viên, doanh thu, số module, số video.
- Quản lý giáo trình/module.
- Upload tài nguyên bài học cho module cụ thể.
- Kiểm tra điều kiện xuất bản.
- Xem hoạt động gần đây.
- Xóa hoặc ẩn khóa học.

## Trạng thái khóa học hiện tại

Ở bước này chưa thay đổi schema database. Model `Course` hiện chỉ có `activeFlag`, nên mapping trạng thái tạm thời là:

- `activeFlag = true`: `PUBLISHED`
- `activeFlag = false`: `HIDDEN`

Các trạng thái nghiệp vụ đầy đủ hơn như `DRAFT`, `REVIEW`, `ARCHIVED` nên được bổ sung bằng một cột riêng, ví dụ `course_status`, khi cần workflow xuất bản chuẩn.

## Quy tắc cập nhật

Admin được phép cập nhật các trường hiện có trong bảng `courses`:

- `title`
- `description`
- `highlights`
- `teacherId`
- `price`
- `tag`
- `imageUrl`
- `activeFlag`

Nếu chọn trạng thái `PUBLISHED`, hệ thống đặt `activeFlag = true`.

Nếu chọn trạng thái `HIDDEN`, hệ thống đặt `activeFlag = false`.

## Quy tắc xóa

Không nên hard delete khóa học nếu đã có dữ liệu liên quan.

Quy tắc tạm thời:

- Nếu khóa chưa có enrollment và chưa có payment: cho phép xóa thật.
- Nếu khóa đã có enrollment hoặc payment: không xóa thật, chỉ chuyển `activeFlag = false` và xem như đã ẩn/lưu trữ.

## Backend cần có

- `AdminCoursesController`
  - Render danh sách khóa học.
  - Render chi tiết khóa học.
  - Nhận form cập nhật khóa học.
  - Nhận thao tác xóa/ẩn khóa học.

- `AdminCourseManagementService`
  - Build dữ liệu trang danh sách.
  - Build dữ liệu trang chi tiết.
  - Cập nhật khóa học.
  - Xóa hoặc ẩn khóa học theo quy tắc dữ liệu liên quan.

- `CourseRepository`
  - Query danh sách khóa học có filter.
  - Query chi tiết khóa học.
  - Query summary.
  - Count enrollment/payment theo khóa.

- DTO/projection
  - `AdminCourseFilterDto`
  - `AdminCourseListItemDto`
  - `AdminCourseListPageDto`
  - `AdminCourseSummaryDto`
  - `AdminCourseDetailDto`
  - `AdminCourseUpdateFormDto`
  - `AdminCourseListProjection`
  - `AdminCourseDetailProjection`
