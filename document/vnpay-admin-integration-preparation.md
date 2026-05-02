# VNPAY Gateway Config

Cap nhat ngay: 2026-05-02

## Field chung

| Field | Muc dich |
|---|---|
| `code` | Ma gateway noi bo, vi du `vnpay` |
| `displayName` | Ten hien thi tren admin va checkout |
| `providerType` | Gia tri co dinh `VNPAY` |
| `paymentEndpoint` | URL tao/redirect thanh toan |
| `returnUrl` | URL user quay ve sau thanh toan |
| `ipnUrl` | URL VNPAY goi callback server-to-server |
| `ipAllowlist` | Danh sach IP callback neu can |
| `enabled` | Bat/tat gateway |
| `sandboxMode` | Test/live |
| `routingPriority` | Thu tu uu tien |
| `status` | Trang thai van hanh |

## Provider config

| Field | Muc dich |
|---|---|
| `credentials.tmnCode` | Ma terminal `vnp_TmnCode` |
| `credentials.hashSecret` | Secret dung ky va verify checksum |
| `requestDefaults.version` | Phien ban API |
| `requestDefaults.command` | Lenh thanh toan, thuong la `pay` |
| `requestDefaults.currCode` | Don vi tien te, thuong la `VND` |
| `requestDefaults.locale` | Ngon ngu request |
| `requestDefaults.orderType` | Loai don hang gui sang VNPAY |
| `requestDefaults.expireMinutes` | So phut het han giao dich |
| `features.allowQr` | Cho phep QR |
| `features.defaultBankCode` | Gia tri mac dinh cho `vnp_BankCode` |

## Request JSON mau

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
    "credentials": {
      "tmnCode": "2QXUI4J4",
      "hashSecret": "secret-value"
    },
    "requestDefaults": {
      "version": "2.1.0",
      "command": "pay",
      "currCode": "VND",
      "locale": "vn",
      "orderType": "other",
      "expireMinutes": 15
    },
    "features": {
      "allowQr": true,
      "defaultBankCode": ""
    }
  }
}
```

## Response JSON mau

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
      "tmnCode": "2QXUI4J4",
      "hashSecret": "********"
    }
  }
}
```

## Luu y

- `vnp_TxnRef` phai duy nhat.
- Khong xac nhan thanh cong chi dua vao redirect.
- Phai verify chu ky o IPN.
