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

## Trạng thái khóa học

Model `Course` cần có trạng thái nghiệp vụ riêng để tách rõ workflow soạn nội dung và trạng thái public.

Các trạng thái chuẩn:

- `DRAFT`: khóa học đang được soạn, chưa đủ dữ liệu để public.
- `REVIEW`: khóa học đã gửi duyệt, chờ admin hoặc người có quyền kiểm tra.
- `PUBLISHED`: khóa học đã public trên trang học viên.
- `HIDDEN`: khóa học bị ẩn khỏi public nhưng vẫn còn dữ liệu vận hành.
- `ARCHIVED`: khóa học đã lưu trữ, không còn dùng trong vận hành thường ngày.

Mapping với `activeFlag`:

- `course_status = PUBLISHED`: `activeFlag = true`
- `course_status != PUBLISHED`: `activeFlag = false`

`activeFlag` chỉ nên đại diện cho việc khóa học có hiển thị public hay không. `course_status` mới là trạng thái nghiệp vụ chính.

## Quy tắc lưu nháp

Chức năng `Lưu nháp` dùng để tạo hồ sơ khóa học cơ bản trước khi bổ sung giáo trình, video và tài nguyên ở trang chi tiết.

Lý do cần lưu nháp:

- Tránh mất dữ liệu khi admin chưa nhập đủ toàn bộ nội dung khóa học.
- Cho phép tạo khóa học trước, sau đó vào trang chi tiết để bổ sung module/video.
- Tránh public khóa học chưa đủ nội dung.
- Chuẩn bị cho workflow duyệt nội dung trước khi xuất bản.

Quy tắc khi tạo nháp:

- Bắt buộc có `title` và `teacherId` vì schema hiện yêu cầu tên khóa và giảng viên.
- `price` nếu bỏ trống thì mặc định là `0`.
- `totalStudents` mặc định là `0`.
- `rating` mặc định là `5`.
- `course_status = DRAFT`.
- `activeFlag = false`.
- Sau khi tạo thành công, chuyển admin sang trang chi tiết để bổ sung nội dung.

## Quy tắc cập nhật

Admin được phép cập nhật các trường trong bảng `courses`:

- `title`
- `description`
- `highlights`
- `teacherId`
- `price`
- `tag`
- `imageUrl`
- `courseStatus`
- `activeFlag`

Nếu chọn trạng thái `PUBLISHED`, hệ thống đặt `course_status = PUBLISHED` và `activeFlag = true`.

Nếu chọn trạng thái khác `PUBLISHED`, hệ thống đặt `activeFlag = false`.

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
  - `AdminCourseCreateFormDto`
  - `AdminCourseUpdateFormDto`
  - `AdminCourseListProjection`
  - `AdminCourseDetailProjection`
