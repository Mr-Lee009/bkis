# Nghiep Vu Payment Flow

Cap nhat ngay: 2026-05-05

## Muc tieu

Tai lieu nay mo ta luong payment tong the cua BKIS theo cach de doc:
- admin cau hinh gateway
- hoc vien thanh toan khoa hoc
- he thong nhan IPN/callback
- cap nhat payment va enrollment
- doi soat va refund

## Pham vi

Gom 2 man nghiep vu chinh:
- cau hinh gateway: `admin/payment-gateways`
- van hanh giao dich: `payments`, `checkout`, `enrollments`

## Vai tro

| Vai tro | Trach nhiem |
|---|---|
| `ADMIN` | Cau hinh gateway, xem giao dich, doi soat, refund |
| `ACCOUNTING` | Theo doi doanh thu, doi soat, xu ly hoan tien |
| `STUDENT` | Thanh toan khoa hoc, xem ket qua va bien lai |
| `SYSTEM` | Tao payment, nhan IPN, cap nhat trang thai, mo enrollment |

## Khai niem chinh

### Payment Gateway

La cau hinh ket noi toi mot nha cung cap thanh toan, vi du:
- VNPAY
- MoMo
- Chuyen khoan ngan hang

Gateway co 2 nhom du lieu:
- field chung: endpoint, return URL, IPN URL, status, sandbox, priority
- field rieng: credential va request default theo tung provider

### Payment Transaction

La mot lan thanh toan cua hoc vien cho mot don hang/khoa hoc.

Trang thai de xuat:
- `PENDING`: da tao giao dich, cho thanh toan
- `PROCESSING`: gateway dang xu ly
- `COMPLETED`: thanh toan thanh cong
- `FAILED`: thanh toan that bai
- `CANCELLED`: giao dich bi huy
- `REFUNDED`: hoan tien toan phan
- `PARTIALLY_REFUNDED`: hoan tien mot phan

### IPN

La callback server-to-server tu gateway.

IPN la nguon su that de cap nhat payment.
Return URL chi dung de hien thi ket qua cho nguoi dung.

## Bang du lieu lien quan

### `payment_gateways`

Dung de luu cau hinh gateway.

Field chinh:
- `code`
- `display_name`
- `provider_type`
- `payment_endpoint`
- `return_url`
- `ipn_url`
- `provider_config_json`
- `enabled`
- `sandbox_mode`
- `routing_priority`
- `status`

### `payments`

Dung de luu giao dich thanh toan.

Nen co them cac field:
- `gateway_code`
- `gateway_transaction_id`
- `currency`
- `payment_method`
- `paid_at`
- `failure_reason`
- `raw_gateway_payload`

### `payment_events`

Dung de luu lich su thay doi trang thai payment va su kien callback.

### `refund_requests`

Dung de luu yeu cau hoan tien.

## Luong 1: Admin cau hinh gateway

1. Admin mo man Payment Gateways.
2. Admin tao hoac sua gateway.
3. He thong validate field chung.
4. He thong validate field rieng theo `providerType`.
5. He thong luu field chung vao cot rieng.
6. He thong luu field rieng vao `provider_config_json`.
7. He thong test gateway neu admin bam test.

## Luong 2: Hoc vien checkout

1. Hoc vien vao trang checkout.
2. He thong lay danh sach gateway `enabled=true`, `status=LIVE`.
3. He thong sap xep theo `routing_priority`.
4. Hoc vien chon gateway.
5. He thong tao `payment` voi trang thai `PENDING`.
6. He thong goi gateway hoac tao URL thanh toan.
7. Hoc vien duoc redirect sang gateway hoac hien thi QR.

## Luong 3: Gateway tra ket qua

Co 2 duong:

### Return URL

- User duoc quay lai BKIS.
- BKIS hien thi ket qua tam thoi.
- Khong dung return URL lam can cu cuoi cung de mo enrollment.

### IPN URL

- Gateway goi callback server-to-server.
- BKIS verify chu ky/signature.
- BKIS doi chieu so tien, ma don hang, gateway code.
- Neu hop le, BKIS cap nhat `payments.status`.
- Neu thanh cong, BKIS mo `enrollment`.

## Luong 4: Cap enrollment

Chi mo enrollment khi:
- payment hop le
- IPN verify thanh cong
- payment dat `COMPLETED`

Khong mo enrollment khi:
- moi chi co redirect
- payment dang `PENDING`
- payment loi hoac chu ky khong hop le

## Luong 5: Doi soat

1. Admin hoac accounting xem danh sach giao dich.
2. Loc theo ngay, gateway, trang thai.
3. Doi chieu payment noi bo voi log/bao cao gateway.
4. Danh dau giao dich can review neu co lech amount, status, transaction id.

## Luong 6: Refund

1. Admin tao refund request.
2. He thong kiem tra payment co du dieu kien refund khong.
3. Neu gateway ho tro refund API, he thong goi refund.
4. Neu khong, xu ly thu cong.
5. Cap nhat `refund_requests` va `payments.status`.

## Quy tac nghiep vu quan trong

- Khong tin trang thai tu browser neu chua verify.
- IPN phai idempotent.
- Mot payment `COMPLETED` khong duoc mo enrollment 2 lan.
- Secret khong duoc tra plain text ve UI.
- Secret nen ma hoa truoc khi luu production.
- Gateway `DISABLED` khong duoc hien thi trong checkout.

## MVP de xuat

MVP nen gom:
- admin cau hinh gateway
- checkout tao payment `PENDING`
- redirect sang gateway
- nhan IPN va verify
- cap nhat `COMPLETED`
- mo enrollment
- admin xem danh sach payment

Phase sau moi them:
- refund that qua gateway
- doi soat tu dong
- payout
- settlement file
