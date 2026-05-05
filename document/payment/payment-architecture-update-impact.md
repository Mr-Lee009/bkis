# Payment Architecture Update Impact

Cap nhat ngay: 2026-05-05

## Muc tieu

Tai lieu nay tom tat neu BKIS ap dung thiet ke moi cho payment gateway:
- field chung o top-level
- field rieng o `providerConfig`
- `ipnUrl` thay cho `webhookUrl`

thi DB va kien truc code se thay doi nhu the nao.

## 1. Ket luan ngan

Neu di theo huong moi, anh huong chinh se nam o 4 khu vuc:
- bang `payment_gateways`
- bang `payments`
- DTO / entity / service cho admin gateway
- service thanh toan thuc te va xu ly IPN

Thay doi lon nhat:
- bo giam cac cot mang y nghia rieng tung gateway
- them `provider_config_json`
- doi `webhook_url` thanh `ipn_url`

## 2. Cac bang se thay doi

### 2.1 Bang `payment_gateways`

Day la bang thay doi nhieu nhat.

#### Cot nen giu

| Cot | Muc dich |
|---|---|
| `id` | Khoa chinh |
| `code` | Ma gateway noi bo |
| `display_name` | Ten hien thi |
| `provider_type` | Loai gateway, vi du `VNPAY`, `MOMO` |
| `description` | Mo ta ngan |
| `payment_endpoint` | Endpoint tao payment |
| `return_url` | URL user quay ve |
| `ip_allowlist` | Danh sach IP callback neu can |
| `enabled` | Bat/tat gateway |
| `sandbox_mode` | Test/live |
| `routing_priority` | Thu tu uu tien |
| `transaction_fee_percent` | Phi giao dich |
| `success_rate_percent` | Chi so admin |
| `status` | Trang thai van hanh |
| `created_by` | Audit |
| `updated_by` | Audit |
| `created_at` | Audit |
| `updated_at` | Audit |

#### Cot nen doi ten

| Cot cu | Cot moi | Ly do |
|---|---|---|
| `webhook_url` | `ipn_url` | Dung thuat ngu chuan hon cho payment gateway |

#### Cot nen bo ve lau dai

| Cot | Ly do |
|---|---|
| `merchant_id` | Khong con phu hop cho tat ca gateway |
| `partner_code` | Ten cot khong dung chung cho VNPAY va MoMo |
| `secret_key` | Khong du de chua het credential rieng cua gateway |

#### Cot nen them

| Cot | Kieu du lieu | Muc dich |
|---|---|---|
| `ipn_url` | `VARCHAR(500)` | URL callback server-to-server |
| `provider_config_json` | `JSON` | Luu credential va request default rieng theo provider |

#### Dinh dang `provider_config_json`

Nen luu theo cau truc:

```json
{
  "credentials": {},
  "requestDefaults": {},
  "features": {}
}
```

Vi du VNPAY:

```json
{
  "credentials": {
    "tmnCode": "2QXUI4J4",
    "hashSecret": "encrypted-value"
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
```

Vi du MoMo:

```json
{
  "credentials": {
    "partnerCode": "MOMOXXXX",
    "accessKey": "encrypted-value",
    "secretKey": "encrypted-value"
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
```

### 2.2 Bang `payments`

Bang `payments` hien co chua du field de support payment gateway that.

#### Cot nen them

| Cot | Kieu du lieu | Muc dich |
|---|---|---|
| `invoice_no` | `VARCHAR(50)` | Ma hoa don noi bo |
| `gateway_code` | `VARCHAR(50)` | Gateway nao xu ly giao dich |
| `gateway_order_ref` | `VARCHAR(100)` | Ma tham chieu do BKIS/gui sang gateway, vi du `vnp_TxnRef`, `orderId` |
| `gateway_transaction_id` | `VARCHAR(100)` | Ma giao dich do gateway tra ve, vi du `vnp_TransactionNo`, `transId` |
| `gateway_response_code` | `VARCHAR(30)` | Ma ket qua do gateway tra ve |
| `currency` | `VARCHAR(10)` | Don vi tien te |
| `payment_method` | `VARCHAR(50)` | Cach thanh toan, vi du QR, ATM, wallet |
| `paid_at` | `DATETIME` | Thoi diem thanh toan thanh cong |
| `failure_reason` | `VARCHAR(500)` | Ly do that bai neu can |
| `signature_verified` | `BIT` | Danh dau callback da verify chu ky |
| `raw_gateway_payload` | `TEXT` hoac `JSON` | Payload callback/return de doi soat |
| `ipn_processed_at` | `DATETIME` | Thoi diem xu ly IPN |

#### Ghi chu

- `gateway_order_ref` la cot chung cho ca VNPAY va MoMo
- khong nen dat cot rieng `vnp_transaction_no` hay `momo_trans_id` trong bang chung
- `raw_gateway_payload` co the de `TEXT` de don gian, hoac `JSON` neu muon query sau nay

### 2.3 Bang `payment_events`

Neu muon van hanh on dinh, nen co bang nay.

Muc dich:
- luu lich su thay doi trang thai payment
- luu su kien IPN/return/refund/query
- ho tro debug va doi soat

#### Cot co ban

