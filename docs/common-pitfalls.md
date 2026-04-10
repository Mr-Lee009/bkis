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
