# Coding Conventions

## Mục tiêu
Tài liệu này chuẩn hóa cách viết mã trong repo `bkis` để:
- giảm sự khác nhau giữa các file
- giúp review nhanh hơn
- giúp Codex áp dụng cùng một chuẩn mỗi lần mở repo

## Nguyên tắc chung
- Ưu tiên code dễ đọc hơn code quá ngắn.
- Mỗi class nên có một trách nhiệm chính.
- Chỉ refactor rộng khi nhiệm vụ thực sự yêu cầu.
- Giữ thay đổi nhỏ, tập trung, và nhất quán với vùng code xung quanh.
- Không thêm abstraction mới nếu chưa có nhu cầu rõ ràng.

## Quy ước đặt tên

### Class
- Dùng `PascalCase`.
- Tên class phải phản ánh vai trò rõ ràng.
- Ví dụ:
  - `StudentController`
  - `AdminStudentCommandService`
  - `AccountManagementPageDto`

### Method
- Dùng `camelCase`.
- Tên method nên bắt đầu bằng động từ.
- Tên phải mô tả hành vi, không mô tả chi tiết kỹ thuật thừa.
- Ví dụ:
  - `findStudents`
  - `createStudent`
  - `loadDashboardInfo`

### Variable
- Dùng `camelCase`.
- Tránh tên quá ngắn trừ biến vòng lặp đơn giản.
- Tên biến phải diễn đạt ý nghĩa nghiệp vụ.
- Tránh các tên mơ hồ như:
  - `data`
  - `obj`
  - `temp`
  - `value`

### Constant
- Dùng `UPPER_SNAKE_CASE`.
- Tên constant phải diễn đạt ý nghĩa, không chỉ kiểu dữ liệu.

### Package
- Dùng chữ thường.
- Tổ chức package theo vai trò hoặc domain hiện có của repo.
- Không tạo package mới nếu chỉ để chứa một class chưa có lý do rõ ràng.

## Quy ước cho từng layer

### Controller
- Controller chỉ nên:
  - nhận request
  - validate input mức cơ bản
  - gọi service
  - trả về view hoặc response
- Không nhồi business logic dài vào controller.
- Không viết query database trực tiếp trong controller.

### Service
- Business logic nên nằm ở service.
- Method service nên rõ input, rõ output.
- Nếu method quá dài, tách thành private method có tên rõ nghĩa.
- Tách riêng command logic và query logic khi feature đủ phức tạp.

### Repository
- Tên method repository nên phản ánh tiêu chí truy vấn.
- Không đặt tên quá chung chung như `getData`.
- Nếu query phức tạp, cần đảm bảo tên method hoặc annotation đủ rõ mục đích.

### DTO
- DTO chỉ nên mang dữ liệu.
- Không nhét business logic vào DTO.
- Tên DTO cần cho biết nó dùng ở đâu:
  - request
  - response
  - summary
  - page
  - form

### Entity
- Entity nên phản ánh mô hình dữ liệu.
- Không đưa logic giao diện vào entity.
- Khi đổi field entity phải kiểm tra:
  - repository
  - service mapping
  - DTO
  - template
  - SQL/data seed liên quan

## Quy ước format code
- Giữ method ngắn khi có thể.
- Tránh lồng `if/else` quá sâu.
- Ưu tiên early return nếu làm code rõ hơn.
- Gom các đoạn logic liên quan gần nhau.
- Không để block code chết hoặc comment-out code lâu dài.
- Import phải gọn, không giữ import thừa.

## Quy ước comment mã nguồn

### Comment bắt buộc cho function
- Tất cả function hoặc method được thêm mới hay chỉnh sửa đều phải có comment ngắn gọn bằng tiếng Việt.
- Comment đặt ngay phía trên function.
- Comment phải đủ để người đọc hiểu nhanh mục đích chính của hàm.
- Nếu cần, comment nên nêu ngắn gọn:
  - hàm dùng để làm gì
  - đầu vào hoặc ngữ cảnh xử lý chính
  - kết quả trả về hoặc tác động chính

### Comment cho hàm có nhiều logic
- Với các hàm có nhiều bước xử lý, cần comment ngắn cho từng block logic chính.
- Quy tắc này đặc biệt quan trọng với các hàm trong `service`.
- Comment theo từng nhóm bước xử lý, không comment từng dòng nhỏ lẻ.
- Ưu tiên comment các bước như:
  - kiểm tra điều kiện đầu vào
  - tải dữ liệu liên quan
  - xử lý hoặc biến đổi dữ liệu
  - kiểm tra ràng buộc nghiệp vụ
  - lưu dữ liệu hoặc trả kết quả
- Nếu một hàm cần quá nhiều comment mới dễ hiểu, ưu tiên tách nhỏ hàm trước.

