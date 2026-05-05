# Nghiep Vu Cau Hinh Cong Thanh Toan

## Muc Dich

Admin quan ly gateway thanh toan ma khong can sua code checkout.

## API hien co

- `GET /api/admin/payment-gateways`
- `GET /api/admin/payment-gateways/{code}`
- `POST /api/admin/payment-gateways`
- `PUT /api/admin/payment-gateways/{code}`
- `POST /api/admin/payment-gateways/{code}/test`
- `POST /api/admin/payment-gateways/test-all`

## Bang du lieu de xuat

`payment_gateways`

| Cot | Muc dich |
|---|---|
| `code` | Ma gateway noi bo |
| `display_name` | Ten hien thi |
| `provider_type` | Loai gateway |
| `description` | Mo ta ngan |
| `payment_endpoint` | Endpoint tao payment |
| `return_url` | URL user quay ve |
| `ipn_url` | URL callback server-to-server |
| `ip_allowlist` | Danh sach IP callback neu can |
| `provider_config_json` | JSON luu credential va request default rieng theo gateway |
| `enabled` | Bat/tat gateway |
| `sandbox_mode` | Test/live |
| `routing_priority` | Thu tu uu tien |
| `transaction_fee_percent` | Phi giao dich |
| `success_rate_percent` | Chi so admin |
| `status` | Trang thai van hanh |

## Request JSON chung

```json
{
  "code": "vnpay",
  "displayName": "VNPay",
  "providerType": "VNPAY",
  "paymentEndpoint": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
  "returnUrl": "https://learn.bkis.edu.vn/payments/return/vnpay",
  "ipnUrl": "https://learn.bkis.edu.vn/api/payments/ipn/vnpay",
  "enabled": true,
  "sandboxMode": true,
  "routingPriority": 1,
  "status": "LIVE",
  "providerConfig": {
    "credentials": {},
    "requestDefaults": {},
    "features": {}
  }
}
```

## Response JSON chung

```json
{
  "id": 1,
  "code": "vnpay",
  "displayName": "VNPay",
  "providerType": "VNPAY",
  "paymentEndpoint": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
  "returnUrl": "https://learn.bkis.edu.vn/payments/return/vnpay",
  "ipnUrl": "https://learn.bkis.edu.vn/api/payments/ipn/vnpay",
  "enabled": true,
  "sandboxMode": true,
  "routingPriority": 1,
  "status": "LIVE",
  "providerConfig": {
    "credentials": {
      "hashSecret": "********"
    }
  }
}
```

## Luong nghiep vu

1. Admin tao hoac sua gateway.
2. He thong validate field chung va field rieng theo `providerType`.
3. He thong luu field chung vao cot rieng.
4. He thong luu field rieng vao `provider_config_json`.
5. Frontend chi nhan secret da mask.

## Luu y

- Nen dung `ipnUrl` thay cho `webhookUrl`.
- Secret khong tra plain text ve UI.
- Secret trong DB nen ma hoa truoc khi production.
