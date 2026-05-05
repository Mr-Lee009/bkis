# MoMo Gateway Config

Cap nhat ngay: 2026-05-02

## Field chung

| Field | Muc dich |
|---|---|
| `code` | Ma gateway noi bo, vi du `momo` |
| `displayName` | Ten hien thi tren admin va checkout |
| `providerType` | Gia tri co dinh `MOMO` |
| `paymentEndpoint` | URL create payment |
| `returnUrl` | URL user quay ve sau thanh toan |
| `ipnUrl` | URL MoMo goi callback server-to-server |
| `ipAllowlist` | Danh sach IP callback neu can |
| `enabled` | Bat/tat gateway |
| `sandboxMode` | Test/live |
| `routingPriority` | Thu tu uu tien |
| `status` | Trang thai van hanh |

## Provider config

| Field | Muc dich |
|---|---|
| `credentials.partnerCode` | Ma merchant do MoMo cap |
| `credentials.accessKey` | Key dung ky request |
| `credentials.secretKey` | Secret dung tao va verify signature |
| `requestDefaults.requestType` | Loai request, phase 1 nen la `payWithMethod` |
| `requestDefaults.lang` | Ngon ngu request |
| `requestDefaults.autoCapture` | Capture tu dong neu flow can |
| `requestDefaults.storeId` | Ma cua hang neu merchant can tach store |
| `features.allowWallet` | Cho phep vi MoMo |
| `features.allowAtm` | Cho phep ATM |
| `features.allowCreditCard` | Cho phep the |
| `features.allowQr` | Cho phep QR |

## Request JSON mau

```json
{
  "code": "momo",
  "displayName": "MoMo",
  "providerType": "MOMO",
  "paymentEndpoint": "https://test-payment.momo.vn/v2/gateway/api/create",
  "returnUrl": "https://learn.bkis.edu.vn/payments/return/momo",
  "ipnUrl": "https://learn.bkis.edu.vn/api/payments/ipn/momo",
  "enabled": true,
  "sandboxMode": true,
  "routingPriority": 2,
  "status": "REVIEW",
  "providerConfig": {
    "credentials": {
      "partnerCode": "MOMOXXXX",
      "accessKey": "access-key",
      "secretKey": "secret-key"
    },
    "requestDefaults": {
      "requestType": "payWithMethod",
      "lang": "vi",
      "autoCapture": true,
      "storeId": "BKIS"
    },
    "features": {
      "allowWallet": true,
      "allowAtm": true,
      "allowCreditCard": true,
      "allowQr": true
    }
  }
}
```

## Response JSON mau

```json
{
  "id": 2,
  "code": "momo",
  "displayName": "MoMo",
  "providerType": "MOMO",
  "paymentEndpoint": "https://test-payment.momo.vn/v2/gateway/api/create",
  "returnUrl": "https://learn.bkis.edu.vn/payments/return/momo",
  "ipnUrl": "https://learn.bkis.edu.vn/api/payments/ipn/momo",
  "enabled": true,
  "sandboxMode": true,
  "routingPriority": 2,
  "status": "REVIEW",
  "providerConfig": {
    "credentials": {
      "partnerCode": "MOMOXXXX",
      "accessKey": "********",
      "secretKey": "********"
    }
  }
}
```

## Luu y

- `orderId` va `requestId` phai unique.
- Phai verify `signature` o IPN.
- Nen respond `204 No Content`.
