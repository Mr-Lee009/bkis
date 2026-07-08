# Payment Flow

## Mục tiêu

Tài liệu này mô tả luồng thanh toán khi học viên mua một khóa học. Flow cũ trong `CourseSignupController` đã được rút về phần render trang, còn việc tạo payment và callback được đẩy sang `PaymentController` và `PaymentService`.

## Luồng xử lý

1. Frontend gọi `PaymentController` để tạo payment hoặc query trạng thái.
2. `PaymentController` nhận request và chuyển toàn bộ xử lý sang `PaymentService`.
3. `PaymentService` tạo giao dịch `PENDING` trong `PaymentTransactionRepository`.
4. `PaymentService` tự đọc cấu hình gateway từ `PaymentGatewayConfigRepository`.
5. `PaymentService` gọi `PaymentGatewayResolver` để lấy adapter theo provider.
6. `PaymentGatewayResolver` quản lý các implementation `PaymentGateway` qua contract chung.
7. `PaymentGateway` sẽ được implement bởi MoMo, VNPAY và ZaloPay adapter.
8. Adapter sẽ build request, sign request và gọi API của provider.
9. Callback từ provider quay về `PaymentController`, sau đó `PaymentService` verify và chuẩn hóa kết quả.
10. `PaymentService` cập nhật `SUCCESS` hoặc `FAILED` vào `PaymentTransactionRepository`.

## Ghi chú

- Trong code hiện tại, trạng thái thành công vẫn đang được lưu theo giá trị `COMPLETED` của enum `PaymentStatus` hiện có.
- Các adapter MoMo, VNPAY và ZaloPay vẫn đang ở mức contract/todo, chưa nối API thật.
