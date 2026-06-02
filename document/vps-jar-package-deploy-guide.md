# Huong Dan Build Va Copy Bo Deploy Len Thu Muc VPS

## Muc Dich

Tai lieu nay huong dan cach:

1. Build ung dung BKIS tren may local
2. Chuan bi dung cac file can copy
3. Copy vao thu muc `vps/`
4. Day bo `vps/` len Ubuntu VPS de chay bang Docker Compose

Tai lieu nay dung cho luong deploy:

- build `.jar` tai may local
- copy bo package da rut gon vao VPS
- VPS khong can source code day du

## Thu Muc Dich Dang Dung

Repo hien tai da co san thu muc:

`vps/`

Thu muc nay la bo package toi thieu de deploy, gom:

- `vps/docker-compose.yml`
- `vps/Dockerfile`
- `vps/README.md`
- `vps/docker/.env.docker`
- `vps/docker/mysql/create_table.sql`
- `vps/docker/mysql/data.sql`
- `vps/build/libs/bkis-0.0.1-SNAPSHOT.jar`

## Khi Nao Dung Cach Nay

Dung cach nay khi:

- ban build code tren may local
- ban muon copy bo package gon len VPS
- ban khong muon cai dat Gradle va source code day du tren server

Khong dung tai lieu nay neu ban muon:

- `git pull` source code tren VPS
- build truc tiep tren VPS bang `./gradlew`

## Cac File Can Co Trong Bo Deploy

Day la cac file/to thu muc can copy len VPS:

- `vps/docker-compose.yml`
- `vps/Dockerfile`
- `vps/docker/.env.docker`
- `vps/docker/mysql/`
- `vps/build/libs/bkis-0.0.1-SNAPSHOT.jar`

Neu doi ten file jar sau khi build, can dam bao:

- file jar nam trong `vps/build/libs/`
- `Dockerfile` van copy dung mau `build/libs/*.jar`

## Buoc 1: Build Jar Tren May Local

Dung PowerShell tai root repo:

```powershell
./gradlew.bat clean build
```

Neu muon build nhanh hon:

```powershell
./gradlew.bat clean bootJar
```

Sau khi build xong, jar se nam tai:

`build/libs/bkis-0.0.1-SNAPSHOT.jar`

## Buoc 2: Copy Jar Moi Vao Thu Muc VPS

Can copy file jar vua build vao:

`vps/build/libs/`

Neu muon lam bang PowerShell:

```powershell
Copy-Item .\build\libs\bkis-0.0.1-SNAPSHOT.jar .\vps\build\libs\bkis-0.0.1-SNAPSHOT.jar -Force
```

Neu trong `build/libs/` co nhieu file jar, hay kiem tra lai dung file can deploy.

## Buoc 3: Kiem Tra File Cau Hinh Truoc Khi Copy Len VPS

Xem lai:

- `vps/docker-compose.yml`
- `vps/Dockerfile`
- `vps/docker/.env.docker`

Can dac biet kiem tra cac gia tri sau trong `vps/docker/.env.docker`:

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_PROFILES_ACTIVE`

Neu app can them env khac cho production nhu SSO, remember-me key hoac upload config, can bo sung vao file env nay hoac dieu chinh `docker-compose.yml` cho phu hop.

## Buoc 4: Copy Thu Muc VPS Len Server

Ban co 2 cach pho bien:

### Cach 1: Copy nguyen thu muc `vps/` bang `scp`

Tu may local:

```bash
scp -r vps user@your-server:/opt/bkis
```

Sau lenh nay, server se co:

`/opt/bkis/vps`

### Cach 2: Dong goi roi copy

Tai may local:

```powershell
Compress-Archive -Path .\vps\* -DestinationPath .\vps-package.zip -Force
```

Sau do copy file zip len server, giai nen vao thu muc deploy.

## Buoc 5: Chay Tren VPS

SSH vao VPS:

```bash
ssh user@your-server
```

Di chuyen vao thu muc deploy:

```bash
cd /opt/bkis/vps
```

Build image va chay:

```bash
docker compose up -d --build
```

## Buoc 6: Kiem Tra Sau Khi Chay

Kiem tra container:

```bash
docker compose ps
```

Xem log:

```bash
docker compose logs -f app
docker compose logs -f db
```

Kiem tra app:

- App: `http://<server-ip>:8888`
- MySQL host port: `3310`

## Quy Trinh Day Duoc De Xuat Moi Lan Deploy

Tai may local:

```powershell
./gradlew.bat clean build
Copy-Item .\build\libs\bkis-0.0.1-SNAPSHOT.jar .\vps\build\libs\bkis-0.0.1-SNAPSHOT.jar -Force
```

Copy len VPS:

```bash
scp -r vps user@your-server:/opt/bkis
```

Tren VPS:

```bash
cd /opt/bkis/vps
docker compose up -d --build
```

## Neu Muon Update Phien Ban Moi

Moi lan code thay doi:

1. build lai jar tai local
2. copy de file jar moi vao `vps/build/libs/`
3. copy lai thu muc `vps/` len server
4. chay lai:

```bash
docker compose up -d --build
```

Neu muon dung va build lai sach:

```bash
docker compose down
docker compose up -d --build
```

## Loi Thuong Gap

### Docker build bao khong tim thay jar

Nguyen nhan:

- chua build jar
- chua copy jar vao `vps/build/libs/`
- file jar bi doi ten nhung `Dockerfile` khong copy trung

### App len container nhung khong ket noi duoc DB

Can kiem tra:

- `vps/docker/.env.docker`
- `SPRING_DATASOURCE_URL`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- service `db` da len hay chua

### Da copy file moi nhung app van la ban cu

Can kiem tra:

- file jar trong `vps/build/libs/` da duoc thay moi that chua
- da chay `docker compose up -d --build` chua
- co can `docker compose down` truoc khi build lai khong

## Ghi Chu

- Thu muc `vps/` la bo package deploy, khong phai noi de phat trien source code.
- Neu thay doi cau truc env hoac route app, nen kiem tra lai `vps/docker/.env.docker`.
- Neu sau nay ban muon deploy bang `git pull` tren VPS, nen dung tai lieu khac thay vi luong package nay.
