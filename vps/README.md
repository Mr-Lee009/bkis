# VPS Jar Package

Thu muc nay da duoc rut gon theo cach deploy chi bang `.jar`.

## File Dang Co

- `docker-compose.yml`
- `Dockerfile`
- `docker/.env.docker`
- `docker/mysql/create_table.sql`
- `docker/mysql/data.sql`
- `build/libs/bkis-0.0.1-SNAPSHOT.jar`

## Khong Con Can

- Source code `src`
- Gradle wrapper
- `build.gradle.kts`
- `settings.gradle.kts`

VPS khong can build lai source. Docker se chay truc tiep tu file jar da copy san.

## Cach Chay Tren VPS

```bash
cd /path/to/vps
docker compose up -d --build
```

## Luu Y

- Kiem tra lai `docker/.env.docker` truoc khi chay.
- Dockerfile dang copy file jar theo mau:

```text
build/libs/*.jar
```

- `docker-compose.yml` dang map MySQL ra host qua port `3310`.
- Neu trong `build/libs` co nhieu hon 1 file `.jar`, can dam bao chi giu lai file jar can deploy.
