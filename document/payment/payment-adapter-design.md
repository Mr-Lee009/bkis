# Payment Adapter Design

## Mục tiêu

Tài liệu này mô tả ý tưởng triển khai hoàn chỉnh cho adapter MoMo / VNPAY / ZaloPay trước khi viết code.

Mục tiêu của mình là giữ `PaymentController` và `PaymentService` làm lớp điều phối, còn phần đặc thù provider nằm trong adapter riêng.

## Ý tưởng tổng thể

Luồng xử lý sẽ đi theo các lớp sau:

1. `PaymentController` nhận request từ frontend.
2. `PaymentService` tạo giao dịch `PENDING` và lưu DB.
3. `PaymentService` đọc config gateway từ `PaymentGatewayConfigRepository`.
4. `PaymentGatewayResolver` chọn adapter phù hợp theo provider.
5. Adapter cụ thể build request, ký chữ ký, gọi API provider.
6. Provider trả kết quả về `payUrl` hoặc lỗi.
7. Callback quay lại `PaymentController`.
8. `PaymentService` verify callback, chuẩn hóa kết quả, cập nhật `SUCCESS` hoặc `FAILED`.

## Contract mình sẽ giữ

### Interface chung

Mỗi provider sẽ implement contract `PaymentGateway`.

Contract này nên chịu trách nhiệm:

- tạo request tới provider
- ký chữ ký
- parse response provider
- verify callback

Nếu cần mở rộng, mình sẽ tách thêm các method sau:

- `createPayment(...)`
- `verifyCallback(...)`
- `queryPayment(...)`

## Cấu trúc code đề xuất

```text
src/main/java/vn/edu/bkis/service/gateway/
  PaymentGateway.java
  PaymentGatewayResolver.java
  momo/MomoPaymentGateway.java
  vnpay/VnPayPaymentGateway.java
  zalopay/ZaloPayPaymentGateway.java
  dto/
    GatewayCreatePaymentResult.java
    GatewayCallbackResult.java
```

## Cách mapping dữ liệu

### 1. Từ checkout form sang giao dịch nội bộ

Frontend hiện gửi form checkout gồm các field như:

- `courseId`
- `paymentMethod`
- `acceptedTerms`
- `phone`
- `learningGoal`
- `learningMode`
- `couponCode`

`PaymentService` sẽ dùng dữ liệu này để tạo `PaymentTransactionEntity` trước, sau đó mới đi ra provider.

### 2. Từ giao dịch nội bộ sang request provider

Mình sẽ map dữ liệu theo nguyên tắc:

- `paymentCode` -> `requestId` hoặc mã giao dịch nội bộ
- `orderId` -> `orderId` của provider
- `amount` -> số tiền thanh toán
- `description` -> `orderInfo` hoặc tương đương
- `returnUrl` -> `redirectUrl`
- callback URL -> `ipnUrl` / `notifyUrl`

## Ví dụ MoMo

### Request mẫu của MoMo

```json
{
  "partnerCode": "MOMO",
  "partnerName": "Test",
  "storeId": "MomoTestStore",
  "requestId": "MOMO1780653480973",
  "amount": "1000",
  "orderId": "MOMO1780653480973",
  "orderInfo": "pay with MoMo",
  "redirectUrl": "https://webhook.site/b3088a6a-2d17-4f8d-a383-71389a6c600b",
  "ipnUrl": "https://webhook.site/b3088a6a-2d17-4f8d-a383-71389a6c600b",
  "lang": "vi",
  "requestType": "payWithMethod",
  "autoCapture": true,
  "extraData": "",
  "orderGroupId": "",
  "signature": "83c160684f718e50f20d67f49b50abc850f6da20a0e24b249918378e6d530049"
}
```

### Mapping mình sẽ dùng

- `partnerCode`: lấy từ config gateway
- `partnerName`: lấy từ config hoặc metadata JSON
- `storeId`: optional, lấy từ config nếu có
- `requestId`: dùng `paymentCode` hoặc một mã sinh từ `paymentCode`
- `amount`: lấy từ `PaymentTransactionEntity.amount`
- `orderId`: lấy từ `PaymentTransactionEntity.orderId`
- `orderInfo`: mô tả khóa học, ví dụ `Thanh toán khóa học ABC`
- `redirectUrl`: URL frontend hoặc URL return của hệ thống
- `ipnUrl`: callback endpoint của hệ thống
- `lang`: mặc định `vi`
- `requestType`: mặc định `payWithMethod`
- `autoCapture`: `true`
- `extraData`: nếu chưa có dữ liệu thêm thì để rỗng
- `orderGroupId`: optional
- `signature`: sinh bằng secret key từ config

### Response mẫu của MoMo

