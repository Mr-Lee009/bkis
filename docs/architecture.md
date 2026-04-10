# Architecture Notes

## Overview
`bkis` is a server-rendered Spring Boot application with Thymeleaf templates and an admin area. The codebase follows a mostly conventional layered structure:
- controller
- service
- repository
- model/entity
- DTO

The current project also includes mock pages and SQL practice assets, so not every file under the repo is part of the production request path.

## Main Runtime Flow
1. A request enters a controller under `src/main/java/vn/edu/bkis/controller` or `src/main/java/vn/edu/bkis/controller/admin`.
2. The controller delegates to a service in `service` or `service/admin`.
3. Services load entities or projections through Spring Data repositories.
4. Services map results into DTOs for templates or JSON responses.
5. Thymeleaf templates under `src/main/resources/templates` render the page, often with static JS from `src/main/resources/static/js`.

## Major Areas

### Public/User Area
- `HomeController` and `HomeService` support the landing/home experience.
- `CourseController` and `CourseDetailService` support course detail flows.
- `StudentController` and `StudentService` support student-facing pages or flows.
- `UploadFileS3Controller` and `UploadFileS3RestController` support upload-related features.

### Admin Area
- `DashboardController` and `DashboardService` support admin dashboard pages.
- `StudentsController` and `AdminStudentRestController` support admin student management.
- `AccountsController` and `AccountManagementService` support account management.
- Admin templates and fragments live under `src/main/resources/templates/admin`.
- Admin page JS is under `src/main/resources/static/js`, including `ad-03-students.js`.

### Security
- `SecurityConfig` configures request security.
- `CustomUserDetailsService` and `CustomUserDetails` support authentication.
- `AuthSuccessHandler`, `AuthFailureHandler`, and `CaptchaFilter` affect login behavior.
- Changes to login or access rules should always start in this package.

### Persistence
- JPA entities live under `src/main/java/vn/edu/bkis/model`.
- Repositories live under `src/main/java/vn/edu/bkis/repository`.
- MySQL is the primary configured database in `application.properties`.
- Seed/setup SQL exists under `sql`, `docker/mysql`, and `src/main/resources/data.sql`.

## Frontend Structure
- Thymeleaf templates are the production view layer.
- Shared layouts/fragments exist under `templates/layouts`, `templates/fragments`, and admin equivalents.
- Static assets live under `src/main/resources/static`.
- `src/main/resources/mock` contains reference/mock pages and should not be assumed to be production-backed unless explicitly used by the task.

## Documentation Already Present
- `document/admin-student-page-rest-design.md`
- `document/mock-function-design.md`

These files appear to contain feature-specific design intent and should be checked when working on the related areas.

## Practical Change Strategy
- For page-level issues: inspect controller, service, DTO, template, and page JS together.
- For REST issues: inspect controller, request/response DTO, service, repository, and frontend caller together.
- For data mismatches: inspect entity, repository, DTO mapping, and template field names together.
- For login/access issues: inspect security package before changing controllers.

## Current Structural Risks
- Mock/reference assets can mislead implementation decisions.
- Some conventions appear implicit rather than documented.
- Test coverage is limited or absent, so regressions are easier to introduce.
