# Common Pitfalls

## Purpose
Use this file to record mistakes, hidden conventions, and implementation traps discovered while working on the repo. This is the fastest way to make future Codex sessions better without relying on memory.

## Initial Pitfalls
- `src/main/resources/mock` contains reference pages and assets, not necessarily live application behavior.
- `document/*.md` may contain feature intent that is not obvious from code alone.
- MySQL is the active default database in `application.properties`, even though H2 exists as a dependency.
- UI behavior may depend on matching names across controller model attributes, DTO fields, Thymeleaf bindings, and static JS.
- Admin features often span backend and frontend files; changing only one layer is likely incomplete.
- Vendor/bootstrap asset directories should usually be left untouched.

## Add New Lessons In This Format
```md
## 2026-04-10
- Area: admin student creation
- Pitfall: Frontend payload names must match `AdminStudentCreateRequest` exactly.
- Symptom: Form submits but fields bind as null.
- Prevention: Check request DTO, JS payload builder, and controller method signature together.
```
## 2026-05-25
- Area: login and SSO
- Pitfall: The Google/Facebook button depends on `app.security.sso.*-enabled`, not only on `spring.security.oauth2.client.registration.*`.
- Symptom: OAuth client id/secret are configured, but the SSO button does not appear on `/login`.
- Prevention: Check `application-dev.properties` or `application-prod.properties`, `AuthController`, and `01-login.html` together.

## 2026-05-25
- Area: login captcha
- Pitfall: The captcha is now server-side and session-based, so field names and error binding must stay aligned across template, JS, filter, and controller.
- Symptom: The captcha image loads, but login always fails or the error message does not show.
- Prevention: Keep `captchaAnswer`, `remember-me`, `/captcha/image`, and `errorMessage` consistent across `01-login.html`, `01-login.js`, `CaptchaFilter`, and `AuthController`.