### Khi nào nên comment
- Comment khi đoạn code có ý định không hiển nhiên.
- Comment khi có ràng buộc nghiệp vụ khó đoán từ code.
- Comment khi có workaround, edge case, hoặc lý do kỹ thuật quan trọng.
- Comment khi cần cảnh báo người sửa sau về hậu quả của thay đổi.

### Khi nào không nên comment
- Không comment những điều code đã nói rất rõ.
- Không viết comment kiểu mô tả từng dòng đơn giản.
- Không dùng comment để che giấu code khó hiểu; hãy ưu tiên đổi tên hoặc tách hàm trước.

### Chuẩn comment
- Comment ngắn, trực tiếp, nói về `why` nhiều hơn `what`.
- Nếu comment là cảnh báo hoặc ràng buộc, phải viết thật cụ thể.
- Với repo này, comment trong source code ưu tiên viết bằng tiếng Việt.
- Tên class, method, biến vẫn giữ theo chuẩn tiếng Anh hiện có.
- Với hàm nhiều bước, comment nên bám theo các block xử lý chính thay vì diễn giải lại từng câu lệnh.

### Ví dụ comment tốt
```java
// Tạo danh sách học viên cho màn hình quản trị theo điều kiện tìm kiếm hiện tại.
public AdminStudentListPageDto findStudents(AdminStudentFilterDto filter) {
```

```java
// Xử lý tạo học viên mới và trả về dữ liệu kết quả cho màn hình quản trị.
public AdminStudentCreateResponseDto createStudent(AdminStudentCreateRequest request) {
    // Kiểm tra dữ liệu đầu vào và các điều kiện bắt buộc.

    // Tải dữ liệu liên quan phục vụ việc tạo học viên.

    // Lưu học viên mới và chuẩn bị dữ liệu trả về.
}
```

```java
// Giữ nguyên tên enum vì MySQL đang lưu trực tiếp giá trị chuỗi này.
```

```java
// Dừng sớm để tránh render model chưa đủ dữ liệu.
```

### Ví dụ comment không tốt
```java
// Set name for student
student.setName(name);
```

```java
// Loop through list
for (StudentDto student : students) {
}
```

## Quy ước logging và exception
- Không nuốt exception im lặng.
- Nếu bắt exception, phải có lý do rõ ràng:
  - thêm context
  - đổi sang exception phù hợp hơn
  - xử lý fallback có chủ đích
- Log phải đủ thông tin để debug, nhưng không lộ dữ liệu nhạy cảm.
- Không spam log cho luồng bình thường nếu không cần thiết.

## Quy ước khi sửa code hiện có
- Ưu tiên làm theo style của vùng code gần nhất.
- Nếu style cũ hơi xấu nhưng ổn định, không refactor toàn file chỉ để đồng bộ hình thức.
- Nếu phải cải thiện readability, chỉ chỉnh trong phạm vi phần đang sửa.
- Khi sửa function cũ mà chưa có comment, bổ sung comment ngắn bằng tiếng Việt cho function đó.
- Khi sửa `service` có nhiều bước xử lý, bổ sung comment cho các block logic chính nếu phần code chưa đủ rõ.

## Quy ước khi tạo hoặc sửa page
- Khi tạo page mới hoặc chỉnh sửa page hiện có, luôn tham khảo HTML tương ứng trong `src/main/resources/mock`.
- File trong `mock` là nguồn tham khảo giao diện, bố cục, và nội dung mẫu.
- Không sao chép nguyên xi từ `mock` nếu khác với luồng production hiện tại.
- Khi đối chiếu page, cần kiểm tra đồng thời:
  - template production trong `src/main/resources/templates`
  - dữ liệu thật từ controller và service
  - JS/CSS thực tế trong `src/main/resources/static`
- Nếu page production khác page mock có chủ đích, ưu tiên luồng production và ghi rõ giả định khi cần.

## Checklist trước khi commit hoặc review
- Tên class, method, biến đã rõ nghĩa chưa.
- Business logic có bị trôi vào controller hoặc DTO không.
- Comment có thực sự cần thiết và có nói đúng lý do không.
- Tất cả function được thêm mới hoặc chỉnh sửa đã có comment ngắn bằng tiếng Việt chưa.
- Với `service` nhiều logic, các block xử lý chính đã được comment rõ chưa.
- Có import thừa, code chết, hoặc block debug còn sót lại không.
- Nếu đổi field hoặc API, đã kiểm tra các consumer liên quan chưa.
- Nếu sửa page, đã tham khảo HTML tương ứng trong `src/main/resources/mock` chưa.

## Gợi ý cập nhật tài liệu này
Khi team chốt thêm quy tắc mới, bổ sung theo nhóm:
- naming
- controller/service/repository rules
- comment style
- exception/logging
- test conventions