```json
{
  "partnerCode": "MOMO",
  "orderId": "MOMO1780653480973",
  "requestId": "MOMO1780653480973",
  "amount": 1000,
  "responseTime": 1783482320466,
  "resultCode": 0,
  "message": "Thành công.",
  "payUrl": "https://test-payment.momo.vn/v2/gateway/pay?...",
  "shortLink": "https://test-payment.momo.vn/v2/gateway/pay?..."
}
```

### Cách xử lý response

- `resultCode == 0` -> coi là tạo payment thành công
- `payUrl` -> redirect user sang cổng thanh toán
- `shortLink` -> có thể dùng nếu frontend muốn rút gọn UI
- `resultCode != 0` -> cập nhật transaction lỗi hoặc giữ `PENDING` và ghi reason

### Response lỗi mẫu của MoMo

```json
{
  "partnerCode": "MOMO",
  "orderId": "MOMO1780653480973",
  "requestId": null,
  "responseTime": 1783509038811,
  "resultCode": 41,
  "message": "Yêu cầu bị từ chối vì trùng orderId."
}
```

### Cách xử lý lỗi trùng orderId

Mình sẽ xem đây là lỗi idempotency / duplicate request.

Khi gặp lỗi này, service nên:

- không tạo transaction mới nếu `orderId` đã tồn tại
- trả lại transaction cũ cho frontend nếu trạng thái còn hợp lệ
- nếu transaction cũ đã `FAILED`, cho phép tạo lại giao dịch mới với `orderId` mới

## Chiến lược cho VNPAY và ZaloPay

### VNPAY

Mình sẽ giữ logic tương tự MoMo nhưng mapping khác field:

- `vnp_TmnCode`
- `vnp_Amount`
- `vnp_TxnRef`
- `vnp_OrderInfo`
- `vnp_ReturnUrl`
- `vnp_IpnUrl`
- `vnp_SecureHash`

### ZaloPay

Mình sẽ tách flow theo style ZaloPay:

- `app_id`
- `app_user`
- `app_trans_id`
- `app_time`
- `amount`
- `item`
- `embed_data`
- `callback_url`
- `mac`

## Resolver

`PaymentGatewayResolver` sẽ chỉ làm nhiệm vụ chọn đúng bean theo provider.

Ví dụ:

- `momo` -> `MomoPaymentGateway`
- `vnpay` -> `VnPayPaymentGateway`
- `zalo_pay` -> `ZaloPayPaymentGateway`

Resolver không nên chứa logic build request hay ký signature.

## Callback handling

Callback nên đi qua một service flow chung.

Mình sẽ xử lý theo 3 bước:

1. đọc `paymentCode` hoặc mã tương đương từ callback
2. verify chữ ký theo provider
3. chuẩn hóa trạng thái provider về `SUCCESS` hoặc `FAILED`

## Mapping trạng thái

Mình sẽ chuẩn hóa trạng thái provider về status nội bộ như sau:

- `0`, `SUCCESS`, `00` -> `COMPLETED`
- lỗi xác thực hoặc callback fail -> `FAILED`
- đang chờ xử lý -> `PENDING`

Hiện tại hệ thống đang dùng `PaymentStatus.COMPLETED` cho trạng thái thành công, nên mình sẽ map provider success về enum này để không phá schema hiện tại.

## Idempotency

Đây là phần mình sẽ ưu tiên.

Nguyên tắc:

- `paymentCode` phải duy nhất
- `orderId` phải duy nhất trong một transaction đang mở
- callback lặp lại không được tạo payment mới
- nếu provider gửi callback nhiều lần thì chỉ update transaction hiện có

## Những file mình sẽ chạm khi code thật

- `src/main/java/vn/edu/bkis/service/PaymentService.java`
- `src/main/java/vn/edu/bkis/service/gateway/PaymentGateway.java`
- `src/main/java/vn/edu/bkis/service/gateway/PaymentGatewayResolver.java`
- `src/main/java/vn/edu/bkis/service/gateway/momo/MomoPaymentGateway.java`
- `src/main/java/vn/edu/bkis/service/gateway/vnpay/VnPayPaymentGateway.java`
- `src/main/java/vn/edu/bkis/service/gateway/zalopay/ZaloPayPaymentGateway.java`
- `src/main/java/vn/edu/bkis/controller/PaymentController.java`
- `src/main/java/vn/edu/bkis/dto/payment/*`

## Kết luận ngắn

Ý tưởng của mình là:

- PaymentService tạo record nội bộ trước
- adapter chỉ lo đặc thù provider
- callback và query status đi qua PaymentController
- trạng thái nội bộ vẫn giữ ổn định với DB hiện tại

Nếu bạn đồng ý, bước code tiếp theo là mình sẽ tách từng adapter provider theo đúng contract này, bắt đầu từ MoMo vì bạn đã đưa mẫu request/response cụ thể.
