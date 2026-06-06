# Signed URL cho video khoa hoc

## 1. Nguyen ly hoat dong

Signed URL la duong dan xem video co chu ky va thoi han su dung ngan.

Luong co ban:

1. DB khong luu public URL day du, chi luu `video_key`, vi du `source/abc-video.mp4`.
2. Khi nguoi dung bam xem video, frontend goi API xin link xem.
3. Backend kiem tra quyen xem video.
4. Backend tao URL co han, kem `expires` va `signature`.
5. Player dung URL nay de phat video.
6. Khi browser request URL stream, backend kiem tra han, chu ky va file path truoc khi tra video.

Vi du signed URL:

```text
/secure/videos/stream?key=source/abc-video.mp4&expires=1716600000&signature=abcxyz
```

### Uu diem

- Video khong public vinh vien.
- Link het han sau 5-15 phut.
- Link cu khong dung lai duoc sau khi het han.
- Co the kiem tra quyen truoc khi cap link.
- Phu hop voi video khoa hoc tra phi.

### Nhuoc diem

- Khong chong duoc quay man hinh.
- Nguoi dung van co the tai video trong thoi gian link con han neu biet ky thuat.
- Can them API tao signed URL va API stream video.
- Neu stream qua server, server se ton bang thong hon so voi dung object storage.

### So sanh voi cac phuong an khac

| Phuong an | Uu diem | Nhuoc diem |
| --- | --- | --- |
| Public `/uploads/**` | De lam, xem nhanh | Ai co link cung xem duoc |
| An nut download tren player | Rat de lam | Khong bao mat that |
| Signed URL | Can bang giua de trien khai va bao mat | Link van dung duoc trong thoi gian con han |
| S3/MinIO presigned URL | Giam tai server, phu hop production | Can object storage |
| HLS + signed segment | Bao ve tot hon file MP4 truc tiep | Trien khai phuc tap hon |
| DRM | Bao mat manh nhat | Chi phi va do phuc tap cao |

## 2. DB can them hoac update

Hien tai `lesson_videos.video_url` dang luu URL video. Co 2 cach xu ly.

### Cach 1: Giu cot cu va doi y nghia

Dung `video_url` de luu `video_key`, khong luu full URL nua.

Neu dang luu full URL:

```sql
UPDATE lesson_videos
SET video_url = REPLACE(video_url, 'http://localhost:8888/uploads/', '')
WHERE video_url LIKE 'http://localhost:8888/uploads/%';
```

Neu dang luu dang `/uploads/...`:

```sql
UPDATE lesson_videos
SET video_url = REPLACE(video_url, '/uploads/', '')
WHERE video_url LIKE '/uploads/%';
```

Sau update, gia tri nen co dang:

```text
source/abc-video.mp4
```

### Cach 2: Them cot moi `video_key`

Khuyen nghi dung cach nay vi ro nghia hon.

```sql
ALTER TABLE lesson_videos
ADD COLUMN video_key VARCHAR(500) NULL AFTER video_url;
```

Copy du lieu cu:

```sql
UPDATE lesson_videos
SET video_key = video_url;
```

Neu `video_key` dang la full URL:

```sql
UPDATE lesson_videos
SET video_key = REPLACE(video_key, 'http://localhost:8888/uploads/', '')
WHERE video_key LIKE 'http://localhost:8888/uploads/%';
```

Neu `video_key` dang la `/uploads/...`:

```sql
UPDATE lesson_videos
SET video_key = REPLACE(video_key, '/uploads/', '')
WHERE video_key LIKE '/uploads/%';
```

Ve lau dai co the giu `video_url` de tuong thich nguoc, nhung player nen dung `video_key`.

## 3. API can trien khai

### API tao signed URL

```http
GET /api/videos/{videoId}/signed-url
```

Response:

```json
{
  "url": "/secure/videos/stream?key=source/abc-video.mp4&expires=1716600000&signature=abcxyz"
}
```

Nhiem vu:

- Tim video theo `videoId`.
- Kiem tra quyen xem video.
- Lay `video_key`.
- Tao `expires`.
- Tao `signature`.
- Tra URL cho frontend.

Chu ky co the tao bang HMAC SHA-256:

```text
signature = HMAC_SHA256(key + ":" + expires, signingSecret)
```

### API stream video

```http
GET /secure/videos/stream?key=source/abc-video.mp4&expires=1716600000&signature=abcxyz
```

Nhiem vu:

- Kiem tra `expires` con han khong.
- Kiem tra `signature` dung khong.
- Chan path traversal nhu `../`.
- Doc file trong thu muc upload.
- Tra video dang stream.

### Cau hinh can them

```properties
app.video.signing-secret=bkis-video-secret-key-change-me
app.video.signed-url-valid-minutes=15
app.video.storage-root=uploads
```

### Frontend can doi

Khong set truc tiep:

```js
video.src = videoUrl;
```

Thay vao do, khi bam `Xem video`:

```http
GET /api/videos/{videoId}/signed-url
```

Sau do set:

```js
video.src = response.url;
```

## Thu tu trien khai khuyen nghi

1. Them cot `video_key`.
2. Khi upload xong, luu `video_key` thay vi full URL.
3. Them API `/api/videos/{videoId}/signed-url`.
4. Them API `/secure/videos/stream`.
5. Update modal xem video de lay signed URL truoc khi play.
6. Sau khi on dinh, khong public `/uploads/**` cho video khoa hoc nua.
