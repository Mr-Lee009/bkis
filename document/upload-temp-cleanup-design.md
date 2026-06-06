# Upload chunk cleanup

## 1. Van de can xu ly

Upload video dang dung co che upload theo chunk. Neu upload loi giua chung, cac chunk da gui len server co the nam lai tren o dia.

Vi du:

```text
upload-tmp/{uploadId}/chunk_0
upload-tmp/{uploadId}/chunk_1
upload-tmp/{uploadId}/chunk_2
```

Neu khong cleanup, cac file tam nay se chiem dung luong may chu va khong con gia tri su dung.

## 2. Nguyen tac thiet ke

He thong cleanup theo 3 lop:

1. Client abort: neu frontend biet upload loi, goi API huy upload.
2. Server TTL cleanup: server tu xoa upload session qua han.
3. Orphan folder cleanup: server quet `upload-tmp` de xoa folder mo coi sau khi app restart.

Client abort giup don nhanh, nhung khong du tin cay vi nguoi dung co the dong tab hoac mat mang. Vi vay server-side cleanup la bat buoc.

## 3. Cau truc thu muc

Temp chunk chi nam trong thu muc rieng, khong public qua `/uploads/**`:

```text
upload-tmp/{uploadId}/chunk_0
upload-tmp/{uploadId}/chunk_1
```

File hoan tat nam trong thu muc dich:

```text
uploads/source/{uuid-file-name}.mp4
uploads/account/{user}/{uuid-file-name}.jpg
```

Khong tron temp folder va final folder de tranh cleanup nham file that.

## 4. API can co

### Init upload

```http
POST /upload/api/init
```

Request:

```text
fileName=video.mp4
totalSize=104857600
totalChunks=10
folder=/source
```

Response:

```json
{
  "uploadId": "uuid"
}
```

### Upload chunk

```http
POST /upload/api/chunk
```

Request multipart:

```text
uploadId=uuid
chunkIndex=0
file=<binary>
```

### Complete upload

```http
POST /upload/api/complete?uploadId=uuid
```

Response:

```json
{
  "url": "http://localhost:8888/uploads/source/uuid-video.mp4"
}
```

Sau khi complete thanh cong:

- Ghep cac chunk.
- Xoa `upload-tmp/{uploadId}`.
- Xoa session khoi memory.

### Abort upload

```http
POST /upload/api/abort
```

Request:

```text
uploadId=uuid
```

Response:

```json
{
  "aborted": true
}
```

Khi abort:

- Xoa session neu con trong memory.
- Xoa folder `upload-tmp/{uploadId}` neu ton tai.
- Chi duoc xoa trong `upload-tmp`, khong xoa thu muc final.

## 5. Cau hinh

```properties
app.upload.temp-ttl-minutes=120
app.upload.cleanup.fixed-delay-ms=1800000
```

Y nghia:

- `app.upload.temp-ttl-minutes`: upload tam qua thoi gian nay se bi xoa.
- `app.upload.cleanup.fixed-delay-ms`: tan suat job cleanup. Mac dinh 30 phut.

## 6. Xu ly loi tren frontend

Widget upload can giu `uploadId` sau khi init.

Neu loi xay ra sau init:

1. Hien thi loi cho nguoi dung.
2. Goi `/upload/api/abort`.
3. Xoa uploadId khoi state frontend.
4. Cho phep chon file va upload lai.

Neu nguoi dung dong tab khi upload dang chay:

- Frontend goi abort bang `navigator.sendBeacon`.
- Neu beacon that bai, server TTL cleanup van se xu ly sau.

## 7. Xu ly loi tren backend

Backend can bat cac truong hop:

- `uploadId` khong ton tai.
- `chunkIndex` sai.
- Chunk rong.
- Thieu chunk khi complete.
- Loi merge file.
- Folder upload co path traversal.
- Folder tmp mo coi sau restart app.

Khi merge loi, file final dang ghi do phai bi xoa de tranh sinh file hong.

## 8. Test case can co

1. Upload thanh cong: file final ton tai, folder tmp bi xoa.
2. Loi chunk giua chung: frontend goi abort, folder tmp bi xoa.
3. Loi complete: file final hong bi xoa, folder tmp co the bi abort hoac TTL cleanup.
4. Dong tab giua chung: sendBeacon abort neu co the.
5. App restart giua upload: folder tmp mo coi bi cleanup sau TTL.
6. Folder `/source` hoac `/account/user` khong bi cleanup nham.

## 9. Ghi chu bao mat

- Khong tin folder tu client, backend phai chuan hoa path.
- Khong cho `../` trong folder dich.
- Ten file nen co UUID de tranh ghi de.
- Chi cleanup trong `upload-tmp`.
- Video khoa hoc tra phi ve lau dai nen dung signed URL thay vi public `/uploads/**`.
