# Huong Dan Build Lai Docker Tren VPS

## Muc Dich

Tai lieu nay huong dan cach build lai ung dung BKIS tren VPS khi co thay doi code, sau do chay lai bang Docker Compose.

Repo hien tai dang dung:
- `docker-compose.yml`
- `Dockerfile`
- `docker/.env.docker`

## Luu Y Quan Trong Truoc Khi Build

Dockerfile hien tai **khong tu build source code Java**. File Docker image app dang copy truc tiep file:

`build/libs/*.jar`

Vi vay, tren VPS bat buoc phai:

1. Pull code moi
2. Build jar bang Gradle
3. Moi duoc `docker compose build`

Neu bo qua buoc build jar, Docker se loi vi khong tim thay `build/libs/*.jar`.

## Cau Truc Dang Deploy

`docker-compose.yml` dang co 2 service:
- `db`: MySQL 8.0
- `app`: Spring Boot app build tu source local

App doc bien moi truong tu:

`docker/.env.docker`

MySQL data duoc luu qua Docker volume:

`db_data`

Dieu nay co nghia la khi `docker compose down`, du lieu DB van con neu khong xoa volume.

## Chuan Bi VPS

Can co san:
- Docker
- Docker Compose plugin
- Git
- Java 17

Kiem tra nhanh:

```bash
docker --version
docker compose version
git --version
java -version
```

## Cac File Can Kiem Tra

Truoc khi rebuild, xem lai:
- `docker-compose.yml`
- `Dockerfile`
- `docker/.env.docker`

Neu thay doi thong tin DB, port, profile, OAuth key hoac secret, phai cap nhat file env truoc khi chay lai.

## Quy Trinh Build Lai Sau Khi Update Code

Di chuyen vao thu muc project:

```bash
cd /path/to/bkis
```

Lay code moi:

```bash
git pull origin <branch>
```

Build jar moi:

```bash
./gradlew clean build
```

Neu server khong cho phep test lau, co the dung:

```bash
./gradlew clean bootJar
```

Sau do build lai image:

```bash
docker compose build app
```

Chay lai container:

```bash
docker compose up -d
```

Neu muon dung lai toan bo stack:

```bash
docker compose down
docker compose up -d --build
```

## Lenh Day Duoc De Xuat Khi Deploy

```bash
cd /path/to/bkis
git pull origin main
./gradlew clean build
docker compose down
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

## Cach Kiem Tra Sau Khi Deploy

Kiem tra trang thai container:

```bash
docker compose ps
```

Kiem tra log app:

```bash
docker compose logs -f app
```

Kiem tra log mysql:

```bash
docker compose logs -f db
```

Kiem tra port:
- App: `8888`
- MySQL: `3306`

Thu mo:

```text
http://<VPS_IP>:8888
```

## Khi Chi Muon Rebuild Rieng App

Neu khong thay doi DB schema bootstrap va khong muon dung lai MySQL:

```bash
cd /path/to/bkis
git pull origin main
./gradlew clean build
docker compose build app
docker compose up -d app
docker compose logs -f app
```

Lenh nay giu service `db` dang chay.

## Khi Can Reset Ca Du Lieu MySQL

Chi dung khi chap nhan mat data local tren VPS:

```bash
docker compose down -v
docker compose up -d --build
```

`-v` se xoa ca volume `db_data`.

Khong dung lenh nay tren production neu chua backup.

## Backup Truoc Khi Nang Cap

Neu VPS dang chay production, nen backup DB truoc:

```bash
docker exec mysql_bkis mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" bkis_edu > backup_bkis_edu.sql
```

Hoac backup bang user duoc cap quyen phu hop.

## Luu Y Ve Profile Prod

`Dockerfile` dang set:

```dockerfile
ENV SPRING_PROFILES_ACTIVE=prod
```

Ngoai ra `docker/.env.docker` cung dang co:

```env
SPRING_PROFILES_ACTIVE=prod
```

Nghia la container app se chay theo profile `prod`.

Repo nay hien dang dung placeholder config qua environment variable, nen khi deploy VPS can dam bao file `docker/.env.docker` co du:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- cac bien secret khac neu app can

## Luu Y Ve OAuth / Secret

Khong commit secret that vao Git.

Neu co:
- Google OAuth client id / secret
- secret key payment gateway
- thong tin SMTP

thi nen dua vao file env tren VPS hoac secret manager, khong hard-code trong source.

## Loi Thuong Gap

### 1. Loi khong tim thay `build/libs/*.jar`

Nguyen nhan:
- Chua chay `./gradlew build`

Cach xu ly:

```bash
./gradlew clean build
docker compose build app
```

### 2. App len container nhung khong ket noi duoc DB

Kiem tra:
- `SPRING_DATASOURCE_URL`
- user/password trong `docker/.env.docker`
- service `db` da healthy chua

Xem log:

```bash
docker compose logs -f app
docker compose logs -f db
```

### 3. Build app thanh cong nhung giao dien khong thay doi

Kiem tra:
- Da pull dung branch chua
- Da build lai jar chua
- Da `docker compose up -d --build` chua
- Browser co dang cache file JS/CSS cu khong

## De Xuat Nang Cap Sau Nay

Deploy hien tai dung source code tren VPS de build image. Cach nay chay duoc, nhung ve lau dai nen doi sang mot trong hai huong:

1. CI build jar/image truoc, VPS chi `docker pull` va `docker compose up -d`
2. Dung multi-stage Dockerfile de Docker tu build jar trong luc build image

Huong 2 se giam phu thuoc vao Java/Gradle tren VPS.
