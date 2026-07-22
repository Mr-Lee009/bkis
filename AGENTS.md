# AGENTS.md

## Purpose
This repository contains a Spring Boot training/project codebase for the BKIS education domain. It mixes:
- server-rendered Thymeleaf pages
- admin pages and supporting REST endpoints
- JPA/MySQL persistence
- SQL practice and seed scripts
- mock UI/reference screens under `src/main/resources/mock`

This file exists to help Codex work consistently every time the repository is opened.

## Project Snapshot
- Project name: `bkis`
- Language: Java 17
- Build tool: Gradle Kotlin DSL
- Frameworks: Spring Boot, Spring MVC, Spring Security, Spring Data JPA, Thymeleaf
- Database: MySQL by default, H2 dependency is present
- Default application port: `8888`

## Repository Map
- `src/main/java/vn/edu/bkis/controller`: MVC controllers
- `src/main/java/vn/edu/bkis/controller/admin`: admin controllers and REST endpoints
- `src/main/java/vn/edu/bkis/service`: business logic for user-facing flows
- `src/main/java/vn/edu/bkis/service/admin`: business logic for admin flows
- `src/main/java/vn/edu/bkis/repository`: Spring Data repositories
- `src/main/java/vn/edu/bkis/model`: JPA entities and enums
- `src/main/java/vn/edu/bkis/dto`: DTOs for pages and APIs
- `src/main/java/vn/edu/bkis/security`: authentication and security flow
- `src/main/resources/templates`: Thymeleaf templates
- `src/main/resources/static`: static JS, CSS, libraries
- `src/main/resources/mock`: mock/reference UI assets, not always production-backed
- `sql`: schema, mock data, practice SQL, ER diagram
- `docker`: local DB bootstrap assets
- `document`: design notes and page/API sketches

## Working Rules For Codex
- Read the surrounding feature files before editing behavior.
- Prefer minimal, targeted changes over broad refactors.
- Preserve existing naming and package structure unless the task requires restructuring.
- Do not treat files under `mock` as the source of truth for production behavior unless the user explicitly asks for mock-page work.
- When creating or editing pages, always reference the corresponding HTML under `src/main/resources/mock`.
- When changing admin student/account behavior, inspect controller, service, DTO, template, JS, and repository layers together.
- When changing authentication behavior, inspect the classes under `security` first.
- If a rule is unclear, infer from the nearest existing feature and note the assumption in the final response.
- All new or modified functions must have Vietnamese comments without diacritics that describe purpose, parameters, return type, and important exceptions when applicable.
- All new or modified functions with non-trivial logic must include Vietnamese comments without diacritics inside the body in the style `Step 1`, `Step 2`, `Step 3`.
- Service methods with multi-step logic must include Vietnamese block comments for the main processing steps.

## Editing Constraints
- Avoid changing generated, vendored, or third-party library assets unless the task is explicitly about them.
- Prefer editing these areas first:
  - controller logic in `src/main/java/vn/edu/bkis/controller`
  - service logic in `src/main/java/vn/edu/bkis/service`
  - templates in `src/main/resources/templates`
  - first-party JS/CSS in `src/main/resources/static`
- Avoid editing bundled library files under:
  - `src/main/resources/static/lib`
  - `src/main/resources/static/scss/bootstrap`
  - `src/main/resources/mock/lib`
  - `src/main/resources/mock/scss/bootstrap`
- Keep SQL changes compatible with the current MySQL-oriented setup unless the user requests a schema migration.

## Run And Verify
- Start app: `./gradlew bootRun` or `gradlew.bat bootRun`
- Build: `./gradlew build`
- Test: `./gradlew test`
- Default DB from `application.properties`:
  - URL: `jdbc:mysql://localhost:3306/bkis_edu`
  - user: `root`
  - password: `root`

## Review Checklist
- Does the change preserve current routes and template bindings?
- Are DTO fields aligned with template or JSON usage?
- Are repository queries and entity mappings still consistent?
- If UI behavior changed, was the related static JS checked?
- If DB-facing logic changed, is the expected schema/data available?
- If behavior is important, should a test be added?

## Known Gaps
- Automated test coverage is not obvious from the current repository layout.
- Some requirements are documented only in `document/*.md` and existing templates.
- Mock pages and production templates may diverge.

## Expected Agent Behavior
- Use this file as the first-pass operating guide.
- Use `docs/architecture.md` for codebase structure.
- Use `docs/business-rules.md` for domain assumptions.
- Use `docs/coding-conventions.md` for naming, code style, and comment conventions.
- Use `docs/common-pitfalls.md` to avoid repeated mistakes.
- When a new project-specific lesson is discovered, add it to one of those docs instead of relying on memory.