| Cot | Muc dich |
|---|---|
| `payment_id` | Lien ket payment |
| `event_type` | Loai su kien |
| `old_status` | Trang thai cu |
| `new_status` | Trang thai moi |
| `gateway_code` | Gateway lien quan |
| `payload` | Noi dung callback/response |
| `created_at` | Thoi diem su kien |

### 2.4 Bang `refund_requests`

Bang nay khong bat buoc cho MVP, nhung nen co neu huong toi refund.

Muc dich:
- quan ly yeu cau hoan tien
- tach refund khoi bang `payments`

## 3. SQL de xuat cho bang `payment_gateways`

### Phuong an chuyen tiep an toan

```sql
ALTER TABLE payment_gateways
    ADD COLUMN ipn_url VARCHAR(500) NULL AFTER return_url,
    ADD COLUMN provider_config_json JSON NULL AFTER ip_allowlist;
```

Sau khi code da doc duoc field moi, moi tinh tiep den:

```sql
ALTER TABLE payment_gateways
    DROP COLUMN merchant_id,
    DROP COLUMN partner_code,
    DROP COLUMN secret_key,
    DROP COLUMN webhook_url;
```

### Phuong an schema cuoi cung

```sql
CREATE TABLE payment_gateways (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    payment_endpoint VARCHAR(500),
    return_url VARCHAR(500),
    ipn_url VARCHAR(500),
    ip_allowlist TEXT,
    provider_config_json JSON,
    enabled BIT DEFAULT b'1',
    sandbox_mode BIT DEFAULT b'0',
    routing_priority INT DEFAULT 99,
    transaction_fee_percent DECIMAL(8,2) DEFAULT 0,
    success_rate_percent DECIMAL(8,2) DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_gateways_code (code)
);
```

## 4. Kien truc code se thay doi nhu the nao

### 4.1 Model / Entity

File bi anh huong truc tiep:
- `src/main/java/vn/edu/bkis/model/PaymentGateway.java`

Thay doi chinh:
- doi `webhookUrl` -> `ipnUrl`
- bo dan cac field flat:
  - `merchantId`
  - `partnerCode`
  - `secretKey`
- them field moi:
  - `providerConfigJson` hoac map sang object config

### 4.2 DTO

File bi anh huong truc tiep:
- `src/main/java/vn/edu/bkis/dto/admin/payment/PaymentGatewayUpsertRequest.java`
- `PaymentGatewayDto`

Thay doi chinh:
- doi `webhookUrl` -> `ipnUrl`
- them:
  - `providerConfig`
- response DTO phai tra:
  - field chung
  - `providerConfig` da mask secret

### 4.3 Service admin gateway

File bi anh huong truc tiep:
- `src/main/java/vn/edu/bkis/service/admin/AdminPaymentGatewayService.java`

Thay doi chinh:
- validate field chung
- validate field rieng theo `providerType`
- serialize `providerConfig` thanh JSON de luu DB
- deserialize JSON khi doc len
- mask secret truoc khi tra frontend

### 4.4 UI admin

File bi anh huong truc tiep:
- `src/main/resources/templates/admin/ad-05-payment-gateways.html`
- `src/main/resources/static/js/ad-05-payment-gateways.js`

Thay doi chinh:
- doi label `Webhook URL` -> `IPN URL`
- tach form field chung va field rieng
- render field rieng theo `providerType`
  - VNPAY: `tmnCode`, `hashSecret`, `orderType`
  - MoMo: `partnerCode`, `accessKey`, `secretKey`, `storeId`

### 4.5 Payment runtime

Neu tich hop that, se can them hoac cap nhat cac service:
- `PaymentGatewayAdapter`
- `VnPayGatewayAdapter`
- `MoMoGatewayAdapter`
- service tao payment
- service xu ly IPN

Thay doi chinh:
- doc gateway config theo `providerType`
- lay credential tu `providerConfig`
- tao request gateway theo adapter rieng
- verify callback theo adapter rieng

## 5. Luong xu ly moi sau khi cap nhat

1. Admin luu gateway.
2. Backend luu field chung vao cot rieng.
3. Backend luu field rieng vao `provider_config_json`.
4. Checkout doc gateway theo `enabled`, `status`, `routing_priority`.
5. Adapter tao request thanh toan theo config.
6. Gateway goi `ipn_url`.
7. Backend verify callback va cap nhat `payments`.
8. Neu thanh cong, mo `enrollments`.

## 6. Huong triem khai an toan

De tranh sua mot luc qua nhieu:

### Buoc 1

- Them `ipn_url`
- Them `provider_config_json`
- Chua xoa cot cu

### Buoc 2

- Sua entity, DTO, service, UI de doc/ghi field moi
- Van co the giu compatibility voi du lieu cu

### Buoc 3

- Migrate du lieu cu sang `provider_config_json`
- Cap nhat seed data va SQL bootstrap

### Buoc 4

- Xoa cot cu khi code da on dinh:
  - `merchant_id`
  - `partner_code`
  - `secret_key`
  - `webhook_url`

## 7. Ket luan

Neu cap nhat theo huong moi, bang thay doi nhieu nhat la:
- `payment_gateways`

Bang can mo rong de van hanh that la:
- `payments`
- `payment_events`
- `refund_requests` neu co refund

Kien truc code se chuyen tu:
- gateway config dang flat, kho mo rong

sang:
- common config + provider-specific config
- adapter theo tung gateway
- IPN flow ro rang va de maintain hon
