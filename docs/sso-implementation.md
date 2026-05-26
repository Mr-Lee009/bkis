# SSO Implementation

## Mục tiêu
Tài liệu này mô tả phương án SSO tối thiểu đã được áp dụng cho repo `bkis`.

## Phương án đã chọn
- Giữ bảng `users` làm nguồn dữ liệu chính cho role và trạng thái tài khoản.
- Thêm bảng liên kết `user_oauth_accounts` để map tài khoản SSO với user local.
- Hỗ trợ song song:
  - đăng nhập bằng username/password
  - đăng nhập bằng Google
  - đăng nhập bằng Facebook

## Vì sao chọn cách này
- Repo hiện đã phụ thuộc nhiều vào bảng `users`.
- Quyền `ADMIN`, `TEACHER`, `STUDENT` đang được quản lý local.
- Admin flow, khóa tài khoản, và thống kê user nên tiếp tục dùng dữ liệu local.
- SSO chỉ nên đóng vai trò xác thực, không nên thay thế toàn bộ mô hình user hiện có.

## Schema tối thiểu

### Bảng giữ nguyên
- `users`

### Bảng mới
- `user_oauth_accounts`

Các cột chính:
- `id`
- `user_id`
- `provider`
- `provider_user_id`
- `email`
- `display_name`
- `profile_picture_url`
- `created_at`
- `updated_at`

## Ý nghĩa dữ liệu
- `user_id`: trỏ về user local trong bảng `users`
- `provider`: `GOOGLE` hoặc `FACEBOOK`
- `provider_user_id`: định danh duy nhất phía provider
- `email`: email trả về từ provider để hỗ trợ link tài khoản
- `display_name`, `profile_picture_url`: dữ liệu tiện ích để đồng bộ thông tin hiển thị

## Luồng đăng nhập
1. Người dùng bấm nút Google hoặc Facebook tại trang login.
2. Spring Security chuyển hướng sang provider tương ứng.
3. Provider trả user info về ứng dụng.
4. Ứng dụng chuẩn hóa dữ liệu profile.
5. Ứng dụng tìm bản ghi trong `user_oauth_accounts`.
6. Nếu đã có mapping:
   - lấy user local tương ứng
   - kiểm tra trạng thái khóa
7. Nếu chưa có mapping:
   - tìm user local theo email
   - nếu có thì link vào user hiện có
   - nếu chưa có thì tạo user local mới với role mặc định `STUDENT`
8. Đăng nhập vào hệ thống với authority lấy từ role local.

## Quy tắc hiện tại
- User bị khóa local sẽ không được đăng nhập bằng SSO.
- User tạo mới từ SSO được gán role mặc định `STUDENT`.
- Username local được sinh tự động từ email hoặc tên hiển thị.
- Hệ thống yêu cầu provider trả về email để link hoặc tạo user local.

## Cấu hình Google
Ví dụ cấu hình:

```properties
app.security.sso.google-enabled=true
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

## Cấu hình Facebook
Ví dụ cấu hình:

```properties
app.security.sso.facebook-enabled=true
spring.security.oauth2.client.registration.facebook.client-id=${FACEBOOK_CLIENT_ID}
spring.security.oauth2.client.registration.facebook.client-secret=${FACEBOOK_CLIENT_SECRET}
spring.security.oauth2.client.registration.facebook.scope=email,public_profile
```

## Lưu ý với Facebook
- Facebook thường yêu cầu cấu hình app kỹ hơn Google.
- Email có thể không được trả về nếu app chưa được cấp quyền phù hợp hoặc tài khoản người dùng không chia sẻ email.
- Với code hiện tại, thiếu email sẽ bị chặn đăng nhập vì bảng `users` yêu cầu email duy nhất và không null.

## Hướng nâng cấp tiếp theo
- Cho phép cấu hình role mặc định theo provider hoặc domain email.
- Thêm màn hình link/unlink tài khoản SSO trong profile.
- Ghi log audit cho lần đăng nhập SSO.
- Nếu cần kiểm soát token lâu dài, cân nhắc persistent login hoặc session store riêng.
## Cap Nhat 2026-05-25

Luá»“ng SSO hiá»‡n táº¡i Ä‘ang tÃ­ch há»£p song song vá»›i form login truyá»n thá»‘ng:

- form login: `POST /login`
- captcha image: `GET /captcha/image`
- Google start: `GET /oauth2/authorization/google`
- Facebook start: `GET /oauth2/authorization/facebook`

`CaptchaFilter` chá»‰ kiá»ƒm tra captcha cho `POST /login`, nÃªn khÃ´ng cháº·n luá»“ng Google/Facebook.

UI login chá»‰ hiá»‡n nÃºt SSO khi:

- `app.security.sso.google-enabled=true` hoáº·c
- `app.security.sso.facebook-enabled=true`

NgoÃ i cÃ¡c cÅ© hiá»ƒn thá»‹ nÃºt, Spring Security cÃ²n cáº§n táº¡o Ä‘Æ°á»£c `ClientRegistrationRepository` tá»« `spring.security.oauth2.client.registration.*` thÃ¬ `SecurityConfig` má»›i báº­t `oauth2Login(...)`.

Vá»›i repo hiá»‡n táº¡i:

- local nÃªn cáº¥u hÃ¬nh trong `src/main/resources/application-dev.properties`
- production nÃªn cáº¥u hÃ¬nh trong `src/main/resources/application-prod.properties` + env runtime
