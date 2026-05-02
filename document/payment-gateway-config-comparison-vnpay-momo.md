# So sanh Config VNPAY va MoMo

Cap nhat ngay: 2026-05-02

## Field chung

| Field | Muc dich |
|---|---|
| `code` | Ma gateway noi bo |
| `displayName` | Ten hien thi |
| `providerType` | Loai gateway |
| `description` | Mo ta ngan |
| `paymentEndpoint` | Endpoint tao payment |
| `returnUrl` | URL user quay ve |
| `ipnUrl` | URL callback server-to-server |
| `ipAllowlist` | Danh sach IP callback neu can |
| `enabled` | Bat/tat gateway |
| `sandboxMode` | Test/live |
| `routingPriority` | Thu tu uu tien |
| `transactionFeePercent` | Phi giao dich |
| `successRatePercent` | Chi so admin |
| `status` | Trang thai van hanh |

## Field rieng

| Nhom | VNPAY | MoMo |
|---|---|---|
| Credential chinh | `tmnCode` | `partnerCode` |
| Key ky request | `hashSecret` | `accessKey`, `secretKey` |
| Request type | `command=pay` | `requestType=payWithMethod` |
| Tham so bo sung | `orderType`, `locale`, `expireMinutes`, `defaultBankCode` | `lang`, `autoCapture`, `storeId` |
| QR | `allowQr`, `defaultBankCode=VNPAYQR` | `allowQr`, co the dung `payUrl`/`qrCodeUrl` |

## Dinh dang `providerConfig`

```json
{
  "credentials": {},
  "requestDefaults": {},
  "features": {}
}
```

## Khuyen nghi

- Doi `webhookUrl` thanh `ipnUrl`
- Them `providerConfigJson` vao DB
- Khong them nhieu cot rieng cho tung gateway
